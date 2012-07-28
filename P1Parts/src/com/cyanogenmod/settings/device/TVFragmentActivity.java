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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.TvOut;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.cyanogenmod.settings.device.R;

public class TVFragmentActivity extends PreferenceFragment {

    private static final String PREF_ENABLED = "1";
    private static final String TAG = "P1Parts_TV";

    private CheckBoxPreference mTvOutEnable;
    private CheckBoxPreference mHDMIEnable;
    private ListPreference mTvOutSystem;
    private TvOut mTvOut;
    private C30Observer c30plug;
    private Activity me;

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

        me = getActivity();

        addPreferencesFromResource(R.xml.tv_preferences);

        PreferenceScreen prefSet = getPreferenceScreen();

        mTvOut = new TvOut();
        mTvOutEnable = (CheckBoxPreference) findPreference(DeviceSettings.KEY_TVOUT_ENABLE);
        mTvOutEnable.setChecked(mTvOut._isEnabled());

        mTvOutEnable.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean enable = (Boolean) newValue;
                Intent i = new Intent(getActivity(), TvOutService.class);
                i.putExtra(TvOutService.EXTRA_COMMAND, enable ? TvOutService.COMMAND_ENABLE :
                        TvOutService.COMMAND_DISABLE);
                getActivity().startService(i);
                return true;
            }

        });

        mTvOutSystem = (ListPreference) findPreference(DeviceSettings.KEY_TVOUT_SYSTEM);
        mTvOutSystem.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (mTvOut._isEnabled()) {
                    int newSystem = Integer.valueOf((String) newValue);
                    Intent i = new Intent(getActivity(), TvOutService.class);
                    i.putExtra(TvOutService.EXTRA_COMMAND, TvOutService.COMMAND_CHANGE_SYSTEM);
                    i.putExtra(TvOutService.EXTRA_SYSTEM, newSystem);
                    getActivity().startService(i);
                }
                updateSummary(mTvOutSystem, Integer.parseInt((String) newValue));
                return true;
            }

        });
        updateSummary(mTvOutSystem, Integer.parseInt(mTvOutSystem.getValue()));

        mHDMIEnable = (CheckBoxPreference) findPreference(DeviceSettings.KEY_HDMI_ENABLE);
        mHDMIEnable.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean enable = (Boolean) newValue;
                Intent i = new Intent(getActivity(), TvOutService.class);
                i.putExtra(TvOutService.EXTRA_COMMAND, enable ? TvOutService.COMMAND_HDMI_ENABLE : TvOutService.COMMAND_HDMI_DISABLE);
                getActivity().startService(i);
                return true;
            }

        });

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
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mHeadsetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mHeadsetReceiver);
    }

    private void updateTvOutEnable(boolean connected) {
        mTvOutEnable.setEnabled(connected);
        mTvOutEnable.setSummaryOff(connected ? R.string.tvout_enable_summary :
                R.string.tvout_enable_summary_nocable);

        if (!connected) {
            if (mTvOutEnable.isChecked()) {
                // Disable on unplug (UI)
                mTvOutEnable.setChecked(false);
            }
        } else {
            if (mTvOut._isEnabled()) {
                mTvOutEnable.setChecked(true);
            }
        }
    }

    private void updateHDMIEnable(boolean connected) {
        mHDMIEnable.setEnabled(connected);
        mHDMIEnable.setSummaryOff(connected ? R.string.hdmi_dock_summary : R.string.hdmi_dock_summary_nodock);

        if (!connected) {
            if (mHDMIEnable.isChecked()) {
                // Disable on unplug (UI)
                mHDMIEnable.setChecked(false);
            }
        } else {
            if (mTvOut._isHdmiEnabled()) {
                mHDMIEnable.setChecked(true);
            }
        }
    }

    private void updateSummary(ListPreference preference, int value) {
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

    @Override
    public void onDestroy() {
        mTvOut.finalize();
        super.onDestroy();
    }

    public static void restore(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }
}
