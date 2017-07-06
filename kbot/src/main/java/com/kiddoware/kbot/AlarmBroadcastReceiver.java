package com.kiddoware.kbot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by user on 04/03/2017.
 */

public class AlarmBroadcastReceiver extends BroadcastReceiver {

    public static final String STARTUP_ACTION_NAME = "kiddobotapp";
    private static final String TAG =  "AlarmBroadcastReceiver";
    public AlarmBroadcastReceiver() {
    }

    public static final String ACTION_CUSTOM_ALARM = "kiddobotapp.alarm.action";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Utility.logMsg("AlarmBroadcastReceiver::onReceive",TAG);

        if (intent.getAction().equals(AlarmBroadcastReceiver.ACTION_CUSTOM_ALARM)) {
            String previousAction = intent
                    .getStringExtra(STARTUP_ACTION_NAME);
            Utility.logMsg("AlarmBroadcastReceiver::previousAction ::" + previousAction, TAG);
            if (previousAction == null || previousAction.length() == 0) {
                previousAction = intent.getAction();
            }
            Bootstrap.startAlwaysOnService(context, previousAction);
            Utility.logMsg("AlarmBroadcastReceiver::startAlwaysOnService",TAG);

        }
    }
}
