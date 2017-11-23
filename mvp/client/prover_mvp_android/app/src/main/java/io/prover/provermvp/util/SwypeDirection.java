package io.prover.provermvp.util;

/**
 * Created by babay on 23.11.2017.
 */

public enum SwypeDirection {
    Left(-1, 0),
    UpLeft(-1, -1),
    Up(0, -1),
    UpRight(1, -1),
    Right(1, 0),
    DownRight(1, 1),
    Down(0, 1),
    DownLeft(-1, 1);

    public final int dx;
    public final int dy;

    SwypeDirection(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public static SwypeDirection ofDelta(int dx, int dy) {
        SwypeDirection[] values = values();
        for (int i = 0; i < values.length; i++) {
            SwypeDirection swypeDirection = values[i];
            if (swypeDirection.dx == dx && swypeDirection.dy == dy)
                return swypeDirection;
        }
        return null;
    }

    public static SwypeDirection ofTwoSwypePoints(int from, int to) {
        int dx = to % 3 - from % 3;
        int dy = to / 3 - from / 3;
        return ofDelta(dx, dy);
    }

    public int degreesTo(SwypeDirection other) {
        int diff = other.ordinal() - ordinal();
        if (diff < 0)
            diff += 8;
        return 360 - diff * 45;
    }

    public boolean isHorizontal() {
        return this == Left || this == Right;
    }

    public boolean isVertical() {
        return this == Up || this == Down;
    }
}
