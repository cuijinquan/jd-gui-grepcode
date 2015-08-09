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

class GrepCodeJarFileSourceSaverProvider extends ZipFileSourceSaverProvider {

    void save(API api, SourceSaver.Controller controller, SourceSaver.Listener listener, Path path, Container.Entry entry) {
        boolean activated = !'false'.equals(api.preferences.get(GrepCodeFileSaverPreferencesProvider.ACTIVATED))

        if (activated && !entry.isDirectory() && entry.path.toLowerCase().endsWith('.jar')) {
            def inputStream = GrepCodeHttpClient.downloadRemoteSourceArchive(entry)

            if (inputStream != null) {
                def srcZipParentPath = path.parent

                if (srcZipParentPath && !Files.exists(srcZipParentPath)) {
                    Files.createDirectories(srcZipParentPath)
                }

                inputStream.withCloseable { is ->
                    Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING)
                }

                // Success !
                return
            }
        }

        super.save(api, controller, listener, path, entry)
    }
}