package io.prover.provermvp.camera2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import io.prover.provermvp.R;
import io.prover.provermvp.camera.Size;
import io.prover.provermvp.controller.CameraController;
import io.prover.provermvp.util.Frame;

import static io.prover.provermvp.Const.TAG;

/**
 * Created by babay on 09.12.2016.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MyCamera2 implements ImageReader.OnImageAvailableListener {
    private final CameraStateListener mCameraStateLisneter;
    private final Camera2PrefsHelper camera2PrefsHelper = new Camera2PrefsHelper();
    private final CameraController cameraController;
    private final Context context;
    private String mCameraId;
    private ImageReader mImageReader;
    private CameraDevice mCameraDevice;
    private volatile VideoSessionWrapper mVideoSessionWrapper;

    private Camera2Config camera2Config;

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.
     */
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            mVideoSessionWrapper = new VideoSessionWrapper(mCameraDevice, cameraController);
            mCameraStateLisneter.onCameraOpened(cameraDevice);
        }


        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            VideoSessionWrapper session = mVideoSessionWrapper;
            if (session != null) {
                session.closeVideoSession();
                session.onCameraDeviceClosed();
            }
            mCameraDevice = null;

            mVideoSessionWrapper = null;
            mCameraStateLisneter.onCameraDisconnected(cameraDevice);
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            VideoSessionWrapper session = mVideoSessionWrapper;
            if (session != null) {
                session.closeVideoSession();
                session.onCameraDeviceClosed();
            }

            cameraDevice.close();
            mCameraDevice = null;
            mVideoSessionWrapper = null;
            mCameraStateLisneter.onCameraError(cameraDevice, error);
        }
    };

    public MyCamera2(CameraStateListener mCameraStateLisneter, CameraController cameraController, Context context) {
        this.mCameraStateLisneter = mCameraStateLisneter;
        this.cameraController = cameraController;
        this.context = context.getApplicationContext();
    }

    @SuppressLint("MissingPermission")
/**
 * Tries to open a {@link CameraDevice}. The result is listened by `mStateCallback`.
 */
    @SuppressWarnings("MissingPermission")
    public void openCamera(Activity activity, Handler backgroundHandler, Size surfaceSize, Size selectedSize) {
        if (null == activity || activity.isFinishing()) {
            return;
        }
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        if (manager == null)
            return;

        try {
            Log.d(TAG, "tryAcquire");
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            mCameraId = camera2PrefsHelper.selectBackCameraId(manager);

            camera2Config = new Camera2Config(manager, mCameraId);
            camera2Config.selectResolutions(surfaceSize, selectedSize, activity);
            manager.openCamera(mCameraId, mStateCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            Toast.makeText(activity, "Cannot access the camera.", Toast.LENGTH_SHORT).show();
            activity.finish();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            ErrorDialog.newInstance(activity.getString(R.string.camera_error))
                    .show(activity.getFragmentManager(), "dialog");
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.");
        }
    }


    public void setResolution(Size surfaceSize, Size selectedSize, Context context) {
        if (mCameraId == null || camera2Config == null)
            return;

        try {
            camera2Config.selectResolutions(surfaceSize, selectedSize, context);
        } catch (CameraAccessException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public Size getPreviewSize() {
        return camera2Config.mPreviewSize;
    }

    /**
     * Closes the current {@link CameraDevice}.
     */
    public void closeCamera() {

        try {
            mCameraOpenCloseLock.acquire();
            if (null != mVideoSessionWrapper) {
                mVideoSessionWrapper.closeVideoSession();
                mVideoSessionWrapper.onCameraDeviceClosed();
                mVideoSessionWrapper = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * Creates a new {@link CameraCaptureSession} for camera preview.
     */
    public void startPreview(Handler backgroundHandler, SurfaceTexture screenTexture, SurfaceTexture rendererTexture) {
        if (mVideoSessionWrapper == null || camera2Config == null)
            return;
        mVideoSessionWrapper.closeVideoSession();
        if (mImageReader == null) {
            mImageReader = camera2Config.imageReader(8);
            mImageReader.setOnImageAvailableListener(this, backgroundHandler);
        }
        screenTexture.setDefaultBufferSize(camera2Config.mPreviewSize.width, camera2Config.mPreviewSize.height);
        if (rendererTexture != null) {
            rendererTexture.setDefaultBufferSize(camera2Config.mVideoSize.width, camera2Config.mVideoSize.height);
        }

        Surface[] surfaces = rendererTexture == null ?
                new Surface[]{new Surface(screenTexture), mImageReader.getSurface()}
                : new Surface[]{new Surface(screenTexture), new Surface(rendererTexture) /*, mImageReader.getSurface()*/};

        Runnable startedNotificator = () -> cameraController.onPreviewStart(camera2Config.cameraResolutions, camera2Config.mVideoSize);
        try {
            mVideoSessionWrapper.startVideoSession(backgroundHandler, startedNotificator, surfaces);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void startVideoRecordingSession(SurfaceTexture texture, MediaRecorder mediaRecorder, Handler backgroundHandler, Activity activity) {
        if (mVideoSessionWrapper == null || camera2Config == null)
            return;
        mVideoSessionWrapper.closeVideoSession();
        if (mImageReader == null) {
            mImageReader = camera2Config.imageReader(8);
            mImageReader.setOnImageAvailableListener(this, backgroundHandler);
        }
        texture.setDefaultBufferSize(camera2Config.mPreviewSize.width, camera2Config.mPreviewSize.height);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Integer orientationHint = OrientationHelper.getOrientationHint(camera2Config.mSensorOrientation, rotation);
        Runnable startedNotificator = () -> cameraController.onRecordingStart(camera2Config.mCaptureFrameSize, camera2Config.mVideoSize, orientationHint);
        try {
            mVideoSessionWrapper.startVideoSession(backgroundHandler, startedNotificator, new Surface(texture), mediaRecorder.getSurface(), mImageReader.getSurface());
        } catch (CameraAccessException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        try {
            Image image = reader.acquireLatestImage();
            if (image != null) {
                cameraController.onFrameAvailable(Frame.obtain(image));
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }


    public Size getVideoSize() {
        return camera2Config.mVideoSize;
    }

    public Integer getSensorOrientation() {
        return camera2Config.mSensorOrientation;
    }

    public void stopVideoSession() {
        if (mVideoSessionWrapper != null) {
            mVideoSessionWrapper.closeVideoSession();
        }
    }

    public interface CameraStateListener {
        void onCameraOpened(@NonNull CameraDevice cameraDevice);

        void onCameraDisconnected(@NonNull CameraDevice cameraDevice);

        void onCameraError(@NonNull CameraDevice cameraDevice, int error);
    }
}