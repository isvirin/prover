package io.prover.provermvp.detector;

import java.util.Arrays;

/**
 * Created by babay on 27.11.2017.
 */

public class DetectorTimesCounter {
    private final long[] detectorTimes;
    private final long[] temp;
    private int pos;
    private int dataAmount;

    public DetectorTimesCounter(int len) {
        this.detectorTimes = new long[len];
        temp = new long[len];
    }

    public void add(long time) {
        ++dataAmount;
        detectorTimes[pos++] = time;
        if (pos == detectorTimes.length)
            pos = 0;
    }

    public float getAverageTime() {
        dataAmount = Math.min(dataAmount, detectorTimes.length);
        System.arraycopy(detectorTimes, 0, temp, 0, dataAmount);
        Arrays.sort(temp);
        int start = detectorTimes.length - dataAmount + dataAmount / 5;
        int end = detectorTimes.length - dataAmount / 5;
        long sum = 0;
        for (int i = start; i < end; i++) {
            sum += temp[i];
        }
        return sum == 0 ? 0 : sum / (dataAmount * 0.6f);
    }
}
