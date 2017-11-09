package io.prover.provermvp.viewholder;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraDevice;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.view.Gravity;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.File;

import io.prover.provermvp.camera.ScreenOrientationLock;
import io.prover.provermvp.camera2.AutoFitTextureView;
import io.prover.provermvp.camera2.MyCamera2;
import io.prover.provermvp.permissions.PermissionManager;
import io.prover.provermvp.util.FrameRateCounter;

/**
 * Created by babay on 08.11.2017.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraViewHolder2 implements ImageReader.OnImageAvailableListener, MyCamera2.CameraStateListener, ICameraViewHolder {
    private final ScreenOrientationLock screenOrientationLock = new ScreenOrientationLock();
    private final FrameRateCounter counter = new FrameRateCounter(60);
    AutoFitTextureView mRoot;
    MyCamera2 myCamera;
    private Activity activity;
    private boolean resumed;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            if (myCamera.getPreviewSize() != null && activity != null) {
                mRoot.configureTransform(activity, myCamera.getPreviewSize(), width, height);
            }
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            myCamera.closeCamera();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }

    };

    public CameraViewHolder2(FrameLayout root, Activity activity) {
        this.mRoot = new AutoFitTextureView(root.getContext());
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.gravity = Gravity.CENTER;
        root.addView(mRoot, lp);
        this.activity = activity;
        myCamera = new MyCamera2(this, this);
    }

    public CameraViewHolder2(AutoFitTextureView root, Activity activity) {
        this.mRoot = root;
        this.activity = activity;
        myCamera = new MyCamera2(this, this);
    }

    private void openCamera(int width, int height) {
        if (!PermissionManager.ensureHaveCameraPermission(activity, null))
            return;

        //myCamera.setUpCamera(activity, width, height, mBackgroundHandler);
        myCamera.openCamera(activity, mBackgroundHandler, width, height);
        mRoot.configurePreviewSize(myCamera.getPreviewSize());
        mRoot.configureTransform(activity, myCamera.getPreviewSize(), width, height);
    }

    private void releaseCamera() {
        myCamera.closeCamera();
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    public void onPause(Activity activity) {
        resumed = false;
        myCamera.closeCamera();
        stopBackgroundThread();


        screenOrientationLock.unlockScreen(activity);
        /*releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        if (cameraPreview != null) {
            cameraPreview.releaseCamera();              // release the camera immediately on pause event
        }*/
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume(Activity activity) {
        resumed = true;

        startBackgroundThread();

        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        if (mRoot.isAvailable()) {
            openCamera(mRoot.getWidth(), mRoot.getHeight());
        } else {
            mRoot.setSurfaceTextureListener(mSurfaceTextureListener);
        }

        //PermissionManager.ensureHaveCameraPermission(activity, this::startCamera);
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        counter.addFrame();
        Image image = reader.acquireLatestImage();
        image.close();
    }

    @Override
    public void onCameraOpened(@NonNull CameraDevice cameraDevice) {
        mRoot.configureTransform(activity, myCamera.getPreviewSize(), mRoot.getWidth(), mRoot.getHeight());
        myCamera.startPreview(mRoot.getSurfaceTexture(), mBackgroundHandler);
    }

    @Override
    public void onCameraDisconnected(@NonNull CameraDevice cameraDevice) {

    }

    @Override
    public void onCameraError(@NonNull CameraDevice cameraDevice, int error) {

    }

    @Override
    public void onStop() {

    }

    public boolean isRecording() {
        return false;
    }

    @Override
    public void finishRecording() {

    }

    @Override
    public File getVideoFile() {
        return null;
    }

    @Override
    public boolean startRecording(Activity activity) {
        return false;
    }
}
