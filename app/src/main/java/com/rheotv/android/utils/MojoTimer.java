package com.rheotv.android.utils;

import java.util.Timer;
import java.util.TimerTask;

public class MojoTimer extends Timer {
    private boolean isTaskExecuting = false;

    public MojoTimer() {
        super();
    }

    @Override
    public void schedule(TimerTask task, long delay, long period) {
        super.schedule(task, delay, period);
        isTaskExecuting = true;
    }

    public boolean isTaskExecuting() {
        return isTaskExecuting;
    }

    @Override
    public void cancel() {
        super.cancel();
        isTaskExecuting=false;
    }
}
