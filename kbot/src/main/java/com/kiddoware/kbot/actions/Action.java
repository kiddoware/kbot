package com.kiddoware.kbot.actions;

import android.content.Context;
import android.support.annotation.NonNull;

import com.kiddoware.kbot.R;

public enum Action {

    BROWSER_OPEN("browser.open"),
    APPS_OPEN("apps.open"),
    INPUT_UNKNOWN("input.unknown"),
    UNKNOWN("com.kiddoware.kbot.actions.unkown");

    private String identifier;

    private Action(final String identifier) {
        this.identifier = identifier;
    }

    @NonNull
    public ActionExecutor getExecutor() {
        switch (this) {

            case BROWSER_OPEN:
                return new BrowserOpenActionExec();
            case APPS_OPEN:
                return new OpenAppActionExecutor();
            case INPUT_UNKNOWN:
            case UNKNOWN:
            default:
                return new DefaultActionExecutor();
        }
    }

    public String getTitle(Context context) {
        switch (this) {
            case BROWSER_OPEN:
                return context.getString(R.string.action_browser_title);
            case APPS_OPEN:
                return context.getString(R.string.action_apps_open_title);
            default:
                return "";
        }
    }

    public String getDescription(Context context) {
        switch (this) {
            case BROWSER_OPEN:
                return context.getString(R.string.action_browser_desc);
            case APPS_OPEN:
                return context.getString(R.string.action_apps_open_desc);
            default:
                return "";
        }
    }

    @NonNull
    public static Action getActionFromIdentifier(String identifier) {

        for (Action action : Action.values()) {
            if (action.identifier.equalsIgnoreCase(identifier)) {
                return action;
            }
        }

        return UNKNOWN;
    }

    @Override
    public String toString() {
        return identifier;
    }
}