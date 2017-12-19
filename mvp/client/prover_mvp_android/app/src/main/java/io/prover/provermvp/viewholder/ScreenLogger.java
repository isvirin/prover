package io.prover.provermvp.viewholder;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.constraint.ConstraintLayout;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.prover.provermvp.R;

/**
 * Created by babay on 14.12.2017.
 */

public class ScreenLogger {
    private final TextView textView;
    private final View belowView;

    public ScreenLogger(ConstraintLayout root) {
        this.textView = new TextView(root.getContext());
        belowView = root.findViewById(R.id.balanceContainer);
        Resources res = root.getResources();
        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        lp.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
        lp.bottomToTop = R.id.bottomControlsLayout;
        root.addView(textView, lp);
        textView.setGravity(Gravity.BOTTOM);
        textView.setMaxWidth((int) (res.getDisplayMetrics().widthPixels * 0.85f));

        int color = res.getColor(R.color.controlsBgColor);
        textView.setBackgroundColor(color);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
        int pad = (int) (res.getDisplayMetrics().density * 4);
        textView.setPadding(pad, pad, pad, pad);
        textView.setAlpha(0.6f);
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append("Screen Log");
        builder.setSpan(new StyleSpan(Typeface.BOLD), 0, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(builder);
    }

    private static int nextEndLine(CharSequence text, TextPaint tp, int start, int maxWidth) {
        int length = text.length();
        int end = start + tp.breakText(text, start, length, true, maxWidth, null);

        for (int i = start; i < end; i++) {
            if (text.charAt(i) == '\n') {
                return i + 1;
            }
        }
        return end;
    }

    public void addText(CharSequence text) {
        Editable editable = getTextEditable();
        editable.append(text);
        setText(editable);
    }

    public Editable getTextEditable() {
        CharSequence text = textView.getText();
        SpannableStringBuilder builder = text instanceof SpannableStringBuilder ? (SpannableStringBuilder) text : new SpannableStringBuilder(text);
        if (builder.length() > 0 && builder.charAt(builder.length() - 1) != '\n')
            builder.append('\n');
        return builder;
    }

    public void setText(CharSequence text) {
        SpannableStringBuilder builder = text instanceof SpannableStringBuilder ? (SpannableStringBuilder) text : new SpannableStringBuilder(text);

        int maxHeight = getMaxTextHeight();
        int lineHeight = textView.getLineHeight();
        if (maxHeight > 0 && lineHeight > 0) {
            TextPaint tp = textView.getPaint();
            int maxWidth = getMaxTextWidth();
            int maxLines = maxHeight / lineHeight - 1;
            int lineCount = 0;
            int cutoffIndex = 0;
            int index = 0;
            int length = builder.length();

            while (index < length - 1) {
                index = nextEndLine(builder, tp, index, maxWidth);
                lineCount++;
                if (lineCount > maxLines) {
                    cutoffIndex = nextEndLine(builder, tp, cutoffIndex, maxWidth);
                }
            }
            if (cutoffIndex > 0) {
                int start = 0;
                StyleSpan[] spans = builder.getSpans(0, builder.length(), StyleSpan.class);
                if (spans != null && spans.length > 0) {
                    start = builder.getSpanEnd(spans[0]) + 1;
                }
                builder.replace(start, cutoffIndex, "");
            }
        }
        if (builder.length() > 0 && builder.charAt(builder.length() - 1) == '\n')
            builder.replace(builder.length() - 1, builder.length(), "");

        textView.setText(builder);
    }

    private int getMaxTextWidth() {
        int maxWidth = textView.getMaxWidth();
        if (maxWidth == 0) {
            ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) textView.getLayoutParams();
            maxWidth = ((View) textView.getParent()).getWidth() - lp.leftMargin - lp.rightMargin;
        }
        maxWidth = maxWidth - textView.getTotalPaddingLeft() - textView.getTotalPaddingRight();
        return maxWidth;
    }

    private int getMaxTextHeight() {
        int maxHeight = textView.getBottom();
        if (maxHeight == 0)
            return 0;

        maxHeight -= belowView.getBottom();
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) textView.getLayoutParams();
        return maxHeight - textView.getTotalPaddingTop() - textView.getTotalPaddingBottom() - lp.topMargin - lp.bottomMargin;
    }

    public void removeFromParent() {
        ViewGroup parent = (ViewGroup) textView.getParent();
        if (parent != null) {
            parent.removeView(textView);
        }
    }
}
