package com.allenc.awakedaydream;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.github.danielnilsson9.colorpickerview.dialog.ColorPickerDialogFragment;

import java.util.List;
import java.util.Set;

/**
 * Created by allenc4 on 2/26/2016.
 */
public class Settings extends PreferenceActivity
        implements ColorPickerDialogFragment.ColorPickerDialogListener {

    public static final String KEY_FONT_COLOR = "key_font_color";
    public static final String KEY_APPLICATION_ENABLE = "key_application_enable";
    public static final String KEY_APPLICATION_EXCLUSIONS = "key_application_exclusions";

    public static boolean enabled = true;
    private static Set<String> whitelistedApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Populate the activity with the top-level headers.
     */
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return GeneralSettingsFragment.class.getName().equals(fragmentName) ||
                DetailedSettingsFragment.class.getName().equals(fragmentName);
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
        edit.putInt(KEY_FONT_COLOR, color);
        edit.commit();
    }

    @Override
    public void onDialogDismissed(int dialogId) {
        // Nothing needed here
    }

    @Override
    protected void onPause() {
        super.onPause();
        Daydream.setInForeground(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Daydream.setInForeground(true);
    }

    public static boolean areWhitelistedApps() {
        return !(whitelistedApps == null);
    }

    public static Set<String> getWhitelistedApps() {
        return whitelistedApps;
    }

    /**
     * This fragment shows all general settings from the pref_general.xml
     */
    public static class GeneralSettingsFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener{

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (key) {
                case KEY_APPLICATION_EXCLUSIONS:
                    // List of whitelisted applications changed
                    whitelistedApps = sharedPreferences.getStringSet(key, null);
                    break;
                case KEY_APPLICATION_ENABLE:
                    enabled = sharedPreferences.getBoolean(key, true);
                    if (enabled) {
                        Base.getAppContext().startService(new Intent(Base.getAppContext(), InactivityService.class));
                    } else {
                        Base.getAppContext().stopService(new Intent(Base.getAppContext(), InactivityService.class));
                    }
                    break;
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            // Set up a listener whenever a key changes
            PreferenceManager.getDefaultSharedPreferences(Base.getAppContext())
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            // Unregister the listener whenever a key changes
            PreferenceManager.getDefaultSharedPreferences(Base.getAppContext())
                    .unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    /**
     * This fragment shows all detailed settings outlined in pref_detailed.xml
     */
    public static class DetailedSettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_detailed);
        }

    }

}
