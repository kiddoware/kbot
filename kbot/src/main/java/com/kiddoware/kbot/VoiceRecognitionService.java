package com.kiddoware.kbot;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;


import java.io.File;
import java.io.IOException;
import java.util.List;

import ai.api.model.AIOutputContext;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

import static android.widget.Toast.makeText;


/**
 * Created by user on 04/03/2017.
 */

public class VoiceRecognitionService extends Service  implements
        RecognitionListener
{


    private static boolean isRunning = false;
    // This is the object that receives interactions from clients.  See
    private final IBinder mBinder = new VoiceRecognitionService.LocalBinder();
    public SpeechRecognizer recognizer;


    //current output context
    private static List<AIOutputContext> glist_AIOutputContext = null;
    /* Named searches allow to quickly reconfigure the decoder */
    private static final String KWS_SEARCH = "wakeup";
    private static final String TAG = "VoiceRecognitionService";
    /* Keyword we are looking for to activate menu */
    public static final String KEYPHRASE = "hello buddy";
    public static final String NOTIFY_VOICE_RECOGNIZATION_SERVICE = "notifyVoiceRecognizationService";
    public static final String START_RECOGNIZATION_SERVICE = "start-recognizer";
    public static final String STOP_VOICE_RECOGNIZATION_SERVICE = "stop-recognizer";

    public VoiceRecognitionService() {


    }



    public class LocalBinder extends Binder {
        VoiceRecognitionService getService() {
            return VoiceRecognitionService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Utility.logMsg("onStartCommand :: isRunning ::" + isRunning(),TAG);

        if (!isRunning()) {
            setIsRunning(true);
            try {
                startInForeground();
                Toast.makeText(this, "KiddoBot is listening" , Toast.LENGTH_LONG).show();

                runRecognizerSetup();
                // Register to receive messages.
                // We are registering an observer (mMessageReceiver) to receive Intents
                // with actions named "custom-event-name".
                LocalBroadcastManager.getInstance(this).registerReceiver(
                        mMessageReceiver, new IntentFilter(NOTIFY_VOICE_RECOGNIZATION_SERVICE));

            } catch (Exception ex) {
                Toast.makeText(this, ex.getMessage() , Toast.LENGTH_LONG).show();


            }


        }
        return START_NOT_STICKY;
    }





    @Override
    public void onDestroy()
    {
        Utility.logMsg("onDestroy :: isRunning ::" + isRunning(),TAG);
        // stop running
        setIsRunning(false);
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
                mMessageReceiver);
        super.onDestroy();

    }
    private void startInForeground() {
        try {
            Intent i = new Intent(this, MainActivity2.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    + Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

            Notification.Builder builder = new Notification.Builder(this);

            builder.setContentIntent(pi)
                    .setOngoing(true)
                    .setAutoCancel(false)
					.setSmallIcon(R.drawable.ic_notification)
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle(getResources().getString(com.kiddoware.kbot.R.string.app_name));
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ) {
                startForeground(1337, builder.build());
            }
        } catch (Exception ex) {
        }

    }

    public static boolean isRunning(){
        return  isRunning;
    }

    public static void setIsRunning(boolean isRunning){
        VoiceRecognitionService.isRunning = isRunning;
    }
    private void runRecognizerSetup() {
        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(getApplicationContext());
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (Exception e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    // ((TextView) findViewById(R.id.caption_text)).setText("Failed to init recognizer " + result);
                } else {
                    switchSearch(KWS_SEARCH);
                }
            }
        }.execute();
    }

    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        try {
            if (hypothesis == null)
                return;

            String text = hypothesis.getHypstr();
            Utility.logMsg("startListeningMode :: Wake Word ::" + text,TAG);
            if (text.equals(KEYPHRASE)) {
                stopWakeWordRecognizer();//stop wake word recognizer since wake word is detected
                final Intent intent = new Intent(this, MainActivity2.class);
                intent.setPackage(getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(MainActivity2.START_LISNENING_MODE, true);
                startActivity(intent);
                //switchSearch(MENU_SEARCH);
            }
        }
        catch(Exception ex){
            Log.e(TAG,"onPartialResult",ex);
            //exception occured so start wake word recognizer
            startWakeWordRecognizer(KWS_SEARCH);
        }

    }

    /**
     * This callback is called when we stop the recognizer.
     */
    @Override
    public void onResult(Hypothesis hypothesis) {
        //((TextView) findViewById(R.id.result_text)).setText("");
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    /**
     * We stop recognizer here to get a final result
     */
    @Override
    public void onEndOfSpeech() {
        if (recognizer != null && !recognizer.getSearchName().equals(KWS_SEARCH))
            switchSearch(KWS_SEARCH);
    }

    private void stopWakeWordRecognizer() {
        if(recognizer != null) {
            recognizer.stop();
        }

    }
    private void startWakeWordRecognizer(String searchName) {
        if (recognizer != null){
            switchSearch(KWS_SEARCH);
        }
    }
    private String getRecognizerStatus(){
        String status = null;
        if(recognizer != null)
            status = recognizer.getSearchName();
        return status;
    }
    private void switchSearch(String searchName) {
        recognizer.stop();

        // If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
        if (searchName.equals(KWS_SEARCH))
            recognizer.startListening(searchName);
        else
            recognizer.startListening(searchName, 10000);

        // String caption = getResources().getString(captions.get(searchName));
        //((TextView) findViewById(R.id.caption_text)).setText(caption);
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them
        //see this link for fine tuning key word recognizer
        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                .setKeywordThreshold((float) 1e-30)//   Try keyword threshold values as 1e-60, 1e-40, 1e-20, 1e-10 (highest to lowest sensitive)
                //.setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)

                .getRecognizer();
        recognizer.addListener(this);

        /** In your application you might not need to add all those searches.
         * They are added here for demonstration. You can leave just one.
         */

        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);

    }

    @Override
    public void onError(Exception error) {
        //((TextView) findViewById(R.id.caption_text)).setText(error.getMessage());
    }

    @Override
    public void onTimeout() {
        switchSearch(KWS_SEARCH);
    }


    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "notifyVoiceRecognizationService" is broadcasted.
    //called by AIDialog class.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            Utility.logMsg("VoiceRecognitionService:: Got message: " + message + " Reccognizer Status :: +" + getRecognizerStatus() ,TAG);

            if(message.equals("start-recognizer")){
                VoiceRecognitionService.this.startWakeWordRecognizer(KWS_SEARCH);
            }
            else if(message.equals("stop-recognizer")){
                VoiceRecognitionService.this.stopWakeWordRecognizer();
            }
            else if(message.equals("check-recognizer-status")){
                if(isRunning()){
                    if((recognizer != null && !recognizer.getSearchName().equals(KWS_SEARCH))){
                        Utility.logMsg("VoiceRecognitionService::startWakeWordRecognizer :: searchName :: "+ recognizer.getSearchName() ,TAG);
                        runRecognizerSetup();
                    }
                    else if(recognizer == null){
                        Utility.logMsg("VoiceRecognitionService::startWakeWordRecognizer :: null :: initializing again ", TAG);
                        runRecognizerSetup();
                    }


                }
            }
        }
    };
}
