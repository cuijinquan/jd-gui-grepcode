/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.service.preferencespanel

import groovy.transform.CompileStatic
import org.jd.gui.spi.PreferencesPanel

import javax.swing.*
import java.awt.*

@CompileStatic
class GrepCodeFileSaverPreferencesProvider extends JPanel implements PreferencesPanel {

    static final String ACTIVATED = 'GrepCodeFileSaverPreferences.activated'

    JCheckBox searchOnGrepCodeCheckBox

    GrepCodeFileSaverPreferencesProvider() {
        super(new GridLayout(0,1))
        add(searchOnGrepCodeCheckBox = new JCheckBox('Search source code on grepcode.com'))
    }

    // --- PreferencesPanel --- //
    @Override String getPreferencesGroupTitle() { 'Source Saver' }
    @Override String getPreferencesPanelTitle() { 'GrepCode' }

    @Override public void init(Color errorBackgroundColor) {}

    @Override public boolean isActivated() { true }

    @Override
    void loadPreferences(Map<String, String> preferences) {
        searchOnGrepCodeCheckBox.selected = !'false'.equals(preferences.get(ACTIVATED))
    }

    @Override
    void savePreferences(Map<String, String> preferences) {
        preferences.put(ACTIVATED, Boolean.toString(searchOnGrepCodeCheckBox.selected))
    }

    @Override boolean arePreferencesValid() { true }

    @Override void addPreferencesChangeListener(PreferencesPanel.PreferencesPanelChangeListener listener) {}
}
