/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.service.preferencespanel

import groovy.transform.CompileStatic
import org.jd.gui.service.platform.PlatformService
import org.jd.gui.spi.PreferencesPanel

import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.util.regex.Pattern

@CompileStatic
class GrepCodeFileViewerPreferencesProvider extends JPanel implements PreferencesPanel, DocumentListener, ActionListener {

    static final String ACTIVATED = 'GrepCodeFileViewerPreferences.activated'
    static final String FILTERS = 'GrepCodeFileViewerPreferences.filters'

    static final String DEFAULT_FILTERS_VALUE = '+org +com.google +com.grepcode +com.ibm +com.jcraft +com.springsource +com.sun -com +java +javax +sun +sunw'

    static final Pattern CONTROL_PATTERN = Pattern.compile('\\s*(\\s[+-]([a-zA-Z_0-9$_.]+\\s*)?)*')

    JCheckBox searchOnGrepCodeCheckBox
    JTextArea filtersTextArea
    JButton resetButton
    PreferencesPanel.PreferencesPanelChangeListener listener
    Color errorBackgroundColor = Color.RED
    Color defaultBackgroundColor

    GrepCodeFileViewerPreferencesProvider() {
        super(new BorderLayout())

        searchOnGrepCodeCheckBox = new JCheckBox('Search source code on grepcode.com for:')
        searchOnGrepCodeCheckBox.addActionListener(this)

        filtersTextArea = new JTextArea()
        filtersTextArea.font = font
        filtersTextArea.lineWrap = true
        filtersTextArea.document.addDocumentListener(this)
        defaultBackgroundColor = filtersTextArea.background

        def spacer = new JComponent() {}
        def scrollPane = new JScrollPane(filtersTextArea)

        if (PlatformService.instance.isMac) {
            spacer.preferredSize = new Dimension(28, -1)
            scrollPane.preferredSize = new Dimension(-1, 56)
        } else if (PlatformService.instance.isLinux) {
            spacer.preferredSize = new Dimension(22, -1)
            scrollPane.preferredSize = new Dimension(-1, 56)
        } else {
            spacer.preferredSize = new Dimension(22, -1)
            scrollPane.preferredSize = new Dimension(-1, 50)
        }

        resetButton = new JButton('Reset')
        resetButton.addActionListener(this)

        def southPanel = new JPanel(new BorderLayout())
        southPanel.add(resetButton, BorderLayout.EAST)

        add(searchOnGrepCodeCheckBox, BorderLayout.NORTH)
        add(spacer, BorderLayout.WEST)
        add(scrollPane, BorderLayout.CENTER)
        add(southPanel, BorderLayout.SOUTH)
    }

    // --- PreferencesPanel --- //
    @Override String getPreferencesGroupTitle() { 'Viewer' }
    @Override String getPreferencesPanelTitle() { 'GrepCode' }

    @Override
    public void init(Color errorBackgroundColor) {
        this.errorBackgroundColor = errorBackgroundColor
    }

    @Override public boolean isActivated() { true }

    @Override
    void loadPreferences(Map<String, String> preferences) {
        filtersTextArea.enabled = resetButton.enabled = searchOnGrepCodeCheckBox.selected = !'false'.equals(preferences.get(ACTIVATED))
        filtersTextArea.text = preferences.get(FILTERS) ?: DEFAULT_FILTERS_VALUE
    }

    @Override
    void savePreferences(Map<String, String> preferences) {
        preferences.put(ACTIVATED, Boolean.toString(searchOnGrepCodeCheckBox.selected))
        preferences.put(FILTERS, filtersTextArea.text.trim())
    }

    @Override
    boolean arePreferencesValid() {
        (' ' + filtersTextArea.text) ==~ CONTROL_PATTERN
    }

    @Override
    void addPreferencesChangeListener(PreferencesPanel.PreferencesPanelChangeListener listener) {
        this.listener = listener
    }

    // --- DocumentListener --- //
    @Override void insertUpdate(DocumentEvent e) { onTextChange() }
    @Override void removeUpdate(DocumentEvent e) { onTextChange() }
    @Override void changedUpdate(DocumentEvent e) { onTextChange() }

    void onTextChange() {
        filtersTextArea.background = arePreferencesValid() ? defaultBackgroundColor : errorBackgroundColor
        listener?.preferencesPanelChanged(this)
    }

    // --- ActionListener --- //
    @Override
    void actionPerformed(ActionEvent e) {
        if (e.source == searchOnGrepCodeCheckBox) {
            filtersTextArea.enabled = resetButton.enabled = searchOnGrepCodeCheckBox.selected
        } else {
            filtersTextArea.text = DEFAULT_FILTERS_VALUE
            filtersTextArea.requestFocus()
        }
    }
}
