package io.prover.provermvp.controller;

import android.os.Handler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by babay on 17.11.2017.
 */

public class ListenerList<T> {
    private final List<T> listeners = new CopyOnWriteArrayList<>();

    private final Handler handler;
    private final NotificationRunner<T> notificationRunner;


    public ListenerList(Handler handler, NotificationRunner<T> notificationRunner) {
        this.handler = handler;
        this.notificationRunner = notificationRunner;
    }

    public synchronized void add(T listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    public synchronized void remove(T listener) {
        listeners.remove(listener);
    }

    void postNotifyEvent() {
        handler.post(this::notifyEvent);
    }

    void notifyEvent() {
        for (T listener : listeners) {
            notificationRunner.doNotification(listener);
        }
    }

    public interface NotificationRunner<T> {
        void doNotification(T listener);
    }
}
