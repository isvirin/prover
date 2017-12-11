package io.prover.provermvp.camera2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
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

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import io.prover.provermvp.R;
import io.prover.provermvp.camera.ResolutionSelector;
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
    private final ResolutionSelector resolutionSelector = new ResolutionSelector();
    private final Camera2PrefsHelper camera2PrefsHelper = new Camera2PrefsHelper();
    private final CameraController cameraController;
    private final Context context;
    int saveImages = 0;
    private Integer mSensorOrientation;
    private Size mPreviewSize;
    private Size mCaptureFrameSize;
    private String mCameraId;
    private ImageReader mImageReader;
    private CameraDevice mCameraDevice;
    private List<Size> cameraResolutions;
    private volatile VideoSessionWrapper mVideoSessionWrapper;
    private int imageFormat;
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
    /**
     * A reference to the current {@link android.hardware.camera2.CameraCaptureSession} for
     * preview.
     */

    private Size mVideoSize;

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

        surfaceSize = surfaceSize.scale(0.5f, 0.5f);
        try {
            Log.d(TAG, "tryAcquire");
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            mCameraId = camera2PrefsHelper.selectBackCameraId(manager);
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map == null) {
                throw new RuntimeException("Cannot get available preview/video sizes");
            }

            imageFormat = camera2PrefsHelper.selectFormat(map);

            cameraResolutions = camera2PrefsHelper.loadCameraResolutions(map, imageFormat);
            selectResolutions(surfaceSize, selectedSize, map, activity);

            mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
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

    private void selectResolutions(Size surfaceSize, Size selectedSize, StreamConfigurationMap map, Context context) {
        mVideoSize = resolutionSelector.selectResolution(selectedSize, cameraResolutions, surfaceSize, context);
        mCaptureFrameSize = camera2PrefsHelper.chooseOptimalSize(map.getOutputSizes(imageFormat), new Size(200, 200), mVideoSize, 2.0f);
        mPreviewSize = camera2PrefsHelper.chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), surfaceSize, mVideoSize, 0.1f);
    }

    public void setResolution(Size surfaceSize, Size selectedSize, Context context) {
        if (mCameraId == null)
            return;
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        if (manager == null)
            return;

        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            selectResolutions(surfaceSize, selectedSize, map, context);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public Size getPreviewSize() {
        return mPreviewSize;
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
        if (mVideoSessionWrapper == null)
            return;
        mVideoSessionWrapper.closeVideoSession();
        /*if (null != mImageReader)
        {
            mImageReader.close();
            mImageReader = null;
        }
        mImageReader = ImageReader.newInstance(mCaptureFrameSize.width, mCaptureFrameSize.height, imageFormat, 8);
        mImageReader.setOnImageAvailableListener(this, backgroundHandler);*/
        if (mImageReader == null) {
            mImageReader = ImageReader.newInstance(mCaptureFrameSize.width, mCaptureFrameSize.height, imageFormat, 8);
            mImageReader.setOnImageAvailableListener(this, backgroundHandler);
        }
        screenTexture.setDefaultBufferSize(mPreviewSize.width, mPreviewSize.height);
        if (rendererTexture != null) {
            rendererTexture.setDefaultBufferSize(mVideoSize.width, mVideoSize.height);
        }

        Surface[] surfaces = rendererTexture == null ?
                new Surface[]{new Surface(screenTexture), mImageReader.getSurface()}
                : new Surface[]{new Surface(screenTexture), new Surface(rendererTexture) /*, mImageReader.getSurface()*/};

        Runnable startedNotificator = () -> cameraController.onPreviewStart(cameraResolutions, mVideoSize);
        try {
            mVideoSessionWrapper.startVideoSession(backgroundHandler, startedNotificator, surfaces);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void startVideoRecordingSession(SurfaceTexture texture, MediaRecorder mediaRecorder, float fps, Handler backgroundHandler, Activity activity) {
        if (mVideoSessionWrapper == null)
            return;
        mVideoSessionWrapper.closeVideoSession();
        /*if (null != mImageReader) {
            mImageReader.close();
            mImageReader = null;
        }
        mImageReader = ImageReader.newInstance(mCaptureFrameSize.width, mCaptureFrameSize.height, imageFormat, 8);
        mImageReader.setOnImageAvailableListener(this, backgroundHandler);*/
        if (mImageReader == null) {
            mImageReader = ImageReader.newInstance(mCaptureFrameSize.width, mCaptureFrameSize.height, imageFormat, 8);
            mImageReader.setOnImageAvailableListener(this, backgroundHandler);
        }
        texture.setDefaultBufferSize(mPreviewSize.width, mPreviewSize.height);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Integer orientationHint = OrientationHelper.getOrientationHint(mSensorOrientation, rotation);
        Runnable startedNotificator = () -> cameraController.onRecordingStart(fps, mCaptureFrameSize, mVideoSize, orientationHint);
        try {
            mVideoSessionWrapper.startVideoSession(backgroundHandler, startedNotificator, new Surface(texture), mediaRecorder.getSurface(), mImageReader.getSurface());
        } catch (CameraAccessException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        saveImages = 1;
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        try {
            Image image = reader.acquireLatestImage();

            /*if (saveImages > 0) {
                saveImages--;
                Image.Plane plane = image.getPlanes()[0];
                ByteBuffer buffer = plane.getBuffer();
                byte[] data = new byte[buffer.capacity() * 3 / 2];
                buffer.get(data, 0, buffer.capacity());
                int pos = buffer.capacity();
                //image.getPlanes()[1].getBuffer().get(data, pos, pos / 4);
                //image.getPlanes()[2].getBuffer().get(data, pos + pos / 4, pos / 4);

                YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
                File file = new File(Environment.getExternalStorageDirectory(), "out.jpg");

                FileOutputStream filecon = new FileOutputStream(file);
                yuvImage.compressToJpeg(
                        new Rect(0, 0, image.getWidth(), image.getHeight()), 92,
                        filecon);

                MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null, null);
            }*/

            if (image != null) {
                cameraController.onFrameAvailable(Frame.obtain(image));
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }


    public Size getVideoSize() {
        return mVideoSize;
    }

    public Integer getSensorOrientation() {
        return mSensorOrientation;
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
