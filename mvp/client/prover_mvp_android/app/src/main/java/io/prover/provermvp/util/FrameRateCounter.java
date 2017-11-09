package io.prover.provermvp.util;

/**
 * Created by babay on 08.11.2017.
 */

public class FrameRateCounter {
    private static final long SEC = 1_000_000_000;
    long[] frameTimes;
    int pos = 0;

    private long lastReportTime = System.currentTimeMillis();

    public FrameRateCounter(int maxFps) {
        frameTimes = new long[maxFps];
    }

    public float addFrame() {
        frameTimes[pos++] = System.nanoTime();
        if (pos == frameTimes.length)
            pos = 0;

        if (System.currentTimeMillis() - lastReportTime >= 1000) {
            float fps = getFps();
            //Log.d(TAG, String.format("FPS: %.0f", fps));
            lastReportTime = System.currentTimeMillis();
            return fps;
        }
        return -1.0f;
    }

    public float getFps() {
        int lastFramePos = pos - 1;
        if (lastFramePos < 0)
            lastFramePos = frameTimes.length - 1;

        long lastFrameTime = frameTimes[lastFramePos];
        long firstframetime = lastFrameTime;

        int fpsPos = lastFramePos;
        int frames = 0;
        while (fpsPos != pos && lastFrameTime - frameTimes[fpsPos] < SEC) {
            firstframetime = frameTimes[fpsPos];
            fpsPos--;
            frames++;
            if (fpsPos < 0)
                fpsPos = frameTimes.length - 1;
        }

        float avgFrameTime = (lastFrameTime - firstframetime) / (float) frames;

        if (SEC - (lastFrameTime - firstframetime) < avgFrameTime)
            return SEC / avgFrameTime;
        return frames;
    }


}
