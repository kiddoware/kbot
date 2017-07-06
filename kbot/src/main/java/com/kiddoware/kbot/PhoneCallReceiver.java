package com.kiddoware.kbot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by Shardul on 30/05/17.
 */

public class PhoneCallReceiver extends BroadcastReceiver {
    private static final String TAG = "PhoneCallReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Utility.logMsg("PhoneCallReceiver::", TAG);
        final String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

        if(state != null && state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING))
        {
            Utility.logMsg("PhoneCallReceiver::inbound", TAG);

            //stop recognizer on inbound
            stopWakeWordRecognizer(context);

        }
        else if (intent. getAction (). equals (Intent. ACTION_NEW_OUTGOING_CALL)) {
            Utility.logMsg("PhoneCallReceiver::outbound", TAG);
            //stop recognizer on outbound
            stopWakeWordRecognizer(context);

        }
        else if (state != null && state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
            Utility.logMsg("PhoneCallReceiver::outbound :: OFFHOOK", TAG);
            //stop recognizer on outbound
            stopWakeWordRecognizer(context);

        }
        else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(
                TelephonyManager.EXTRA_STATE_IDLE))
        {
            Utility.logMsg("PhoneCallReceiver::idle", TAG);

            //Start  Recognizer on idle
            startWakeWordRecognizer(context);

        }

    }
    // Send an Intent with an action named "notifyVoiceRecognizationService. The Intent
    // sent should
    // be received by the ReceiverActivity.
    private void startWakeWordRecognizer(Context context) {
        Log.d("sender", "Broadcasting message");
        Intent intent = new Intent("notifyVoiceRecognizationService");
        // You can also include some extra data.
        intent.putExtra("message", "start-recognizer");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
    // Send an Intent with an action named "custom-event-name". The Intent
    // sent should
    // be received by the ReceiverActivity.
    private void stopWakeWordRecognizer(Context context) {
        Log.d("sender", "Broadcasting message");
        Intent intent = new Intent("notifyVoiceRecognizationService");
        // You can also include some extra data.
        intent.putExtra("message", "stop-recognizer");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
