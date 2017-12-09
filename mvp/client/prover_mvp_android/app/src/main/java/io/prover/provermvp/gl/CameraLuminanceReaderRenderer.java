package io.prover.provermvp.gl;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.os.Looper;
import android.util.Log;

import io.prover.provermvp.camera.Size;
import io.prover.provermvp.gl.lib.EglCore;
import io.prover.provermvp.gl.lib.WindowSurface;
import io.prover.provermvp.gl.prog.CopyProgram;
import io.prover.provermvp.gl.prog.ReadCameraProgram;

/**
 * Base camera rendering class. Responsible for rendering to proper window contexts, as well as
 * recording video with built-in media recorder.
 * <p>
 * Subclass this and add any kind of fun stuff u want, new shaders, textures, uniforms - go to town!
 * <p>
 * TODO: add methods for users to create their own mediarecorders/change basic settings of default mr
 */

public class CameraLuminanceReaderRenderer extends Thread implements SurfaceTexture.OnFrameAvailableListener, ICameraRenderer {

    private static final String TAG = CameraLuminanceReaderRenderer.class.getSimpleName();
    private static final String THREAD_NAME = "CameraRendererThread";
    private final ReadCameraProgram readCameraProgram = new ReadCameraProgram();
    private final CopyProgram copyProgram = new CopyProgram();

    private final GlTextures textures = new GlTextures();
    private final CameraTexture cameraTexture = new CameraTexture();
    private final TexRect texRect = new TexRect();
    private final ReadLuminancePixelsProcessor readLuminancePixelsProcessor = new ReadLuminancePixelsProcessor();
    /**
     * Current context for use with utility methods
     */
    protected Context mContext;
    protected int mSurfaceWidth, mSurfaceHeight;
    protected float mSurfaceAspectRatio;

    /**
     * main texture for display, based on TextureView that is created in activity or fragment
     * and passed in after onSurfaceTextureAvailable is called, guaranteeing its existence.
     */
    private SurfaceTexture mSurfaceTexture;
    /**
     * EGLCore used for creating {@link WindowSurface}s for preview and recording
     */
    private EglCore mEglCore;
    /**
     * Primary {@link WindowSurface} for rendering to screen
     */
    private WindowSurface mWindowSurface;
    /**
     * Handler for communcation with the UI thread. Implementation below at
     * {@link RenderHandler RenderHandler}
     */
    private RenderHandler mHandler;
    /**
     * Interface listener for some callbacks to the UI thread when rendering is setup and finished.
     */
    private OnRendererReadyListener mOnRendererReadyListener;
    private int mViewportWidth;
    private int mViewportHeight;

    /**
     * Simple ctor to use default shaders
     */
    public CameraLuminanceReaderRenderer(Context context, SurfaceTexture texture, int width, int height) {
        init(context, texture, width, height);
    }

    private void init(Context context, SurfaceTexture texture, int width, int height) {
        this.setName(THREAD_NAME);

        this.mContext = context;
        this.mSurfaceTexture = texture;

        this.mSurfaceWidth = width;
        this.mSurfaceHeight = height;
        this.mSurfaceAspectRatio = (float) width / height;
    }

    private void initialize() {
        setViewport(mSurfaceWidth, mSurfaceHeight);
    }


    /**
     * Initialize all necessary components for GLES rendering, creating window surfaces for drawing
     * the preview as well as the surface that will be used by MediaRecorder for recording
     */
    public void initGL() {
        mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE | EglCore.FLAG_TRY_GLES3);

        //create preview surface
        mWindowSurface = new WindowSurface(mEglCore, mSurfaceTexture);
        mWindowSurface.makeCurrent();

