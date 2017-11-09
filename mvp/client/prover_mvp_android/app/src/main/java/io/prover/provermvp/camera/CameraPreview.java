package io.prover.provermvp.camera;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import io.prover.provermvp.util.FrameRateCounter;

import static android.content.Context.WINDOW_SERVICE;

/**
 * A basic Camera preview class
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private static final String TAG = "CameraPreview";
    private final FrameRateCounter frameRateCounter = new FrameRateCounter(60);
    public boolean recording;
    MyCamera camera;
    Camera mCamera;
    boolean isPreviewRunning;
    boolean isPreviewRequested = true;
    private SurfaceHolder mHolder;
    private int surfaceWidth;
    private int surfaceHeight;

    public CameraPreview(Context context, MyCamera camera) {
        super(context);
        this.camera = camera;
        mCamera = camera.getCamera();

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        surfaceWidth = holder.getSurfaceFrame().width();
        surfaceHeight = holder.getSurfaceFrame().height();
        startPreview();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        this.surfaceWidth = width;
        this.surfaceHeight = height;

        if (isPreviewRequested) {
            try {
                stopPreview();
                isPreviewRequested = true;
                startPreview();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }

        }
    }

    public Camera getCamera() {
        return camera.getCamera();
    }

    public void releaseCamera() {
        if (camera != null) {
            camera.getCamera().setPreviewCallback(null);
            camera.release();        // release the camera for other applications
            camera = null;
        }
    }

    public void stopPreview() {
        if (isPreviewRunning) {
            camera.getCamera().stopPreview();
            camera.getCamera().setPreviewCallback(null);
            isPreviewRunning = false;
        }
        isPreviewRequested = false;
    }

    public void startPreview() {
        isPreviewRequested = true;
        if (surfaceWidth != 0 && surfaceHeight != 0) {
            Display display = ((WindowManager) getContext().getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
            Camera.Parameters parameters = mCamera.getParameters();

            MyCamera.Size size = camera.getOptimalPreviewSize(parameters,
                    surfaceWidth, surfaceHeight, getResources().getDisplayMetrics());

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
                        break;

                    case Surface.ROTATION_90:
                        parameters.setPreviewSize(previewWidth, previewHeight);
                        break;

                    case Surface.ROTATION_180:
                        parameters.setPreviewSize(previewHeight, previewWidth);
                        break;

                    case Surface.ROTATION_270:
                        parameters.setPreviewSize(previewWidth, previewHeight);
                        break;
                }
                parameters.setPreviewFrameRate(30);
                mCamera.setParameters(parameters);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
                parameters = mCamera.getParameters();
                parameters.setPreviewFrameRate(30);
                size = new MyCamera.Size(parameters.getPreviewSize());
            }
            adjustPreviewSize(size);
            camera.updateDisplayOrientation(display.getRotation());

            camera.getCamera().setPreviewCallback(this);

            if (!isPreviewRunning) {
                try {
                    camera.getCamera().setPreviewDisplay(mHolder);
                    camera.getCamera().startPreview();
                    isPreviewRunning = true;
                } catch (Exception e) {
                    Log.d(getClass().getSimpleName(), "Cannot start preview", e);
                }
            }

        }
    }

    public void onChangingOrientation() {
        resetParentPadding();
    }

    private boolean resetParentPadding() {
        if (getParent() instanceof View) {
            View p = (View) getParent();
            if (p.getPaddingLeft() != 0 || p.getPaddingTop() != 0 || p.getPaddingRight() != 0 || p.getPaddingBottom() != 0) {
                p.setPadding(0, 0, 0, 0);
                return true;
            }
        }
        return false;
    }

    private void adjustPreviewSize(MyCamera.Size size) {
        float ratio = surfaceWidth / (float) surfaceHeight;
        float targetRatio = size.ratio;
        boolean flip = ratio < 1;
        if (flip) {
            ratio = 1 / ratio;
        }
        if (Math.abs(1 - ratio / targetRatio) < 0.03) {
            return;
        }
        int vPad = 0, hPad = 0;
        if (flip) {
            if (size.ratio < ratio) {
                vPad = (int) ((surfaceHeight - surfaceWidth * targetRatio) / 2);
            } else {
                hPad = (int) ((surfaceWidth - surfaceHeight / targetRatio) / 2);
            }
        } else {
            if (size.ratio < ratio) {
                hPad = (int) ((surfaceWidth - surfaceHeight * targetRatio) / 2);
            } else {
                vPad = (int) ((surfaceHeight - surfaceWidth / targetRatio) / 2);
            }
        }

        if (getParent() instanceof View) {
            ((View) getParent()).setPadding(hPad, vPad, hPad, vPad);
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

        int i = 0;
        i++;
        frameRateCounter.addFrame();
        //TextureView v;
        //v.getSurfaceTexture().g
    }
}