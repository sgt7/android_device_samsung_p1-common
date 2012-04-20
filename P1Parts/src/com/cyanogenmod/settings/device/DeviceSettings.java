package com.cyanogenmod.settings.device;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.TvOut;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

import java.io.File;
import java.io.IOException;

public class DeviceSettings extends PreferenceActivity  {

    public static final String KEY_HSPA = "hspa";
    public static final String KEY_TVOUT_ENABLE = "tvout_enable";
    public static final String KEY_TVOUT_SYSTEM = "tvout_system";
    public static final String KEY_BUTTONS_DISABLE = "buttons_disable";
    public static final String KEY_BUTTONS = "buttons_category";

    public static final String COMMAND_SHELL = "/system/bin/sh";
    public static final String ECHO_COMMAND = "echo ";
    public static final String BUTTONS_ENABLED_PATH =
            "/sys/devices/platform/s3c2440-i2c.2/i2c-2/2-004a/buttons_enabled";
    public static final String BUTTONS_ENABLED_COMMAND =
            " > /sys/devices/platform/s3c2440-i2c.2/i2c-2/2-004a/buttons_enabled";

    private ListPreference mHspa;
    private CheckBoxPreference mTvOutEnable;
    private ListPreference mTvOutSystem;
    private TvOut mTvOut;
    private CheckBoxPreference mDisableButtons;

    private BroadcastReceiver mHeadsetReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra("state", 0);
            updateTvOutEnable(state != 0);
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.main);

        PreferenceScreen prefSet = getPreferenceScreen();

        mHspa = (ListPreference) findPreference(KEY_HSPA);
        mHspa.setEnabled(Hspa.isSupported());
        mHspa.setOnPreferenceChangeListener(new Hspa(this));

        mTvOut = new TvOut();
        mTvOutEnable = (CheckBoxPreference) findPreference(KEY_TVOUT_ENABLE);
        mTvOutEnable.setChecked(mTvOut._isEnabled());

        mTvOutEnable.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean enable = (Boolean) newValue;
                Intent i = new Intent(DeviceSettings.this, TvOutService.class);
                i.putExtra(TvOutService.EXTRA_COMMAND, enable ? TvOutService.COMMAND_ENABLE :
                        TvOutService.COMMAND_DISABLE);
                startService(i);
                return true;
            }

        });

        mTvOutSystem = (ListPreference) findPreference(KEY_TVOUT_SYSTEM);
        mTvOutSystem.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (mTvOut._isEnabled()) {
                    int newSystem = Integer.valueOf((String) newValue);
                    Intent i = new Intent(DeviceSettings.this, TvOutService.class);
                    i.putExtra(TvOutService.EXTRA_COMMAND, TvOutService.COMMAND_CHANGE_SYSTEM);
                    i.putExtra(TvOutService.EXTRA_SYSTEM, newSystem);
                    startService(i);
                }
                return true;
            }

        });

        mDisableButtons = (CheckBoxPreference) findPreference(KEY_BUTTONS_DISABLE);
        File file = new File(BUTTONS_ENABLED_PATH);
        if (!file.exists()) {
            prefSet.removePreference(findPreference(KEY_BUTTONS));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mHeadsetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mHeadsetReceiver);
    }

    private void updateTvOutEnable(boolean connected) {
        mTvOutEnable.setEnabled(connected);
        mTvOutEnable.setSummaryOff(connected ? R.string.tvout_enable_summary :
                R.string.tvout_enable_summary_nocable);

        if (!connected && mTvOutEnable.isChecked()) {
            // Disable on unplug (UI)
            mTvOutEnable.setChecked(false);
        }
    }

    @Override
    protected void onDestroy() {
        mTvOut.finalize();
        super.onDestroy();
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;
        if (preference == mDisableButtons) {
            value = mDisableButtons.isChecked();
            try {
                String[] cmds = {COMMAND_SHELL, "-c",
                        ECHO_COMMAND + (value ? "0" : "1") +
                        BUTTONS_ENABLED_COMMAND};
                Runtime.getRuntime().exec(cmds);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

}
