package com.kiddoware.kbot.actions;

public interface Executor extends Runnable {

    public void pause();
    public void resume();
    public void release();

    public boolean isComplete();
    public boolean isOngoing();
}