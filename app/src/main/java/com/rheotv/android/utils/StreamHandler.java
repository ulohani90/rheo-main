package com.rheotv.android.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.LinkedList;
import java.util.List;

public abstract class StreamHandler<T> {
    protected Runnable eventRunner = this::publish;
    protected Handler eventHandler = new Handler(Looper.getMainLooper());
    protected boolean isPublishing;
    protected LinkedList<T> queue = new LinkedList<>();

    public void add(T item) {
        queue.add(item);
        if (!isPublishing) {
            isPublishing = true;
            eventHandler.post(eventRunner);
        }
    }

    public void add(T item, boolean isFirst) {
        if (isFirst) {
            queue.addFirst(item);
        } else {
            queue.addLast(item);
        }

        if (!isPublishing) {
            isPublishing = true;
            eventHandler.post(eventRunner);
        }
    }

    public void addList(List<T> item) {
        queue.addAll(item);
        if (!isPublishing) {
            isPublishing = true;
            eventHandler.post(eventRunner);
        }
    }

    public void remove(T item) {
        queue.remove(item);
    }

    public abstract void publish();

    public boolean isQueueEmpty() {
        if (queue.peek() == null) {
            isPublishing = false;
            return true;
        }
        return false;
    }

    public void removeCallbacks() {
        eventHandler.removeCallbacks(eventRunner);
    }
}
