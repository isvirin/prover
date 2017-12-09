package io.prover.provermvp.controller;

import android.os.Handler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by babay on 17.11.2017.
 */

public class ListenerList1<T, Q> {
    private final List<T> listeners = new CopyOnWriteArrayList<>();

    private final Handler handler;
    private final NotificationRunner<T, Q> notificationRunner;

    public ListenerList1(Handler handler, NotificationRunner<T, Q> notificationRunner) {
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

    public void postNotifyEvent(final Q param1) {
        handler.post(() -> notifyEvent(param1));
    }

    void notifyEvent(final Q param1) {
        for (T listener : listeners) {
            notificationRunner.doNotification(listener, param1);
        }
    }

    public interface NotificationRunner<T, Q> {
        void doNotification(T listener, Q param1);
    }
}
