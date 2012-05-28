package com.cyanogenmod.settings.device;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.IOException;

import com.cyanogenmod.settings.device.DeviceSettings;

public class Startup extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent bootintent) {
        Hspa.restore(context);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        ToggleCapacitiveKeys.restore(context);
        TouchKeyBacklightTimeout.restore(context);
    }

}
