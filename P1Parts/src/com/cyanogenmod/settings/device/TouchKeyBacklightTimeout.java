package com.cyanogenmod.settings.device;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;

public class TouchKeyBacklightTimeout implements OnPreferenceChangeListener {

    private static final String FILE = "/sys/devices/platform/s3c2440-i2c.2/i2c-2/2-004a/leds_timeout";

    public static boolean isSupported() {
        return Utils.fileExists(FILE);
    }

    /**
     * Restore backlight timeout setting from SharedPreferences. (Write to kernel.)
     * @param context       The context to read the SharedPreferences from
     */
    public static void restore(Context context) {
        if (!isSupported()) {
            return;
        }

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Utils.writeValue(FILE, sharedPrefs.getString(DeviceSettings.KEY_BACKLIGHT_TIMEOUT, "1600"));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Utils.writeValue(FILE, (String) newValue);
        updateSummary((ListPreference) preference, Integer.parseInt(newValue.toString()));
        return true;
    }

    public static void updateSummary(ListPreference preference, int value) {
        final CharSequence[] entries = preference.getEntries();
        final CharSequence[] values = preference.getEntryValues();
        int best = 0;
        for (int i = 0; i < values.length; i++) {
            int summaryValue = Integer.parseInt(values[i].toString());
            if (value >= summaryValue) {
                best = i;
            }
        }
        preference.setSummary(entries[best].toString());
    }

}
