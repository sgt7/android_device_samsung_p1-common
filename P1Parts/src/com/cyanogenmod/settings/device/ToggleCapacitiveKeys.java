/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
