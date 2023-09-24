package com.example.simpleocr.Views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

/**
 * @author 30415
 */
public class ScanView extends View {

    private Paint mLinePaint;
    private RectF mFrameRect;

    public ScanView(Context context) {
        super(context);
        init();
    }

    public ScanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mLinePaint = new Paint();
        mLinePaint.setColor(Color.WHITE);
        mLinePaint.setStrokeWidth(16);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setAntiAlias(true);

        float mFrameWidth = getResources().getDisplayMetrics().widthPixels / 1.5f;
        float mFrameHeight = getResources().getDisplayMetrics().widthPixels / 1.5f;
        float left = (getResources().getDisplayMetrics().widthPixels - mFrameWidth) / 2;
        float top = (getResources().getDisplayMetrics().heightPixels - mFrameHeight) / 2 - dpToPx();
        mFrameRect = new RectF(left, top, left + mFrameWidth, top + mFrameHeight);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        float frameLeft = mFrameRect.left;
        float frameTop = mFrameRect.top;
        float frameRight = mFrameRect.right;
        float frameBottom = mFrameRect.bottom;

        float cornerSize = 60f;

        // 左上角
        @SuppressLint("DrawAllocation") Path path = new Path();
        path.moveTo(frameLeft, frameTop + cornerSize);
        path.lineTo(frameLeft, frameTop);
        path.lineTo(frameLeft + cornerSize, frameTop);
        canvas.drawPath(path, mLinePaint);

        // 右上角
        path.reset();
        path.moveTo(frameRight - cornerSize, frameTop);
        path.lineTo(frameRight, frameTop);
        path.lineTo(frameRight, frameTop + cornerSize);
        canvas.drawPath(path, mLinePaint);

        // 右下角
        path.reset();
        path.moveTo(frameRight, frameBottom - cornerSize);
        path.lineTo(frameRight, frameBottom);
        path.lineTo(frameRight - cornerSize, frameBottom);
        canvas.drawPath(path, mLinePaint);

        // 左下角
        path.reset();
        path.moveTo(frameLeft + cornerSize, frameBottom);
        path.lineTo(frameLeft, frameBottom);
        path.lineTo(frameLeft, frameBottom - cornerSize);
        canvas.drawPath(path, mLinePaint);
    }

    private int dpToPx() {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(100 * density);
    }
}