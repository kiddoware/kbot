package com.kiddoware.kbot.actions;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.widget.MediaController;

import com.google.gson.JsonElement;
import com.kiddoware.kbot.models.ActionMessage;
import com.kiddoware.kbot.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ai.api.model.AIOutputContext;
import ai.api.model.AIResponse;
import ai.api.model.Result;

/**
 * Created by Shardul on 08/05/17.
 */

public class ActionController implements MediaController.MediaPlayerControl {

    private Activity activity;
    private MediaPlayer mediaPlayer;
    private TextToSpeech textToSpeech;

    private ArrayList<ActionExecutor> runningActions;
    private ArrayList<ActionExecutor> actionQueue;

    private ActionExecutor currentActionExecutor;

    private ActionControllerListener listener;
    private ActionMessage previousActionMessage;

    private List<AIOutputContext> resultContext = null;

    public ActionController(Activity activity) {
        this.activity = activity;
        this.mediaPlayer = new MediaPlayer();
        this.runningActions = new ArrayList<>();
        this.actionQueue = new ArrayList<>();
    }

    public void enqueueAction(AIResponse response) {
        Result result = response.getResult();

        if (result != null) {

            if (resultContext != null)
                resultContext.clear(); // TODO Review Paresh: Should we really be doing this ?

            resultContext = result.getContexts();

            ActionMessage actionMessage = new ActionMessage();

            actionMessage.setSource(result.getSource());
            actionMessage.setExecutor(activity.getString(R.string.app_name));

            if (result.getFulfillment() != null && result.getFulfillment().getSpeech() != null) {
                actionMessage.setMessage(result.getFulfillment().getSpeech());
            }


            if (result.isActionIncomplete()) {

                String speech = actionMessage.getMessage() != null ? actionMessage.getMessage() : activity.
                        getString(R.string.input_unknown_answer);

                actionMessage.setMessage(speech);

                speak(speech);

                notifyListener(actionMessage);
                return;
            }


            Action action = Action.getActionFromIdentifier(result.getAction());

            ActionExecutor executor = action.getExecutor();

            actionMessage.setExecutor(executor.getClass().getSimpleName());

            if (result.getParameters() != null && !result.getParameters().isEmpty()) {

                HashMap<String, String> parameters = new HashMap<String, String>();


                for (final Map.Entry<String, JsonElement> entry :
                        result.getParameters().entrySet()) {
                    try {
                        parameters.put(entry.getKey(), entry.getValue().getAsString());
                    } catch (Exception e) {
                        // ignoring getValueAsString errors
                    }
                }

                executor.setParameters(parameters);

            }

            if (result.getFulfillment().getData() != null
                    && !result.getFulfillment().getData().isEmpty()) {

                HashMap<String, String> fullFillment = new HashMap<String, String>();

                for (final Map.Entry<String, JsonElement> entry :
                        result.getFulfillment().getData().entrySet()) {
                    try {
                        fullFillment.put(entry.getKey(), entry.getValue().getAsString());
                    } catch (Exception e) {
                        // ignoring getValueAsString errors
                    }
                }

                executor.setFullFillment(fullFillment);
            }

            executor.setAIResponse(response);
            executor.setController(this);

            executor.run();

            if (actionMessage.getMessage() == null || actionMessage.getMessage().length() == 0) {
                actionMessage.setMessage(activity.getString(android.R.string.ok));
            }

            if (executor.showDefaultMessage()) {
                notifyListener(actionMessage);
            }

            this.currentActionExecutor = executor;
        }
    }


    void speak(String message) {
        if (textToSpeech != null) {

            if (Build.VERSION.SDK_INT >= 21) {
                textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null,
                        String.valueOf(System.currentTimeMillis()));
            } else {
                textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null);
            }

        } else {
            textToSpeech = new TextToSpeech(activity, new TextToSpeechInitListener(message));
        }

    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public TextToSpeech getTextToSpeech() {
        return textToSpeech;
    }

    public void onActionComplete(ActionExecutor actionExecutor) {
        this.currentActionExecutor = null;
        // move to next action
    }

    public void pauseOngoingAction() {
        if (currentActionExecutor != null) {
            currentActionExecutor.pause();
        }
    }

    public void resumeOngoingAction() {
        if (currentActionExecutor != null) {
            currentActionExecutor.resume();
        }
    }

    public void releasePendingActions() {
        if (currentActionExecutor != null) {
            currentActionExecutor.release();
        }
    }

    @Override
    public void start() {

    }

    public void pause() {
        if (textToSpeech != null && textToSpeech.isSpeaking()) {
            textToSpeech.stop();
        }

        pauseOngoingAction();
    }

    @Override
    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {

    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    public void resume() {
        resumeOngoingAction();
    }

    public void release() {
        releasePendingActions();

        mediaPlayer.stop();
        mediaPlayer.release();

        if (textToSpeech != null) {
            textToSpeech.shutdown();
            textToSpeech = null;
        }
    }

    public Activity getActivity() {
        return activity;
    }

    public void setListener(ActionControllerListener listener) {
        this.listener = listener;
    }


    private class TextToSpeechInitListener implements TextToSpeech.OnInitListener {

        private String textToSpeakOnInit = null;

        public TextToSpeechInitListener(String textToSpeakOnInit) {
            this.textToSpeakOnInit = textToSpeakOnInit;
        }

        @Override
        public void onInit(int status) {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.getDefault());

                if (textToSpeakOnInit != null) {
                    speak(textToSpeakOnInit);
                    textToSpeakOnInit = null;
                }

            } else {
                textToSpeech = null;
            }
        }
    }

    ActionMessage getPreviousActionMessage() {
        return previousActionMessage;
    }

    void notifyListener(ActionMessage actionMessage) {
        this.previousActionMessage = actionMessage;
        if (listener != null) {
            listener.onActionMessage(this, actionMessage);
        }
    }

    void notifyListener(MediaPlayer mediaPlayer) {
        if (listener != null) {
            listener.onMediaPlayerPrepared(this, mediaPlayer);
        }
    }

    public static interface ActionControllerListener {
        public void onActionMessage(ActionController controller, ActionMessage actionMessage);
        public void onMediaPlayerPrepared(ActionController controller, MediaPlayer mediaPlayer);
    }
}
