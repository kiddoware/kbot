package com.kiddoware.kbot;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Calendar;

import static android.os.Build.VERSION.SDK_INT;

/**
 * Created by user on 04/03/2017.
 */

public class Bootstrap
{

    public static final int ALARM_REPEAT_INTERVAL = 60;//1 minute
    public static final String STARTUP_ACTION_NAME = "kiddobotapp";
    public static final String TAG = "Bootstrap";

    public static synchronized void startAlwaysOnService(Context context,
                                                         String loadedFrom) {
        Utility.logMsg("startListeningMode :: startAlwaysOnService ::" + VoiceRecognitionService.isRunning(),TAG);

        if (!VoiceRecognitionService.isRunning())
        {
            // start service and get new recommendations


            Intent pIntent = new Intent(context, VoiceRecognitionService.class);
            pIntent.putExtra(STARTUP_ACTION_NAME, loadedFrom);
            context.startService(pIntent);

            // enable 1 minute secs restart
            Intent mIntent = new Intent(context, AlarmBroadcastReceiver.class);

            if (  SDK_INT >= 12                  ) {
                mIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            }

            mIntent.putExtra(STARTUP_ACTION_NAME, loadedFrom);
            mIntent.setAction(AlarmBroadcastReceiver.ACTION_CUSTOM_ALARM);
            PendingIntent sender = PendingIntent.getBroadcast(context, 0,
                    mIntent, 0);
            Calendar time = Calendar.getInstance();
            time.setTimeInMillis(System.currentTimeMillis());
            time.add(Calendar.SECOND, 1);

            AlarmManager am = (AlarmManager) context
                    .getSystemService(Context.ALARM_SERVICE);

            am.setRepeating(AlarmManager.RTC, time.getTimeInMillis(),
                    ALARM_REPEAT_INTERVAL * 1000, sender);

            // enable boot/powerkey restart
            setBootupListen(context, true);
        }
        else{
            //start wake word recognizer in case
            // Send an Intent with an action named "notifyVoiceRecognizationService. The Intent
            // sent should
            // be received by the ReceiverActivity.
            Utility.logMsg("startListeningMode :: startAlwaysOnService :: check-recognizer-status" + VoiceRecognitionService.isRunning(),TAG);
            Intent intent = new Intent("notifyVoiceRecognizationService");
            // You can also include some extra data.
            intent.putExtra("message", "check-recognizer-status");
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

    public static synchronized void stopAlwaysOnService(Context context) {

        // stop service
        Intent pIntent = new Intent(context, VoiceRecognitionService.class);
        context.stopService(pIntent);

        // cancel alarm restart
        Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
        intent.setAction(AlarmBroadcastReceiver.ACTION_CUSTOM_ALARM);
        PendingIntent sender = PendingIntent
                .getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);

        // cancel boot/power key restart
        setBootupListen(context, false);
    }


    private static void setBootupListen(Context context, boolean isEnabled)
    {
        int flag = (isEnabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                : PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
        ComponentName component = new ComponentName(context,
                RebootBroadcastReceiver.class);

        context.getPackageManager().setComponentEnabledSetting(component, flag,
                PackageManager.DONT_KILL_APP);
    }


}
