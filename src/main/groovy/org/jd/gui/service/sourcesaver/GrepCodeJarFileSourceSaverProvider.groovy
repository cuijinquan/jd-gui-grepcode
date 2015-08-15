/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.service.sourcesaver

import org.jd.gui.api.API
import org.jd.gui.api.model.Container
import org.jd.gui.service.preferencespanel.GrepCodeFileSaverPreferencesProvider
import org.jd.gui.service.util.net.GrepCodeHttpClient
import org.jd.gui.spi.SourceSaver

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.zip.ZipInputStream

class GrepCodeJarFileSourceSaverProvider extends ZipFileSourceSaverProvider {

    @Override
    void saveContent(API api, SourceSaver.Controller controller, SourceSaver.Listener listener, Path rootPath, Path path, Container.Entry entry) {
        boolean activated = !'false'.equals(api.preferences.get(GrepCodeFileSaverPreferencesProvider.ACTIVATED))

        if (activated && !entry.isDirectory() && entry.path.toLowerCase().endsWith('.jar')) {
            def inputStream = GrepCodeHttpClient.downloadRemoteSourceArchive(entry)

            if (inputStream) {
                def zis = new ZipInputStream(inputStream)

                try {
                    def zipEntry = zis.getNextEntry()

                    while (zipEntry) {
                        def zipEntryPath = path.resolve(zipEntry.name)

                        if (zipEntry.isDirectory()) {
                            Files.createDirectories(zipEntryPath)
                        } else {
                            // Call listener
                            listener.pathSaved(zipEntryPath)

                            Files.createDirectories(zipEntryPath.parent)
                            Files.copy(zis, zipEntryPath, StandardCopyOption.REPLACE_EXISTING)
                        }

                        zipEntry = zis.getNextEntry()
                    }
                } finally {
                    zis.close()
                }

                // Success !
                return
            }
        }

        super.saveContent(api, controller, listener, rootPath, path, entry)
    }
}