package com.kiddoware.kbot.models;

/**
 * Created by VMac on 17/11/16.
 */

import java.io.Serializable;

public class ActionMessage implements Serializable {

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        Message,
        MediaMessage,
        Options,
        Suggestions
    }

    String id;
    String message;

    String source;
    String executor;

    long time;
    Object[] options;

    Type type = Type.Message;

    public ActionMessage() {
        time = System.currentTimeMillis();
    }

    public ActionMessage(String id, String message, String createdAt) {
        this.id = id;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.type = Type.Message;
        this.message = message;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setOptions(Object[] options) {
        this.options = options;
        this.type = Type.Options;
    }

    public void setSuggestions(Object[] suggestions) {
        this.options = suggestions;
        this.type = Type.Suggestions;
    }

    public Object[] getOptions() {
        return options;
    }

    public Type getType() {
        return type;
    }
}

