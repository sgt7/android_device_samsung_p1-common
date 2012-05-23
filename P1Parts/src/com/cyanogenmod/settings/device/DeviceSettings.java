package com.cyanogenmod.settings.device;

import android.app.Activity;
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
    public static final String KEY_HDMI_ENABLE = "hdmi_enable";
    public static final String KEY_BUTTONS_DISABLE = "buttons_disable";
    public static final String KEY_BUTTONS = "buttons_category";
    public static final String KEY_BACKLIGHT_TIMEOUT = "backlight_timeout";

    public static final String COMMAND_SHELL = "/system/bin/sh";
    public static final String ECHO_COMMAND = "echo ";
    public static final String BUTTONS_ENABLED_PATH =
            "/sys/devices/platform/s3c2440-i2c.2/i2c-2/2-004a/buttons_enabled";
    public static final String BUTTONS_ENABLED_COMMAND =
            " > /sys/devices/platform/s3c2440-i2c.2/i2c-2/2-004a/buttons_enabled";

    private ListPreference mHspa;
    private CheckBoxPreference mTvOutEnable;
    private CheckBoxPreference mHDMIEnable;
    private ListPreference mTvOutSystem;
    private TvOut mTvOut;
    private C30Observer	c30plug;
    private Activity	me;
    private CheckBoxPreference mDisableButtons;
    private ListPreference mBacklightTimeout;

    private boolean	mTVoutConnected = false;
    private boolean mHDMIConnected = false;

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
        
        me = this;
        
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

        mHDMIEnable = (CheckBoxPreference) findPreference(KEY_HDMI_ENABLE);
        mHDMIEnable.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {       	
                boolean enable = (Boolean) newValue;
                Intent i = new Intent(DeviceSettings.this, TvOutService.class);
                i.putExtra(TvOutService.EXTRA_COMMAND, enable ? TvOutService.COMMAND_HDMI_ENABLE : TvOutService.COMMAND_HDMI_DISABLE);
                startService(i);
                return true;
            }

        });

        mDisableButtons = (CheckBoxPreference) findPreference(KEY_BUTTONS_DISABLE);
        File file = new File(BUTTONS_ENABLED_PATH);
        if (!file.exists()) {
            prefSet.removePreference(findPreference(KEY_BUTTONS));
        }

        mBacklightTimeout = (ListPreference) findPreference(KEY_BACKLIGHT_TIMEOUT);
        mBacklightTimeout.setEnabled(TouchKeyBacklightTimeout.isSupported());
        mBacklightTimeout.setOnPreferenceChangeListener(new TouchKeyBacklightTimeout());

        c30plug = new C30Observer();
        
        mTVoutConnected = c30plug.isTVoutConnected();
        mHDMIConnected = c30plug.isDockDeskConnected();
        
        updateTvOutEnable(mTVoutConnected);
        updateHDMIEnable(mHDMIConnected);
        
        c30plug.setOnStateChangeListener(new C30StateListener() {
			
			@Override
			public boolean onStateChange(Object dev, Object state) {
				final boolean connected = "online".equals(state);
				final String  device = (String)dev;
				
				// Need to post message to itself here
				me.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if ("TV".equals(device))
							updateTvOutEnable(connected);
						else if ("desk".equals(device))
							updateHDMIEnable(connected);
					}
				});

				return true;
			}
		});
        
        c30plug.start();
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

        if (!connected)
        {
        	if (mTvOutEnable.isChecked())
        		// Disable on unplug (UI)
        		mTvOutEnable.setChecked(false);
        }
        else
        	if (mTvOut._isEnabled())
        		mTvOutEnable.setChecked(true);
    }

    private void updateHDMIEnable(boolean connected) {
        mHDMIEnable.setEnabled(connected);
        mHDMIEnable.setSummaryOff(connected ? R.string.hdmi_dock_summary : R.string.hdmi_dock_summary_nodock);

        if (!connected)
        {
        	if (mHDMIEnable.isChecked())
        		// Disable on unplug (UI)
        		mHDMIEnable.setChecked(false);
        }
        else
        	if (mTvOut._isHdmiEnabled())
        		mHDMIEnable.setChecked(true);
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
