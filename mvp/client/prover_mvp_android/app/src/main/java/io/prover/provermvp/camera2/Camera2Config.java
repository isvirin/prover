package io.prover.provermvp.camera2;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;

import java.util.List;

import io.prover.provermvp.camera.ResolutionSelector;
import io.prover.provermvp.camera.Size;

/**
 * Created by babay on 12.12.2017.
 */

public class Camera2Config {

    private static final int MIN_CAPTURE_SIZE = 200;
    public final List<Size> cameraResolutions;
    public final int mImageFormat;
    public final int mSensorOrientation;
    private final Camera2PrefsHelper camera2PrefsHelper = new Camera2PrefsHelper();
    private final ResolutionSelector resolutionSelector = new ResolutionSelector();
    public Size mVideoSize;
    public Size mPreviewSize;
    public Size mCaptureFrameSize;
    private String mCameraId;

    public Camera2Config(CameraManager manager, String cameraId) throws CameraAccessException {
        this.mCameraId = cameraId;

        CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (map == null) {
            throw new RuntimeException("Cannot get available preview/video sizes");
        }

        mImageFormat = camera2PrefsHelper.selectFormat(map);
        cameraResolutions = camera2PrefsHelper.loadCameraResolutions(map, mImageFormat);
        mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
    }

    public void selectResolutions(Size surfaceSize, Size selectedSize, Context context) throws CameraAccessException {
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        if (manager == null)
            return;

        CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraId);
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        surfaceSize = surfaceSize.scale(0.5f, 0.5f);
        mVideoSize = resolutionSelector.selectResolution(selectedSize, cameraResolutions, surfaceSize, context);
        mCaptureFrameSize = camera2PrefsHelper.chooseOptimalSize(map.getOutputSizes(mImageFormat), new Size(MIN_CAPTURE_SIZE, MIN_CAPTURE_SIZE), mVideoSize, 2.0f);
        mPreviewSize = camera2PrefsHelper.chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), surfaceSize, mVideoSize, 0.1f);
    }

    public ImageReader imageReader(int maxImages) {
        return ImageReader.newInstance(mCaptureFrameSize.width, mCaptureFrameSize.height, mImageFormat, maxImages);
    }
}
