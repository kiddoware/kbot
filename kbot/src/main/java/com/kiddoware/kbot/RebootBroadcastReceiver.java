package com.kiddoware.kbot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by user on 04/03/2017.
 */


public class RebootBroadcastReceiver extends BroadcastReceiver {
    public RebootBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if ((intent.getAction().equals(Intent.ACTION_USER_PRESENT))
                || (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))) {
            Bootstrap.startAlwaysOnService(context, intent.getAction());




        }



    }
}

