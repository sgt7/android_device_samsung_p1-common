package com.cyanogenmod.settings.device;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;

public class Hspa implements OnPreferenceChangeListener {

    private static final String APK_FILE = "/system/app/SamsungServiceMode.apk";
    private Context mCtx;

    public Hspa(Context context) {
        mCtx = context;
    }

    public static boolean isSupported() {
        return Utils.fileExists(APK_FILE);
    }

    /**
     * Restore HSPA setting from SharedPreferences. (Write to kernel.)
     * @param context       The context to read the SharedPreferences from
     */
    public static void restore(Context context) {
        if (!isSupported()) {
            return;
        }

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        sendIntent(context, sharedPrefs.getString(DeviceSettings.KEY_HSPA, "23"));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        sendIntent(mCtx, (String) newValue);
        updateSummary((ListPreference) preference, Integer.parseInt(newValue.toString()));
        return true;
    }

    private static void sendIntent(Context context, String value) {
        Intent i = new Intent("com.cyanogenmod.SamsungServiceMode.EXECUTE");
        i.putExtra("sub_type", 20); // HSPA Setting
        i.putExtra("data", value);
        context.sendBroadcast(i);
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
