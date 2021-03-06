package com.cyanogenmod.settings.device;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Startup extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent bootintent) {
        ToggleCapacitiveKeys.restore(context);
        TouchKeyBacklightTimeout.restore(context);
        GpuOverclock.restore(context);
        TouchscreenClock.restore(context);
    }
}
