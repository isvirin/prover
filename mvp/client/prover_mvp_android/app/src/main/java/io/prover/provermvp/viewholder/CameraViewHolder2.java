package io.prover.provermvp.viewholder;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraDevice;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import io.prover.common.permissions.PermissionManager;
import io.prover.provermvp.R;
import io.prover.provermvp.camera.CameraUtil;
import io.prover.common.util.ScreenOrientationLock;
import io.prover.provermvp.camera.Size;
import io.prover.provermvp.camera2.AutoFitTextureView;
import io.prover.provermvp.camera2.MyCamera2;
import io.prover.provermvp.camera2.OrientationHelper;
import io.prover.provermvp.controller.CameraController;

import static io.prover.provermvp.camera.CameraUtil.MEDIA_TYPE_VIDEO;

/**
 * Created by babay on 08.11.2017.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraViewHolder2 implements MyCamera2.CameraStateListener, ICameraViewHolder, CameraController.OnRecordingStartListener, SurfacesHolder.SurfacesHolderListener {

    private final ScreenOrientationLock screenOrientationLock = new ScreenOrientationLock();
    private final Activity activity;
    private final FrameLayout mRoot;
    private final MyCamera2 myCamera;
    private final CameraController cameraController;
    private final SurfacesHolder surfacesHolder;
    private final Handler foregroundHandler = new Handler(Looper.getMainLooper());

    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private Size selectedSize;
    private boolean resumed;
    private MediaRecorder mMediaRecorder;
    private File videoFile;

    public CameraViewHolder2(ViewGroup root, Activity activity, CameraController cameraController) {
        this.mRoot = root.findViewById(R.id.cameraContainer);
        AutoFitTextureView textureView = new AutoFitTextureView(root.getContext());
        this.activity = activity;
        this.cameraController = cameraController;
        AutoFitTextureView supportTextureView = root.findViewById(R.id.supportTextureView);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.gravity = Gravity.CENTER;
        mRoot.addView(textureView, lp);

        surfacesHolder = new SurfacesHolder(activity, textureView, supportTextureView, this);

        myCamera = new MyCamera2(this, cameraController, root.getContext());
        cameraController.onRecordingStart.add(this);
    }

    private void openCamera() {
        if (!PermissionManager.ensureHaveCameraPermission(activity, null))
            return;


        myCamera.openCamera(activity, mBackgroundHandler, surfacesHolder.getPreviewSurfaceSize(), selectedSize);
        selectedSize = myCamera.getVideoSize();
        surfacesHolder.setPreviewSize(myCamera.getPreviewSize());
    }

    private void startPreview() {
        Size size = myCamera.getPreviewSize();
        foregroundHandler.post(() -> {
            surfacesHolder.textureView.configurePreviewSize(size);
            surfacesHolder.textureView.configureTransform(activity, size, surfacesHolder.textureView.getWidth(), surfacesHolder.textureView.getHeight());
        });

        myCamera.startPreview(mBackgroundHandler, surfacesHolder.textureView.getSurfaceTexture(), surfacesHolder.getRendererInputTexture());
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
        //surfacesHolder.onPause();
        if (cameraController.isRecording()) {
            finishRecording();
        }
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
        surfacesHolder.onResume(activity);
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
        mMediaRecorder.setVideoEncodingBitRate(videoSize.getHighQualityBitRate());
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
        Runnable r = () -> {
            cameraController.beforeRecordingStop();
            myCamera.stopVideoSession();
            stopRecording();
            cameraController.onRecordingStop(mRoot.getContext(), videoFile);
            if (resumed)
                myCamera.startPreview(mBackgroundHandler, surfacesHolder.textureView.getSurfaceTexture(), surfacesHolder.getRendererInputTexture());
            videoFile = null;
        };
        if (mBackgroundHandler != null)
            mBackgroundHandler.post(r);
        else r.run();
    }

    @Override
    public boolean startRecording(Activity activity) {
        if (!surfacesHolder.textureView.isAvailable() || null == myCamera.getVideoSize() || mBackgroundThread == null || !mBackgroundThread.isAlive()) {
            return false;
        }
        mBackgroundHandler.post(() -> {
            try {
                prepareMediaRecorder();
                SurfaceTexture texture = surfacesHolder.textureView.getSurfaceTexture();
                myCamera.startVideoRecordingSession(texture, mMediaRecorder, mBackgroundHandler, activity);
                foregroundHandler.post(() -> {
                    mRoot.setKeepScreenOn(true);
                    screenOrientationLock.lockScreenOrientation(activity);
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return true;
    }

    @Override
    public void setCameraResolution(Size size) {
        if (size == null || !size.equalsIgnoringRotation(selectedSize)) {
            selectedSize = size;
            if (surfacesHolder.getPreviewSurfaceSize() != null) {
                surfacesHolder.textureView.configurePreviewSize(size);
                surfacesHolder.textureView.configureTransform(activity, size, surfacesHolder.textureView.getWidth(), surfacesHolder.textureView.getHeight());
                myCamera.setResolution(surfacesHolder.getPreviewSurfaceSize(), selectedSize, mRoot.getContext());
                surfacesHolder.setPreviewSize(myCamera.getPreviewSize());
                myCamera.startPreview(mBackgroundHandler, surfacesHolder.textureView.getSurfaceTexture(), surfacesHolder.getRendererInputTexture());
            }
        }
    }

    @Override
    public void cancelRecording() {
        if (mMediaRecorder != null && cameraController.isRecording()) {
            cameraController.beforeRecordingStop();
            myCamera.stopVideoSession();
            stopRecording();
            cameraController.onRecordingStop(mRoot.getContext(), videoFile);
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

            if (Thread.currentThread().equals(foregroundHandler.getLooper().getThread())) {
                mRoot.setKeepScreenOn(false);
                screenOrientationLock.unlockScreen(activity);
            } else {
                foregroundHandler.post(() -> {
                    mRoot.setKeepScreenOn(false);
                    screenOrientationLock.unlockScreen(activity);
                });
            }
        }
    }

    @Override
    public void onRecordingStart() {
        mMediaRecorder.start();
    }

    @Override
    public void onSurfaceDestroyed() {
        myCamera.closeCamera();
    }

    @Override
    public void onReady() {
        openCamera();
    }
}
