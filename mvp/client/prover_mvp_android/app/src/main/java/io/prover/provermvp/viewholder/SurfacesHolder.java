package io.prover.provermvp.viewholder;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.TextureView;

import io.prover.provermvp.camera.Size;
import io.prover.provermvp.camera2.AutoFitTextureView;
import io.prover.provermvp.gl.CameraRenderer;

import static io.prover.provermvp.Settings.SHOW_RENDERER_PREVIEW;

/**
 * Created by babay on 24.11.2017.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class SurfacesHolder implements CameraRenderer.OnRendererReadyListener {

    private static final Object sync = new Object();
    public final AutoFitTextureView textureView;
    public final AutoFitTextureView supportTextureView;
    private final SurfacesHolderListener listener;
    private final Activity activity;
    CameraRenderer cameraRenderer;
    private volatile boolean supportTextureReady;
    private volatile boolean screenTextureReady;
    private volatile boolean cameraRendererReady;
    private Size cameraPreviewSize;
    private Size mSurfaceSize;
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            onScreenTextureReady(width, height);
        }


        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            if (cameraPreviewSize != null && activity != null) {
                textureView.configureTransform(activity, cameraPreviewSize, width, height);
            }
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            synchronized (sync) {
                screenTextureReady = false;
            }
            listener.onSurfaceDestroyed();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }
    };
    private Size mSupportSurfaceSize;
    private final TextureView.SurfaceTextureListener mSupportTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            mSupportSurfaceSize = new Size(supportTextureView.getWidth(), supportTextureView.getHeight());
            onSupportTextureReady(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            synchronized (sync) {
                supportTextureReady = false;
                if (cameraRenderer != null) {
                    cameraRenderer.getRenderHandler().sendShutdown();
                    cameraRenderer = null;
                }
            }
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }
    };

    public SurfacesHolder(Activity activity, AutoFitTextureView textureView, AutoFitTextureView supportTextureView, SurfacesHolderListener listener) {
        this.textureView = textureView;
        this.supportTextureView = supportTextureView;
        this.activity = activity;
        this.listener = listener;
    }

    private void onTextureReady() {
        if (supportTextureReady && screenTextureReady && cameraRendererReady) {
            listener.onReady();
        }
    }

    public void onResume() {
        screenTextureReady = false;
        supportTextureReady = false;
        cameraRendererReady = false;

        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).


        if (textureView.isAvailable()) {
            onScreenTextureReady(textureView.getWidth(), textureView.getHeight());
        } else {
            textureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }

        if (supportTextureView.isAvailable()) {
            mSupportSurfaceSize = new Size(supportTextureView.getWidth(), supportTextureView.getHeight());
            onSupportTextureReady(mSupportSurfaceSize.width, mSupportSurfaceSize.height);
        } else {
            supportTextureView.setSurfaceTextureListener(mSupportTextureListener);
        }
    }

    private void onSupportTextureReady(int width, int height) {
        synchronized (sync) {
            supportTextureReady = true;
            if (!SHOW_RENDERER_PREVIEW)
                cameraRendererReady = true;
        }
        if (SHOW_RENDERER_PREVIEW) {
            cameraRenderer = new CameraRenderer(activity, supportTextureView.getSurfaceTexture(), width, height);
            cameraRenderer.setOnRendererReadyListener(this);
            cameraRenderer.start();
        } else
            onTextureReady();
    }

    private void onScreenTextureReady(int width, int height) {
        mSurfaceSize = new Size(width, height);
        synchronized (sync) {
            screenTextureReady = true;
        }
        onTextureReady();
    }

    @Override
    public void onRendererReady() {
        synchronized (sync) {
            cameraRendererReady = true;
        }
        onTextureReady();
    }

    @Override
    public void onRendererFinished() {
        synchronized (sync) {
            cameraRendererReady = false;
        }
    }

    public Size getPreviewSurfaceSize() {
        return mSurfaceSize;
    }

    public void setPreviewSize(Size previewSize) {
        cameraPreviewSize = previewSize;
        if (cameraRenderer != null) {
            cameraRenderer.getRenderHandler().setFrameSize(previewSize);
        }
    }

    public SurfaceTexture getRendererInputTexture() {
        return cameraRenderer == null ? null : cameraRenderer.getPreviewTexture();
    }

    public interface SurfacesHolderListener {
        void onSurfaceDestroyed();

        void onReady();
    }
}
