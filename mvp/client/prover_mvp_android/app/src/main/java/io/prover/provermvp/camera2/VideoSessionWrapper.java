package io.prover.provermvp.camera2;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Surface;

import java.util.ArrayList;
import java.util.List;

import io.prover.provermvp.controller.CameraController;

import static io.prover.provermvp.camera.MyCamera.TAG;

/**
 * Created by babay on 19.11.2017.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class VideoSessionWrapper {
    private final CameraController cameraController;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CameraCaptureSession mCameraVideoSession;

    public VideoSessionWrapper(CameraDevice cameraDevice, CameraController cameraController) {
        this.mCameraDevice = cameraDevice;
        this.cameraController = cameraController;
    }

    public synchronized void closeVideoSession() {
        if (mCameraVideoSession != null) {
            try {
                mCameraVideoSession.close();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
            mCameraVideoSession = null;
            synchronized (this) {
                try {
                    this.wait(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void startVideoSession(Handler mBackgroundHandler, Runnable startedNotificator, Surface... surfaces) throws CameraAccessException {
        mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
        List<Surface> surfaceList = new ArrayList<>();

        for (Surface surface : surfaces) {
            surfaceList.add(surface);
            mPreviewRequestBuilder.addTarget(surface);
        }

        // Start a capture session
        // Once the session starts, we can update the UI and start recording
        mCameraDevice.createCaptureSession(surfaceList, new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                synchronized (this) {
                    if (mCameraDevice == null)
                        return;
                }

                mCameraVideoSession = cameraCaptureSession;
                try {
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                    mCameraVideoSession.setRepeatingRequest(mPreviewRequestBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                            cameraController.onFrameDone();
                        }
                    }, mBackgroundHandler);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
                if (startedNotificator != null)
                    startedNotificator.run();
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                //TODO: do something
            }

            @Override
            public void onClosed(@NonNull CameraCaptureSession session) {
                super.onClosed(session);
            }

        }, mBackgroundHandler);
    }

    public void onCameraDeviceClosed() {
        synchronized (this) {
            mCameraDevice = null;
        }
    }
}
