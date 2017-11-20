package io.prover.provermvp.controller;

import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by babay on 17.11.2017.
 */

public class ListenerList<T> {
    private final List<T> listeners = new ArrayList<>();

    private final Handler handler;
    private final NotificationRunner<T> notificationRunner;


    public ListenerList(Handler handler, NotificationRunner<T> notificationRunner) {
        this.handler = handler;
        this.notificationRunner = notificationRunner;
    }

    public void add(T listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    public void remove(T listener) {
        listeners.remove(listener);
    }

    public void postNotifyEvent() {
        handler.post(this::notifyEvent);
    }

    public void notifyEvent() {
        for (T listener : listeners) {
            notificationRunner.doNotification(listener);
        }
    }

    public interface NotificationRunner<T> {
        void doNotification(T listener);
    }
}
