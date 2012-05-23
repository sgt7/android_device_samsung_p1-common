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

        if (prefs.getBoolean(DeviceSettings.KEY_BUTTONS_DISABLE, false)) {
            configureButtons();
        }
        TouchKeyBacklightTimeout.restore(context);
    }

    private void configureButtons() {
        try {
            String[] cmds = {DeviceSettings.COMMAND_SHELL, "-c",
                    DeviceSettings.ECHO_COMMAND + "0" + DeviceSettings.BUTTONS_ENABLED_COMMAND};
            Runtime.getRuntime().exec(cmds);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
