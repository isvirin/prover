package io.prover.provermvp.viewholder;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraDevice;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.view.Gravity;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import io.prover.provermvp.camera.CameraUtil;
import io.prover.provermvp.camera.ScreenOrientationLock;
import io.prover.provermvp.camera.Size;
import io.prover.provermvp.camera2.AutoFitTextureView;
import io.prover.provermvp.camera2.MyCamera2;
import io.prover.provermvp.camera2.OrientationHelper;
import io.prover.provermvp.controller.CameraController;
import io.prover.provermvp.permissions.PermissionManager;

import static io.prover.provermvp.camera.CameraUtil.MEDIA_TYPE_VIDEO;

/**
 * Created by babay on 08.11.2017.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraViewHolder2 implements MyCamera2.CameraStateListener, ICameraViewHolder, CameraController.OnRecordingStartListener {

    private final ScreenOrientationLock screenOrientationLock = new ScreenOrientationLock();
    private final FrameLayout mRoot;
    private final CameraController cameraController;
    AutoFitTextureView textureView;
    MyCamera2 myCamera;
    Size selectedSize;
    private Activity activity;
    private boolean resumed;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private Size mSurfaceSize;
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            if (myCamera.getPreviewSize() != null && activity != null) {
                textureView.configureTransform(activity, myCamera.getPreviewSize(), width, height);
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
    private MediaRecorder mMediaRecorder;
    private File videoFile;

    public CameraViewHolder2(FrameLayout root, Activity activity, CameraController cameraController) {
        this.mRoot = root;
        this.textureView = new AutoFitTextureView(root.getContext());
        this.activity = activity;
        this.cameraController = cameraController;
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.gravity = Gravity.CENTER;
        root.addView(textureView, lp);

        myCamera = new MyCamera2(this, cameraController);
        cameraController.onRecordingStart.add(this);
    }

    private void openCamera(int width, int height) {
        if (!PermissionManager.ensureHaveCameraPermission(activity, null))
            return;

        mSurfaceSize = new Size(width, height);
        myCamera.openCamera(activity, mBackgroundHandler, mSurfaceSize, selectedSize);
        selectedSize = myCamera.getVideoSize();
    }

    private void startPreview() {
        Size size = myCamera.getPreviewSize();
        textureView.configurePreviewSize(size);
        textureView.configureTransform(activity, size, textureView.getWidth(), textureView.getHeight());

        myCamera.startPreview(textureView.getSurfaceTexture(), mBackgroundHandler);
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
        cancelRecording();
        myCamera.closeCamera();
        stopBackgroundThread();
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
        if (textureView.isAvailable()) {
            openCamera(textureView.getWidth(), textureView.getHeight());
        } else {
            textureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onCameraOpened(@NonNull CameraDevice cameraDevice) {
        startPreview();
    }

    @Override
    public void onCameraDisconnected(@NonNull CameraDevice cameraDevice) {

    }

    @Override
    public void onCameraError(@NonNull CameraDevice cameraDevice, int error) {
        Toast.makeText(activity, "Video error: " + error, Toast.LENGTH_SHORT).show();
    }

    private void prepareMediaRecorder() throws IOException {
        if (mMediaRecorder == null)
            mMediaRecorder = new MediaRecorder();
        Size videoSize = myCamera.getVideoSize();
        videoFile = CameraUtil.getOutputMediaFile(MEDIA_TYPE_VIDEO, mRoot.getContext());
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(videoFile.getPath());
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(videoSize.width, videoSize.height);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Integer orientationHint = OrientationHelper.getOrientationHint(myCamera.getSensorOrientation(), rotation);
        if (orientationHint != null)
            mMediaRecorder.setOrientationHint(orientationHint);

        mMediaRecorder.prepare();
    }

    @Override
    public void onStop() {
        releaseCamera();
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    @Override
    public void finishRecording() {
        myCamera.stopVideoSession();
        stopRecording();
        cameraController.onRecordingStop(videoFile);
        myCamera.startPreview(textureView.getSurfaceTexture(), mBackgroundHandler);
        MediaScannerConnection.scanFile(mRoot.getContext(), new String[]{videoFile.getAbsolutePath()}, null, null);
        videoFile = null;
    }

    @Override
    public boolean startRecording(Activity activity, float averageFps) {
        if (!textureView.isAvailable() || null == myCamera.getVideoSize()) {
            return false;
        }
        try {
            prepareMediaRecorder();
            SurfaceTexture texture = textureView.getSurfaceTexture();
            myCamera.startVideoRecordingSession(texture, mMediaRecorder, averageFps, mBackgroundHandler);
            mRoot.setKeepScreenOn(true);
            screenOrientationLock.lockScreenOrientation(activity);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void setCameraResolution(Size size) {
        if (size == null || !size.equalsIgnoringRotation(selectedSize)) {
            selectedSize = size;
            if (mSurfaceSize != null) {
                textureView.configurePreviewSize(size);
                textureView.configureTransform(activity, size, textureView.getWidth(), textureView.getHeight());
                myCamera.setResolution(mSurfaceSize, selectedSize, mRoot.getContext());
                myCamera.startPreview(textureView.getSurfaceTexture(), mBackgroundHandler);
            }
        }
    }

    @Override
    public void cancelRecording() {
        if (mMediaRecorder != null && cameraController.isRecording()) {
            stopRecording();
            cameraController.onRecordingStop(videoFile);
            videoFile.delete();
            videoFile = null;
        }
    }

    private void stopRecording() {
        if (mMediaRecorder != null) {
            try {
                mMediaRecorder.stop();
            } catch (Exception ignored) {
            }
            mMediaRecorder.reset();   // clear recorder configuration

            mRoot.setKeepScreenOn(false);
            screenOrientationLock.unlockScreen(activity);
        }
    }

    @Override
    public void onRecordingStart(float fps) {
        mMediaRecorder.start();
    }
}
