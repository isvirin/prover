package io.prover.provermvp.viewholder;

import android.hardware.Camera;
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

import static android.content.Context.WINDOW_SERVICE;
import static io.prover.provermvp.Const.TAG;

/**
 * Created by babay on 09.11.2017.
 */

public class CameraPreviewHolder implements SurfaceHolder.Callback {
    private final AutoFitSurfaceView mRoot;
    private final SurfaceHolder mHolder;
    private final Camera.PreviewCallback previewCallback;
    private volatile MyCamera mCamera;
    private int surfaceWidth;
    private int surfaceHeight;
    private volatile boolean isPreviewRequested;
    private volatile boolean isPreviewRunning;
    private boolean hasPermissions = false;

    private MyCamera.Size preferredSize;

    public CameraPreviewHolder(AutoFitSurfaceView root, MyCamera camera, Camera.PreviewCallback previewCallback) {
        this.mRoot = root;
        this.mCamera = camera;
        this.previewCallback = previewCallback;
        mHolder = mRoot.getHolder();
        mHolder.addCallback(this);
    }

    public CameraPreviewHolder(FrameLayout root, MyCamera camera, Camera.PreviewCallback previewCallback) {
        this(new AutoFitSurfaceView(root.getContext()), camera, previewCallback);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.gravity = Gravity.CENTER;
        root.addView(mRoot, lp);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        surfaceWidth = holder.getSurfaceFrame().width();
        surfaceHeight = holder.getSurfaceFrame().height();
        startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        this.surfaceWidth = width;
        this.surfaceHeight = height;

        if (isPreviewRequested) {
            restartPreview();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopPreview();
    }

    public void startPreview() {
        isPreviewRequested = true;
        if (!hasPermissions)
            return;
        if (surfaceWidth != 0 && surfaceHeight != 0) {
            Display display = ((WindowManager) mRoot.getContext().getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
            Camera camera = mCamera.open();
            if (camera == null)
                return;
            Camera.Parameters parameters = camera.getParameters();

            MyCamera.Size size = mCamera.getOptimalVideoSize(parameters,
                    surfaceWidth, surfaceHeight, mRoot.getResources().getDisplayMetrics());

            int previewWidth = size.width;
            int previewHeight = size.height;

            if (previewHeight < previewWidth && surfaceHeight > surfaceWidth) {
                int a = previewWidth;
                previewWidth = previewHeight;
                previewHeight = a;
            }

            try {
                switch (display.getRotation()) {
                    case Surface.ROTATION_0:
                        parameters.setPreviewSize(previewHeight, previewWidth);
                        parameters.setPictureSize(previewHeight, previewWidth);
                        break;

                    case Surface.ROTATION_90:
                        parameters.setPreviewSize(previewWidth, previewHeight);
                        parameters.setPictureSize(previewWidth, previewHeight);
                        break;

                    case Surface.ROTATION_180:
                        parameters.setPreviewSize(previewHeight, previewWidth);
                        parameters.setPictureSize(previewHeight, previewWidth);
                        break;

                    case Surface.ROTATION_270:
                        parameters.setPreviewSize(previewWidth, previewHeight);
                        parameters.setPictureSize(previewWidth, previewHeight);
                        break;
                }
                parameters.setPreviewFpsRange(25, 60);
                camera.setParameters(parameters);

            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
                parameters = camera.getParameters();
                size = new MyCamera.Size(parameters.getPreviewSize());
            }
            mRoot.configurePreviewSize(size);
            mCamera.updateDisplayOrientation(display.getRotation());

            camera.setPreviewCallback(previewCallback);

            if (!isPreviewRunning) {
                try {
                    camera.setPreviewDisplay(mHolder);
                    camera.startPreview();
                    isPreviewRunning = true;
                } catch (Exception e) {
                    Log.d(getClass().getSimpleName(), "Cannot start preview", e);
                }
            }

        }
    }

    public void stopPreview() {
        if (isPreviewRunning) {
            Camera camera = mCamera.getCamera();
            if (camera != null) {
                camera.stopPreview();
                camera.setPreviewCallback(null);
            }
            isPreviewRunning = false;
        }
        isPreviewRequested = false;
    }

    public void onResume() {
        if (isPreviewRequested) {
            restartPreview();
        }
    }

    private void restartPreview() {
        try {
            stopPreview();
            isPreviewRequested = true;
            startPreview();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void releaseCamera() {
        if (mCamera != null)
            mCamera.release();
    }

    public void lockCamera() {
        Camera camera = mCamera.open();
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

    public Surface getSurface() {
        return mHolder.getSurface();
    }

    public void setHasPermissions(boolean hasPermissions) {
        this.hasPermissions = hasPermissions;
        if (hasPermissions && isPreviewRequested && !isPreviewRunning) {
            if (mCamera == null)
                mCamera = MyCamera.openBackCamera();
            startPreview();
        }
    }

    public void setPreferredSize(MyCamera.Size preferredSize) {
        this.preferredSize = preferredSize;
    }
}