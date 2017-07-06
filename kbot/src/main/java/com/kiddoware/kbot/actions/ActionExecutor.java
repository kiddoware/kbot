package com.kiddoware.kbot.actions;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.kiddoware.kbot.models.ActionMessage;
import com.kiddoware.kbot.R;

import java.util.HashMap;

import ai.api.model.AIResponse;

/**
 * Created by Shardul on 06/05/17.
 */

public abstract class ActionExecutor implements Executor {

    protected ActionController controller;

    @NonNull
    protected HashMap<String, String> parameters;

    @NonNull
    protected HashMap<String, String> fullFillment;

    protected AIResponse aiResponse;

    private long identifier;

    public ActionExecutor() {
        parameters = new HashMap<String, String>();
        fullFillment = new HashMap<String, String>();
        identifier = System.currentTimeMillis();
    }


    public void pause() {

    }

    @Override
    public void release() {

    }

    @Override
    public void resume() {

    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public boolean isOngoing() {
        return false;
    }

    public abstract void run();

    public void setController(ActionController controller) {
        this.controller = controller;
    }

    public void setParameters(HashMap<String, String> parameters) {
        if (parameters != null) {
            this.parameters = parameters;
        }
    }

    public ActionController getController() {
        return controller;
    }

    @NonNull
    public HashMap<String, String> getParameters() {
        return parameters;
    }

    @NonNull
    public HashMap<String, String> getFullFillment() {
        return fullFillment;
    }

    public String getFullFillmentSpeech() {
        try {
            return aiResponse.getResult().getFulfillment().getSpeech();
        } catch (NullPointerException e) {
            return controller.getActivity().getResources()
                    .getString(com.kiddoware.kbot.R.string.input_unknown_answer);
        }
    }

    void setAIResponse(AIResponse aiResponse) {
        this.aiResponse = aiResponse;
    }

    public void setFullFillment(HashMap<String, String> fullFillment) {
        if (fullFillment != null) {
            this.fullFillment = fullFillment;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActionExecutor executor = (ActionExecutor) o;

        return identifier == executor.identifier;

    }

    public void handleOption(Object appData) {

    }

    @Override
    public int hashCode() {
        return (int) (identifier ^ (identifier >>> 32));
    }

    protected Activity getActivity() {
        return controller.getActivity();
    }

    protected String getString(int resource) {
        return getActivity().getString(resource);
    }

    protected void notifyControllerForMessage(String msg) {
        ActionMessage actionMessage = new ActionMessage();

        actionMessage.setSource(getString(R.string.app_name));
        actionMessage.setMessage(msg);
        actionMessage.setExecutor(getClass().getSimpleName());
        controller.notifyListener(actionMessage);
    }

    protected boolean showDefaultMessage() {
        return true;
    }


}

