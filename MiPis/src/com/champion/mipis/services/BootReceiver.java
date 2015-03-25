package com.champion.mipis.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

    public static final String TAG = "BootReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {

            Intent intentStartServices = new Intent();
            intentStartServices.setClass(context, ConnectService.class);
            Log.d(TAG, "BootReceiver ACTION_BOOT_COMPLETED..............");
            context.startService(intentStartServices);
        }

    }

}
