package io.prover.provermvp.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by babay on 28.12.2017.
 */

public class DefectView extends View {
    private static final float TARGET_RADIUS = 0.22f;
    private static final float FIT_FACTOR_H = 0.55f;
    private static final float PARABOLA_START = -0.03f;
    private static final float PARABOLA_END = 1.21f;
    private static final float PARABOLA_STEP = 0.123f;
    private final RectF defectRect = new RectF();
    private final RectF defectRect2 = new RectF();
    private final RectF targetRect = new RectF();
    Paint p = new Paint();
    Paint targetPaint = new Paint();
    Paint borderPaint = new Paint();
    Point targetCenter = new Point();
    Point sidePoint1 = new Point();
    Point sidePoint2 = new Point();
    float targetRadius;
    Path parabPath1 = new Path();
    Path parabPath2 = new Path();
    boolean isDiagonal;
    float parabolaRotationAngle;
    float fromXpx;
    float fromYpx;

    public DefectView(Context context) {
        this(context, null);
    }

    public DefectView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DefectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        float density = getResources().getDisplayMetrics().density;

        p.setAntiAlias(true);
        p.setColor(0x40ff0000);
        p.setStyle(Paint.Style.FILL);

        targetPaint.setAntiAlias(true);
        targetPaint.setStrokeWidth(density);
        targetPaint.setColor(Color.GREEN);
        targetPaint.setStyle(Paint.Style.STROKE);

        borderPaint.setAntiAlias(true);
        borderPaint.setStrokeWidth(density);
        borderPaint.setColor(Color.RED);
        borderPaint.setStyle(Paint.Style.STROKE);

        targetRadius = density * 96 * TARGET_RADIUS;

        float sqrt2 = (float) Math.sqrt(2);
        float sqrt2neg = 1 / sqrt2;
        float y = (PARABOLA_START - sqrt2neg);
        y = (y * y + 0.5f) / sqrt2;
        float mul = density * 96;
        parabPath1.moveTo(PARABOLA_START * mul, y * mul);
        parabPath2.moveTo(PARABOLA_START * mul, -y * mul);
        for (float x = PARABOLA_START; x <= PARABOLA_END; x += PARABOLA_STEP) {
            y = (x - sqrt2neg);
            y = (y * y + 0.5f) / sqrt2;
            parabPath1.lineTo(x * mul, y * mul);
            parabPath2.lineTo(x * mul, -y * mul);
        }
    }

    public void set(float centerX, float centerY, float defectX, float defectY) {
        defectRect.left = centerX - defectX;
        defectRect.right = centerX + defectX;
        defectRect.top = centerY - defectY;
        defectRect.bottom = centerY + defectY;

        defectRect2.left = centerX - defectX * 2;
        defectRect2.right = centerX + defectX * 2;
        defectRect2.top = centerY - defectY * 2;
        defectRect2.bottom = centerY + defectY * 2;
        invalidate();
    }

    public void configureBorder(int from, int to) {
        --from;
        --to;
        int fromX = from % 3;
        int fromY = from / 3;
        int toX = to % 3;
        int toY = to / 3;

        float density = getResources().getDisplayMetrics().density;

        fromXpx = density * (34 + 96 * fromX);
        fromYpx = density * (34 + 96 * fromY);

        float toXpx = density * (34 + 96 * toX);
        float toYpx = density * (34 + 96 * toY);

        targetCenter.set((int) toXpx, (int) toYpx);

        targetRect.left = Math.min(fromXpx, toXpx);
        targetRect.top = Math.min(fromYpx, toYpx);
        targetRect.right = Math.max(fromXpx, toXpx);
        targetRect.bottom = Math.max(fromYpx, toYpx);
        float shift = 96 * FIT_FACTOR_H * density;
        if (fromX < toX) {
            targetRect.left -= shift;
            targetRect.right += targetRadius;
        } else if (fromX > toX) {
            targetRect.right += shift;
            targetRect.left -= targetRadius;
        } else {
            targetRect.left -= shift;
            targetRect.right += shift;
        }
        if (fromY < toY) {
            targetRect.top -= shift;
            targetRect.bottom += targetRadius;
        } else if (fromY > toY) {
            targetRect.bottom += shift;
            targetRect.top -= targetRadius;
        } else {
            targetRect.top -= shift;
            targetRect.bottom += shift;
        }

        isDiagonal = fromX != toX && fromY != toY;
        if (isDiagonal) {
            if (toX > fromX)
                parabolaRotationAngle = toY > fromY ? 45 : -45;
            else
                parabolaRotationAngle = toY > fromY ? 135 : -135;

            sidePoint1.set((int) fromXpx, (int) toYpx);
            sidePoint2.set((int) toXpx, (int) fromYpx);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawOval(defectRect, p);
        canvas.drawOval(defectRect2, p);
        canvas.drawRect(targetRect, borderPaint);

        canvas.drawCircle(targetCenter.x, targetCenter.y, targetRadius, targetPaint);

        if (isDiagonal) {
            canvas.save();
            canvas.translate(fromXpx, fromYpx);
            canvas.rotate(parabolaRotationAngle);
            canvas.drawPath(parabPath1, borderPaint);
            canvas.drawPath(parabPath2, borderPaint);
            canvas.restore();
            canvas.drawCircle(sidePoint1.x, sidePoint1.y, targetRadius, borderPaint);
            canvas.drawCircle(sidePoint2.x, sidePoint2.y, targetRadius, borderPaint);
        }
    }

    @Override
    public void onDrawForeground(Canvas canvas) {
        super.onDrawForeground(canvas);
    }
}