        initGLComponents();
    }

    protected void initGLComponents() {
        onPreSetupGLComponents();

        texRect.init();
        textures.init();
        cameraTexture.init(textures.getNextTexture(), this);
        readLuminancePixelsProcessor.init(mContext, textures, new Size(320, 240));

        readCameraProgram.load(mContext);
        copyProgram.load(mContext);

        onSetupComplete();
    }


    // ------------------------------------------------------------
    // deinit
    // ------------------------------------------------------------

    public void deinitGL() {
        cameraTexture.deInit();
        textures.release();
        readLuminancePixelsProcessor.release();
        mWindowSurface.release();
        mEglCore.release();
    }


    // ------------------------------------------------------------
    // setup
    // ------------------------------------------------------------

    /**
     * override this method if there's anything else u want to accomplish before
     * the main camera setup gets underway
     */
    private void onPreSetupGLComponents() {

    }

    /**
     * called when all setup is complete on basic GL stuffs
     * override for adding textures and other shaders and make sure to call
     * super so that we can let them know we're done
     */
    protected void onSetupComplete() {
        mOnRendererReadyListener.onRendererReady();
    }

    @Override
    public synchronized void start(int rotationAngle) {
        initialize();
        cameraTexture.setScreenRotation(rotationAngle);

        if (mOnRendererReadyListener == null)
            throw new RuntimeException("OnRenderReadyListener is not set! Set listener prior to calling start()");

        super.start();
    }


    /**
     * primary loop - this does all the good things
     */
    @Override
    public void run() {
        Looper.prepare();

        //create handler for communication from UI
        mHandler = new RenderHandler(this);

        //initialize all GL on this context
        initGL();

        //LOOOOOOOOOOOOOOOOP
        Looper.loop();

        //we're done here
        deinitGL();

        mOnRendererReadyListener.onRendererFinished();
    }

    /**
     * stop our thread, and make sure we kill a recording if its still happening
     * <p>
     * this should only be called from our handler to ensure thread-safe
     */
    @Override
    public void shutdown() {

        //kill ouy thread
        Looper.myLooper().quit();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        boolean swapResult;

        synchronized (this) {
            cameraTexture.updatePreviewTexture();

            if (mEglCore.getGlVersion() >= 3) {
                draw();

                //swap main buff
                mWindowSurface.makeCurrent();
                swapResult = mWindowSurface.swapBuffers();
            } else //gl v2
            {
                draw();

                mWindowSurface.makeCurrent();
                swapResult = mWindowSurface.swapBuffers();
            }

            if (!swapResult) {
                // This can happen if the Activity stops without waiting for us to halt.
                Log.e(TAG, "swapBuffers failed, killing renderer thread");
                shutdown();
            }
        }
    }

    /**
     * main draw routine
     */
    public void draw() {
        GLES20.glViewport(0, 0, mViewportWidth, mViewportHeight);

        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);


        if (readLuminancePixelsProcessor.isInitialised()) {
            readLuminancePixelsProcessor.draw(cameraTexture, texRect);

            GLES20.glViewport(0, 0, mViewportWidth, mViewportHeight);

            copyProgram.bind(readLuminancePixelsProcessor.fbo, texRect);
            texRect.draw();
            copyProgram.unbind();
        } else {
            readCameraProgram.bind(cameraTexture.texId, cameraTexture.mCameraTransformMatrix, texRect.textureBuffer, texRect.vertexBuffer);
            texRect.draw();

            readCameraProgram.unbind();
        }
    }

    //getters and setters

    public void setViewport(int viewportWidth, int viewportHeight) {
        mViewportWidth = viewportWidth;
        mViewportHeight = viewportHeight;
    }

    @Override
    public SurfaceTexture getInputTexture() {
        return cameraTexture.mCameraInputTexture;
    }

    @Override
    public RenderHandler getRenderHandler() {
        return mHandler;
    }

    @Override
    public void setOnRendererReadyListener(OnRendererReadyListener listener) {
        mOnRendererReadyListener = listener;

    }

    @Override
    public void setFrameSize(Size size) {
/*        frameBufferHolder.deinit();
        frameBufferHolder.init(4, size);*/
    }

}
