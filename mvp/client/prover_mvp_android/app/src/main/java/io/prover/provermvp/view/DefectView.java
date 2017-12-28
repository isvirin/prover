package io.prover.provermvp.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by babay on 28.12.2017.
 */

public class DefectView extends View {
    Paint p = new Paint();
    private float left, top, right, bottom;

    public DefectView(Context context) {
        this(context, null);
    }

    public DefectView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DefectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        p.setAntiAlias(true);
        p.setColor(0x80ff0000);
    }

    public void set(float centerX, float centerY, float defectX, float defectY) {
        left = centerX - defectX;
        right = centerX + defectX;
        top = centerY - defectY;
        bottom = centerY + defectY;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
        canvas.drawRect(left, top, right, bottom, p);
    }

    @Override
    public void onDrawForeground(Canvas canvas) {
        super.onDrawForeground(canvas);
    }
}
