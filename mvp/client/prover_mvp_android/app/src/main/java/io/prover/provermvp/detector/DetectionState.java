package io.prover.provermvp.detector;

import io.prover.provermvp.BuildConfig;

/**
 * Created by babay on 11.11.2017.
 */

public class DetectionState {
    public final State state;
    public final int index;
    public final int x;
    public final int y;
    public final int d;
    public DetectionState(int[] source) {
        state = State.values()[source[0]];
        index = source[1];
        x = source[2];
        y = source[3];
        d = source[4];
    }

    public DetectionState(long[] source) {
        state = State.values()[(int) source[0]];
        index = (int) source[1];
        x = (int) source[2];
        y = (int) source[3];
        d = (int) source[4];
    }

    public boolean isEqualsArray(int[] arr) {
        if (BuildConfig.DEBUG) {
            return state.ordinal() == arr[0] && index == arr[1] && x == arr[2] && y == arr[3] && d == arr[4];
        } else {
            return state.ordinal() == arr[0] && index == arr[1] && x == arr[2] && y == arr[3];
        }
    }

    public boolean isEqualsArray(long[] arr) {
        if (BuildConfig.DEBUG) {
            return state.ordinal() == arr[0] && index == arr[1] && x == arr[2] && y == arr[3] && d == arr[4];
        } else {
            return state.ordinal() == arr[0] && index == arr[1] && x == arr[2] && y == arr[3];
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DetectionState)) return false;

        DetectionState that = (DetectionState) o;

        if (state != that.state) return false;
        if (index != that.index) return false;
        if (x != that.x) return false;
        if (y != that.y) return false;
        return !BuildConfig.DEBUG || d == that.d;
    }

    @Override
    public int hashCode() {
        int result = state.ordinal();
        result = 31 * result + index;
        result = 31 * result + x;
        result = 31 * result + y;
        result = 31 * result + d;
        return result;
    }

    public enum State {Waiting, GotProverNoCode, GotProverWaiting, InputCode, Confirmed}
}
