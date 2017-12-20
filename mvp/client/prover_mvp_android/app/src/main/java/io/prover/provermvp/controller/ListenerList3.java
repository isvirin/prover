package io.prover.provermvp.controller;

import android.os.Handler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by babay on 17.11.2017.
 */

public class ListenerList3<T, Q, R, S> {
    private final List<T> listeners = new CopyOnWriteArrayList<>();

    private final Handler handler;
    private final NotificationRunner<T, Q, R, S> notificationRunner;

    public ListenerList3(Handler handler, NotificationRunner<T, Q, R, S> notificationRunner) {
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

    void postNotifyEvent(final Q param1, final R param2, final S param3) {
        handler.post(() -> notifyEvent(param1, param2, param3));
    }

    void notifyEvent(final Q param1, final R param2, final S param3) {
        for (T listener : listeners) {
            notificationRunner.doNotification(listener, param1, param2, param3);
        }
    }

    public interface NotificationRunner<T, Q, R, S> {
        void doNotification(T listener, Q param1, R param2, S param3);
    }
}
