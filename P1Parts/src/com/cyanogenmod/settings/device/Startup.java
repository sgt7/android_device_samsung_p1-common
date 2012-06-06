package com.cyanogenmod.settings.device;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Startup extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent bootintent) {
        Hspa.restore(context);
        ToggleCapacitiveKeys.restore(context);
        TouchKeyBacklightTimeout.restore(context);
        GpuOverclock.restore(context);
        WifiPowerManagement.restore(context);
        FastCharge.restore(context);
        LiveOverClock.restore(context);
    }

}
