package com.cyanogenmod.settings.device;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;

public class ToggleCapacitiveKeys implements OnPreferenceChangeListener {

    private static final String FILE = "/sys/devices/platform/s3c2440-i2c.2/i2c-2/2-004a/buttons_enabled";

    public static boolean isSupported() {
        return Utils.fileExists(FILE);
    }

    public static void restore(Context context) {
        if (!isSupported()) {
            return;
        }

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Utils.writeValue(FILE, (sharedPrefs.getBoolean(DeviceSettings.KEY_BUTTONS_DISABLE, false) ? "0" : "1"));
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Utils.writeValue(FILE, ((CheckBoxPreference)preference).isChecked() ? "1" : "0");
        return true;
    }

}
