package com.kiddoware.kbot.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shardul on 26/05/17.
 */

public class ActionSection implements Serializable {

    private String title;
    private String executor;
    private List<ActionMessage> messages;

    public ActionSection(String title) {
        this.title = title;
        this.messages = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public List<ActionMessage> getMessages() {
        return messages;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }
}
