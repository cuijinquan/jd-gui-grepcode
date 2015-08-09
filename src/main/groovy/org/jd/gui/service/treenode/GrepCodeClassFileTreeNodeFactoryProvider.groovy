/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.service.treenode

import groovy.transform.CompileStatic
import org.jd.gui.api.API
import org.jd.gui.api.feature.ContainerEntryGettable
import org.jd.gui.api.feature.ContentCopyable
import org.jd.gui.api.feature.ContentSavable
import org.jd.gui.api.feature.ContentSearchable
import org.jd.gui.api.feature.ContentSelectable
import org.jd.gui.api.feature.FocusedTypeGettable
import org.jd.gui.api.feature.IndexesChangeListener
import org.jd.gui.api.feature.LineNumberNavigable
import org.jd.gui.api.feature.PreferencesChangeListener
import org.jd.gui.api.feature.UriGettable
import org.jd.gui.api.feature.UriOpenable
import org.jd.gui.api.model.Container
import org.jd.gui.api.model.Indexes
import org.jd.gui.service.preferencespanel.GrepCodeFileViewerPreferencesProvider
import org.jd.gui.service.util.net.GrepCodeHttpClient
import org.jd.gui.view.component.ClassFilePage
import org.jd.gui.view.component.JavaFilePage
import org.jd.gui.view.component.TypePage
import org.jd.gui.view.data.TreeNodeBean

import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.tree.DefaultMutableTreeNode
import java.awt.BorderLayout
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class GrepCodeClassFileTreeNodeFactoryProvider extends ClassFileTreeNodeFactoryProvider {
    protected static final Factory FACTORY = new Factory();
    protected static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor()

    @Override
    public <T extends DefaultMutableTreeNode & ContainerEntryGettable & UriGettable> T make(API api, Container.Entry entry) {
        int lastSlashIndex = entry.path.lastIndexOf('/')
        def name = entry.path.substring(lastSlashIndex+1)

        return new AbstractTypeFileTreeNodeFactoryProvider.FileTreeNode(
                entry,
                new TreeNodeBean(label:name, icon:CLASS_FILE_ICON),
                FACTORY
        )
    }

    /**
     * Page & tip factory
     */
    protected static class Factory extends ClassFileTreeNodeFactoryProvider.Factory {
        @Override
        public <T extends JComponent & UriGettable> T makePage(API api, Container.Entry entry) {
            String source = GrepCodeHttpClient.getCachedSourceFile(entry)

            if (source) {
                // Source found -> Display original source code immediately
                return new JavaFilePage(api, new EntryWrapper(entry, source))
            } else {
                // Source not found -> Display the decompiled source code and search the original source in background
                return new DynamicPage(api, entry);
            }
        }
    }

    /**
     * This page 1) display the decompiled source code, 2) call GrepCode API and 3) display the original source code.
     */
    @CompileStatic
    protected static class DynamicPage
            extends JPanel
            implements ContentCopyable, ContentSavable, ContentSearchable, ContentSelectable, FocusedTypeGettable,
                       IndexesChangeListener, LineNumberNavigable, PreferencesChangeListener,
                       Runnable, UriGettable, UriOpenable
    {
        API api
        Container.Entry entry
        TypePage page
        URI lastOpenedUri

        DynamicPage(API api, Container.Entry entry) {
            super(new BorderLayout())
            this.api = api
            this.entry = entry
            // Display the decompiled source code
            add(page = new ClassFilePage(api, entry))
            // Execute a background task
            EXECUTOR.execute(this)
        }

        // --- ContentCopyable --- //
        @Override void copy() { page.copy() }

        // --- ContentSavable --- //
        @Override String getFileName() { page.getFileName() }
        @Override void save(API api, OutputStream outputStream) { page.save(api, outputStream) }

        // --- ContentSearchable --- //
        @Override boolean highlightText(String text, boolean caseSensitive) { page.highlightText(text, caseSensitive) }
        @Override void findNext(String text, boolean caseSensitive) { page.findNext(text, caseSensitive) }
        @Override void findPrevious(String text, boolean caseSensitive) { page.findPrevious(text, caseSensitive) }

        // --- ContentSearchable --- //
        @Override
        void selectAll() { page.selectAll() }

        // --- FocusedTypeGettable --- //
        @Override String getFocusedTypeName() { page.getFocusedTypeName() }

        // --- IndexesChangeListener --- //
        @Override void indexesChanged(Collection<Indexes> collectionOfIndexes) { page.indexesChanged(collectionOfIndexes) }

        // --- LineNumberNavigable --- //
        @Override int getMaximumLineNumber() { page.getMaximumLineNumber() }
        @Override void goToLineNumber(int lineNumber) { page.goToLineNumber(lineNumber) }
        @Override boolean checkLineNumber(int lineNumber) { page.checkLineNumber(lineNumber) }

        // --- PreferencesChangeListener --- //
        @Override void preferencesChanged(Map<String, String> preferences) { page.preferencesChanged(preferences) }

        // --- Runnable --- //
        @Override
        void run() {
            boolean activated = !'false'.equals(api.preferences.get(GrepCodeFileViewerPreferencesProvider.ACTIVATED))

            if (activated && accepted(entry)) {
                String source = GrepCodeHttpClient.downloadRemoteSourceFile(entry)

                if (source != null) {
                    // Replace the decompiled source code by the original
                    removeAll()
                    add(page = new JavaFilePage(api, new EntryWrapper(entry, source)))

                    if (lastOpenedUri) {
                        page.openUri(lastOpenedUri)
                    }
                }
            }
        }

        boolean accepted(Container.Entry entry) {
            // 'filters' example : '+org +com.google +com.grepcode +com.ibm +com.jcraft +com.springsource +com.sun -com +java +javax +sun +sunw'
            def filters = api.preferences.get(GrepCodeFileViewerPreferencesProvider.FILTERS) ?: GrepCodeFileViewerPreferencesProvider.DEFAULT_FILTERS_VALUE
            def path = entry.path

            for (String filter : filters.tokenize()) {
                String prefix = filter.substring(1).replace('.', '/')

                if (prefix.charAt(prefix.length()-1) !=  '/')
                    prefix += '/'

                if (path.startsWith(prefix)) {
                    return (filter.charAt(0) == '+')
                }
            }

            return true
        }

        // --- UriGettable --- //
        @Override URI getUri() { entry.uri }

        // --- UriOpenable --- //
        @Override boolean openUri(URI uri) { page.openUri(lastOpenedUri = uri) }
    }

    protected static class EntryWrapper implements Container.Entry {
        Container.Entry entry
        String source

        EntryWrapper(Container.Entry entry, String source) {
            this.entry = entry
            this.source = source
        }

        @Override Container getContainer() { entry.container }
        @Override Container.Entry getParent() { entry.parent }
        @Override URI getUri() { entry.uri }
        @Override String getPath() { entry.path }
        @Override boolean isDirectory() { entry.isDirectory() }
        @Override long length() { entry.length() }
        @Override InputStream getInputStream() { new ByteArrayInputStream(source.getBytes()) }
        @Override Collection<Container.Entry> getChildren() { entry.children }
    }
}
