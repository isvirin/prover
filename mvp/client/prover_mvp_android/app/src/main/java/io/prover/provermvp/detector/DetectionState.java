package io.prover.provermvp.detector;

/**
 * Created by babay on 11.11.2017.
 */

public class DetectionState {
    public final int state;
    public final int index;
    public final int x;
    public final int y;

    public DetectionState(int[] source) {
        state = source[0];
        index = source[1];
        x = source[2];
        y = source[3];
    }

    public boolean isEqualsArray(int[] arr) {
        return state == arr[0] && index == arr[1] && x == arr[2] && y == arr[3];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DetectionState)) return false;

        DetectionState that = (DetectionState) o;

        if (state != that.state) return false;
        if (index != that.index) return false;
        if (x != that.x) return false;
        return y == that.y;
    }

    @Override
    public int hashCode() {
        int result = state;
        result = 31 * result + index;
        result = 31 * result + x;
        result = 31 * result + y;
        return result;
    }
}
