package io.prover.provermvp.viewholder;

import android.hardware.Camera;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import io.prover.provermvp.camera.AutoFitSurfaceView;
import io.prover.provermvp.camera.MyCamera;
import io.prover.provermvp.camera.Size;
import io.prover.provermvp.controller.CameraController;

import static android.content.Context.WINDOW_SERVICE;
import static io.prover.provermvp.Const.TAG;

/**
 * Created by babay on 09.11.2017.
 */

public class CameraPreviewHolder implements SurfaceHolder.Callback {
    private final FrameLayout mRoot;
    private final CameraController cameraController;
    private AutoFitSurfaceView surfaceView;
    private volatile MyCamera mCamera;
    private Size surfaceSize;
    private Size cameraResolution;
    private volatile boolean isPreviewRunning;
    private boolean hasPermissions = false;
    private volatile boolean surfaceCreated = false;
    /**
     * current resolution with orientation matching surface orientation
     */
    private Size currentResolution;

    public CameraPreviewHolder(FrameLayout root, MyCamera camera, CameraController cameraController) {
        this.mRoot = root;
        this.mCamera = camera;
        this.cameraController = cameraController;
        addSurfaceView();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        surfaceSize = new Size(holder.getSurfaceFrame().width(), holder.getSurfaceFrame().height());
        surfaceCreated = true;
        if (surfaceSize.width > 0 && surfaceSize.height > 0 && mCamera != null) {
            cameraResolution = selectResolution();
            startPreview();
        }
    }

    private @Nullable
    Size selectResolution() {
        if (mCamera == null || surfaceSize == null || surfaceSize.area() == 0)
            return null;
        return mCamera.selectResolution(currentResolution, surfaceSize, mRoot.getContext());
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        surfaceSize = new Size(width, height);
        Size newSize = selectResolution();
        if (newSize != null && !newSize.equals(cameraResolution) && surfaceCreated) {
            cameraResolution = newSize;
            restartPreview();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceCreated = false;
        stopPreview();
    }

    public void startPreview() {
        if (!hasPermissions || cameraResolution == null || surfaceSize == null || surfaceView == null || surfaceSize.area() <= 0)
            return;
        Camera camera = mCamera.open();
        if (camera == null)
            return;
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(cameraResolution.width, cameraResolution.height);
        parameters.setPictureSize(cameraResolution.width, cameraResolution.height);
        parameters.setRecordingHint(true);
        parameters.setPreviewFpsRange(25, 60);
        try {
            camera.setParameters(parameters);
            Log.d(TAG, "selected resolution: " + cameraResolution.toString());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            parameters = camera.getParameters();
            cameraResolution = new Size(parameters.getPreviewSize());
        }
        currentResolution = cameraResolution.toOrientation(surfaceSize.getOrientation());
        surfaceView.configurePreviewSize(currentResolution);
        Display display = ((WindowManager) mRoot.getContext().getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        mCamera.updateDisplayOrientation(display.getRotation());

        mCamera.updateCallback();

        if (!isPreviewRunning) {
            try {
                camera.setPreviewDisplay(surfaceView.getHolder());
                camera.startPreview();
                cameraController.previewStart.postNotifyEvent(mCamera.getAvailableResolutions(), cameraResolution);
                isPreviewRunning = true;
            } catch (Exception e) {
                Log.d(getClass().getSimpleName(), "Cannot start preview", e);
            }
        }
    }

    public void stopPreview() {
        if (isPreviewRunning) {
            mCamera.stopPreview();
            isPreviewRunning = false;
        }
    }

    private void restartPreview() {
        try {
            stopPreview();
            startPreview();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void releaseCamera() {
        stopPreview();
        if (mCamera != null)
            mCamera.release();
        removeSurfaceView();
    }

    public void onResume() {
        addSurfaceView();
    }

    private void removeSurfaceView() {
        if (surfaceView != null) {
            surfaceView.getHolder().removeCallback(this);
            mRoot.removeView(surfaceView);
            surfaceView = null;
        }
    }

    private void addSurfaceView() {
        if (surfaceView == null) {
            this.surfaceView = new AutoFitSurfaceView(mRoot.getContext());
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            lp.gravity = Gravity.CENTER;
            mRoot.addView(surfaceView, lp);
            surfaceView.getHolder().addCallback(this);
        }
    }

    public void lockCamera() {
        Camera camera = mCamera.getCamera();
        if (camera != null)
            camera.lock();
    }

    public void unlockCamera() {
        Camera camera = mCamera.getCamera();
        if (camera != null)
            camera.unlock();
    }

    public MyCamera getCamera() {
        return mCamera;
    }

    public @Nullable
    Surface getSurface() {
        return surfaceView == null ? null : surfaceView.getHolder().getSurface();
    }

    public void setHasPermissions(boolean hasPermissions) {
        this.hasPermissions = hasPermissions;
        if (hasPermissions) {
            if (mCamera == null) mCamera = MyCamera.openBackCamera(cameraController);
            if (surfaceCreated && !isPreviewRunning && surfaceSize != null && surfaceSize.area() > 0) {
                cameraResolution = selectResolution();
                startPreview();
            }
        }
    }

    public void setResolution(Size size) {
        if (size == null)
            return;
        if (cameraResolution == null || !currentResolution.equals(size)) {
            currentResolution = size;
            cameraResolution = selectResolution();
            stopPreview();
            releaseCamera();
            removeSurfaceView();

            new Handler().postDelayed(this::addSurfaceView, 10);
        }
    }

    public Size getCameraResolution() {
        return cameraResolution;
    }
}
