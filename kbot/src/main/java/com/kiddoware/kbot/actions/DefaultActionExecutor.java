package com.kiddoware.kbot.actions;

/**
 * Created by Shardul on 17/05/17.
 */

public class DefaultActionExecutor extends ActionExecutor {

    @Override
    public void run() {
        controller.speak(getFullFillmentSpeech());
    }
}
