package io.prover.provermvp.detector;

import android.media.Image;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.concurrent.atomic.AtomicInteger;

import io.prover.provermvp.controller.CameraController;

import static io.prover.provermvp.Const.TAG;

/**
 * Created by babay on 11.11.2017.
 */

public class SwypeDetectorHandler extends Handler {
    private static final int MAX_FRAMES_IN_QUEUE = 1;

    private static final int MESSAGE_INIT = 1;
    private static final int MESSAGE_SET_SWYPE = 2;
    private static final int MESSAGE_PROCESS_FRAME = 3;
    private static final int MESSAGE_PROCESS_FRAME2 = 4;
    private static final int MESSAGE_QUIT = 5;
    private static volatile int counter = 0;
    private final HandlerThread handlerThread;

    private final ProverDetector detector;
    private final AtomicInteger framesInQueue = new AtomicInteger();
    private final CameraController cameraController;
    private volatile boolean quitRequested = false;

    public SwypeDetectorHandler(Looper looper, CameraController cameraController) {
        super(looper);
        handlerThread = (HandlerThread) looper.getThread();
        detector = new ProverDetector(cameraController);
        this.cameraController = cameraController;
    }

    public static SwypeDetectorHandler newHandler(int fps, String swype, CameraController cameraController) {
        HandlerThread handlerThread = new HandlerThread("SwypeDetectorThread_" + counter++);
        handlerThread.start();
        SwypeDetectorHandler handler = new SwypeDetectorHandler(handlerThread.getLooper(), cameraController);
        handler.sendInit(fps, swype);
        return handler;
    }

    public void sendInit(int fps, String swype) {
        sendMessage(obtainMessage(MESSAGE_INIT, fps, 0, swype));
    }

    public void sendSetSwype(String swype) {
        sendMessage(obtainMessage(MESSAGE_SET_SWYPE, swype));
    }

    public boolean sendProcesstFrame(byte[] frame, int width, int height) {
        int inQueue = framesInQueue.get();
        if (inQueue < MAX_FRAMES_IN_QUEUE) {
            framesInQueue.incrementAndGet();
            sendMessage(obtainMessage(MESSAGE_PROCESS_FRAME, width, height, frame));
            return true;
        } else {
            Log.e(TAG, "detectorframes processing queue maxed out!");
            return false;
        }
    }


    public boolean sendProcesstFrame(Image image) {
        //return false;
        int inQueue = framesInQueue.get();
        if (inQueue < MAX_FRAMES_IN_QUEUE) {
            framesInQueue.incrementAndGet();
            sendMessage(obtainMessage(MESSAGE_PROCESS_FRAME2, image));
            return true;
        } else {
            Log.e(TAG, "frames processing queue maxed out!");
            return false;
        }
    }

    public void sendQuit() {
        quitRequested = true;
        sendMessage(obtainMessage(MESSAGE_QUIT));
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_INIT:
                if (!quitRequested)
                    detector.init(msg.arg1, (String) msg.obj);
                return;

            case MESSAGE_SET_SWYPE:
                if (!quitRequested)
                    detector.setSwype((String) msg.obj);
                return;

            case MESSAGE_PROCESS_FRAME:
                if (!quitRequested)
                    detector.detectFrame((byte[]) msg.obj, msg.arg1, msg.arg2);
                framesInQueue.decrementAndGet();
                return;

            case MESSAGE_PROCESS_FRAME2:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Image image = (Image) msg.obj;
                    if (quitRequested) {
                        image.close();
                    } else {
                        detector.detectFrame(image);
                        image.close();
                    }
                }
                framesInQueue.decrementAndGet();
                break;


            case MESSAGE_QUIT:
                detector.release();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    handlerThread.quitSafely();
                } else {
                    handlerThread.quit();
                }
                return;
        }

        super.handleMessage(msg);
    }
}
