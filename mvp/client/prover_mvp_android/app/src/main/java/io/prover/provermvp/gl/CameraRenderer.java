package io.prover.provermvp.gl;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Looper;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import io.prover.provermvp.camera.Size;
import io.prover.provermvp.gl.lib.EglCore;
import io.prover.provermvp.gl.lib.GlUtil;
import io.prover.provermvp.gl.lib.WindowSurface;
import io.prover.provermvp.gl.prog.CopyProgram;
import io.prover.provermvp.gl.prog.ReadCameraProgram;
import io.prover.provermvp.gl.prog.ReadCameraToGrayscaleProgram;

/**
 * Base camera rendering class. Responsible for rendering to proper window contexts, as well as
 * recording video with built-in media recorder.
 * <p>
 * Subclass this and add any kind of fun stuff u want, new shaders, textures, uniforms - go to town!
 * <p>
 * TODO: add methods for users to create their own mediarecorders/change basic settings of default mr
 */

public class CameraRenderer extends Thread implements SurfaceTexture.OnFrameAvailableListener {
    /**
     * "arbitrary" maximum number of textures. seems that most phones dont like more than 16
     */
    public static final int MAX_TEXTURES = 4;
    private static final String TAG = CameraRenderer.class.getSimpleName();
    private static final String THREAD_NAME = "CameraRendererThread";
    /**
     * Basic mesh rendering code
     */
    private static float squareSize = 1.0f;
    private static float squareCoords[] = {
            -squareSize, squareSize, // 0.0f,     // top left
            squareSize, squareSize, // 0.0f,   // top right
            -squareSize, -squareSize, // 0.0f,   // bottom left
            squareSize, -squareSize, // 0.0f,   // bottom right
    };
    private static short drawOrder[] = {0, 1, 2, 1, 3, 2};
    private final FrameBufferHolder frameBufferHolder = new FrameBufferHolder();
    private final ReadCameraToGrayscaleProgram cameraToGrayscaleProgram = new ReadCameraToGrayscaleProgram();
    private final ReadCameraProgram readCameraProgram = new ReadCameraProgram();
    private final CopyProgram copyProgram = new CopyProgram();

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
     * Texture created for GLES rendering of camera data
     */
    private SurfaceTexture mPreviewTexture;
    private FloatBuffer textureBuffer;
    private float textureCoords[] = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
    };
    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;
    /**
     * for storing all texture ids from genTextures, and used when binding
     * after genTextures, id[0] is reserved for camera texture
     */
    private int[] mTexturesIds = new int[MAX_TEXTURES];
    /**
     * array of proper constants for use in creation,
     * updating, and drawing. most phones max out at 16
     * same number as {@link #MAX_TEXTURES}
     * <p>
     */
    private int[] mTextureConsts = {
            GLES20.GL_TEXTURE1,
            GLES20.GL_TEXTURE2,
            GLES20.GL_TEXTURE3,
            GLES20.GL_TEXTURE4,
            GLES20.GL_TEXTURE5,
            GLES20.GL_TEXTURE6,
            GLES20.GL_TEXTURE7,
            GLES20.GL_TEXTURE8,
            GLES20.GL_TEXTURE9,
            GLES20.GL_TEXTURE10,
            GLES20.GL_TEXTURE11,
            GLES20.GL_TEXTURE12,
            GLES20.GL_TEXTURE13,
            GLES20.GL_TEXTURE14,
            GLES20.GL_TEXTURE15,
            GLES20.GL_TEXTURE16,
    };

    /**
     * matrix for transforming our camera texture, available immediately after {@link #mPreviewTexture}s
     * {@code updateTexImage()} is called in our main {@link #draw()} loop.
     */
    private float[] mCameraTransformMatrix = new float[16];
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
    public CameraRenderer(Context context, SurfaceTexture texture, int width, int height) {
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

        setupVertexBuffer();
        setupTextures();
        setupCameraTexture();

        cameraToGrayscaleProgram.load(mContext);
        readCameraProgram.load(mContext);
        copyProgram.load(mContext);

        onSetupComplete();
    }


    // ------------------------------------------------------------
    // deinit
    // ------------------------------------------------------------

    public void deinitGL() {
        deinitGLComponents();

        mWindowSurface.release();

        mEglCore.release();
    }

    protected void deinitGLComponents() {
        GLES20.glDeleteTextures(MAX_TEXTURES, mTexturesIds, 0);

        mPreviewTexture.release();
        mPreviewTexture.setOnFrameAvailableListener(null);
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

    protected void setupVertexBuffer() {
        // Draw list buffer
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        // Initialize the texture holder
        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);
    }

    protected void setupTextures() {
        ByteBuffer texturebb = ByteBuffer.allocateDirect(textureCoords.length * 4);
        texturebb.order(ByteOrder.nativeOrder());

        textureBuffer = texturebb.asFloatBuffer();
        textureBuffer.put(textureCoords);
        textureBuffer.position(0);

        // Generate the max amount texture ids
        GLES20.glGenTextures(MAX_TEXTURES, mTexturesIds, 0);
        GlUtil.checkGlError2("Texture generate");
    }

    /**
     * Remember that Android's camera api returns camera texture not as {@link GLES20#GL_TEXTURE_2D}
     * but rather as {@link GLES11Ext#GL_TEXTURE_EXTERNAL_OES}, which we bind here
     */
    protected void setupCameraTexture() {
        //set texture[0] to camera texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTexturesIds[0]);
        GlUtil.checkGlError2("Texture bind");

        mPreviewTexture = new SurfaceTexture(mTexturesIds[0]);
        mPreviewTexture.setOnFrameAvailableListener(this);
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
    public synchronized void start() {
        initialize();

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
    public void shutdown() {

        //kill ouy thread
        Looper.myLooper().quit();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        boolean swapResult;

        synchronized (this) {
            updatePreviewTexture();

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

        //set shader
        //GLES20.glUseProgram(mCameraShaderProgram);
        if (frameBufferHolder.isInitialised()) {
            cameraToGrayscaleProgram.bind(mTexturesIds[0], mCameraTransformMatrix, textureBuffer, vertexBuffer);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferHolder.fboNames[0]);
            GLES20.glViewport(0, 0, frameBufferHolder.size.width, frameBufferHolder.size.height);

            drawElements();

            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            cameraToGrayscaleProgram.unbind();
            GLES20.glViewport(0, 0, mViewportWidth, mViewportHeight);

            copyProgram.bind(frameBufferHolder.textureNames[0], textureBuffer, vertexBuffer);
            drawElements();
            copyProgram.unbind();
        } else {
            readCameraProgram.bind(mTexturesIds[0], mCameraTransformMatrix, textureBuffer, vertexBuffer);
            drawElements();
            readCameraProgram.unbind();
        }

    }

    /**
     * update the SurfaceTexture to the latest camera image
     */
    protected void updatePreviewTexture() {
        mPreviewTexture.updateTexImage();
        mPreviewTexture.getTransformMatrix(mCameraTransformMatrix);

        float[] mtemp = new float[16];
        float[] mtemp2 = new float[16];
        //Matrix.setIdentityM(mCameraTransformMatrix, 0);
    }

    protected void drawElements() {
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
    }

    //getters and setters

    public void setViewport(int viewportWidth, int viewportHeight) {
        mViewportWidth = viewportWidth;
        mViewportHeight = viewportHeight;
    }

    public SurfaceTexture getPreviewTexture() {
        return mPreviewTexture;
    }

    public RenderHandler getRenderHandler() {
        return mHandler;
    }

    public void setOnRendererReadyListener(OnRendererReadyListener listener) {
        mOnRendererReadyListener = listener;

    }

    public void setFrameSize(Size size) {
        frameBufferHolder.deinit();
        frameBufferHolder.init(4, size);
    }

    /**
     * Interface for callbacks when render thread completes its setup
     */
    public interface OnRendererReadyListener {
        /**
         * Called when {@link #onSetupComplete()} is finished with its routine
         */
        void onRendererReady();

        /**
         * Called once the looper is killed and our {@link #run()} method completes
         */
        void onRendererFinished();
    }

}
