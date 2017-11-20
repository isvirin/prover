package io.prover.provermvp.controller;

import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by babay on 17.11.2017.
 */

public class ListenerList1<T, Q> {
    private final List<T> listeners = new ArrayList<>();

    private final Handler handler;
    private final NotificationRunner<T, Q> notificationRunner;

    public ListenerList1(Handler handler, NotificationRunner<T, Q> notificationRunner) {
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

    public void postNotifyEvent(final Q param1) {
        handler.post(() -> notifyEvent(param1));
    }

    public void notifyEvent(final Q param1) {
        for (T listener : listeners) {
            notificationRunner.doNotification(listener, param1);
        }
    }

    public interface NotificationRunner<T, Q> {
        void doNotification(T listener, Q param1);
    }
}
