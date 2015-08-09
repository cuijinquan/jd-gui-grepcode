/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui

/**
 * Simple JD-GUI launcher to test the extension.
 */
class AppLauncher extends App {
    static void main(String[] args) {
        def separator = System.getProperty('path.separator')
        def classpath = System.getProperty('java.class.path')
        int servicesIndex = classpath.indexOf('services-')
        int extensionIndex = classpath.indexOf('jd-gui-grepcode' + separator)

        assert servicesIndex < extensionIndex : "Open project settings and place 'jd-gui-grepcode' after 'services' in the class path"

        App.main(args)
    }
}
