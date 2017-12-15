package io.prover.provermvp.detector;

import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import io.prover.provermvp.camera.Size;
import io.prover.provermvp.controller.CameraController;
import io.prover.provermvp.controller.CameraControllerBase;
import io.prover.provermvp.util.Frame;

import static io.prover.provermvp.Const.TAG;

/**
 * Created by babay on 11.11.2017.
 */

public class SwypeDetectorHandler extends Handler implements CameraController.OnSwypeCodeSetListener,
        CameraControllerBase.OnRecordingStopListener, CameraControllerBase.SwypeCodeConfirmedListener {
    private static final int MAX_FRAMES_IN_QUEUE = 2;

    private static final int MESSAGE_INIT = 1;
    private static final int MESSAGE_SET_SWYPE = 2;
    private static final int MESSAGE_PROCESS_FRAME = 3;
    private static final int MESSAGE_QUIT = 4;
    private static volatile int counter = 0;
    private final HandlerThread handlerThread;

    private final ProverDetector detector;
    private final AtomicInteger framesInQueue = new AtomicInteger();
    private final CameraController cameraController;
    private final Size videoSize;
    private final Size detectorSize;
    private volatile boolean processing = true;
    private boolean quitDone = false;

    public SwypeDetectorHandler(Looper looper, CameraController cameraController, Size videoSize, Size detectorSize) {
        super(looper);
        handlerThread = (HandlerThread) looper.getThread();
        detector = new ProverDetector(cameraController);
        this.cameraController = cameraController;
        this.videoSize = videoSize;
        this.detectorSize = detectorSize;
        cameraController.swypeCodeSet.add(this);
        cameraController.onRecordingStop.add(this);
        cameraController.swypeCodeConfirmed.add(this);
    }

    public static SwypeDetectorHandler newHandler(Size videoSize, Size detectorSize, CameraController cameraController) {
        HandlerThread handlerThread = new HandlerThread("SwypeDetectorThread_" + counter++);
        handlerThread.start();
        SwypeDetectorHandler handler = new SwypeDetectorHandler(handlerThread.getLooper(), cameraController, videoSize, detectorSize);
        handler.sendInit(cameraController.getOrientationHint());
        return handler;
    }

    public void sendInit(int orientationHint) {
        sendMessage(obtainMessage(MESSAGE_INIT, orientationHint, 0));
    }

    public void sendSetSwype(String swype) {
        sendMessage(obtainMessage(MESSAGE_SET_SWYPE, swype));
    }

    public void onFrameAvailable(Frame frame) {
        if (processing) {
            int inQueue = framesInQueue.get();
            if (inQueue < MAX_FRAMES_IN_QUEUE && processing) {
                framesInQueue.incrementAndGet();
                sendMessage(obtainMessage(MESSAGE_PROCESS_FRAME, frame));
                return;
            } else {
                Log.e(TAG, "frames processing queue maxed out!");
            }
        }
        frame.recycle();
    }

    public void sendQuit() {
        processing = false;
        sendMessage(obtainMessage(MESSAGE_QUIT));
    }

    public void quitSync() {
        processing = false;
        sendMessage(obtainMessage(MESSAGE_QUIT));
        synchronized (detector) {
            while (!quitDone) {
                try {
                    detector.wait();
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_INIT:
                if (processing) {
                    detector.init(videoSize, detectorSize, msg.arg1);
                }
                return;

            case MESSAGE_SET_SWYPE:
                if (processing)
                    detector.setSwype((String) msg.obj);
                return;

            case MESSAGE_PROCESS_FRAME:
                Frame frame = (Frame) msg.obj;
                if (processing) {
                    detector.detectFrame(frame);
                }
                framesInQueue.decrementAndGet();
                frame.recycle();
                break;

            case MESSAGE_QUIT:
                cameraController.swypeCodeSet.remove(this);
                cameraController.onRecordingStop.remove(this);
                cameraController.swypeCodeConfirmed.remove(this);
                detector.release();
                quitDone = true;
                synchronized (detector) {
                    try {
                        detector.notifyAll();
                    } catch (Exception ignored) {
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    handlerThread.quitSafely();
                } else {
                    handlerThread.quit();
                }
                return;
        }
        super.handleMessage(msg);
    }

    @Override
    public void onSwypeCodeSet(String swypeCode, String actualSwypeCode) {
        sendSetSwype(swypeCode);
    }

    @Override
    public void onRecordingStop(File file, boolean isVideoConfirmed) {
        sendQuit();
    }

    @Override
    public void onSwypeCodeConfirmed() {
        cameraController.handler.postDelayed(this::sendQuit, 1000);
    }

    public boolean isAlive() {
        return processing;
    }
}
