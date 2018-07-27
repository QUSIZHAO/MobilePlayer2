package com.qsz.mobileplayer2.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.media.audiofx.Visualizer;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by QSZ on 2018/7/17 10:12
 *
 * @author QSZ
 */
public class BaseVisualizerView extends View implements Visualizer.OnDataCaptureListener {

    private static final int DN_W = 480;
    private static final int DN_H = 160;
    private static final int DN_SL = 14;
    private static final int DN_SW = 6;

    private int hgap = 0;
    private int vgap = 0;
    private int levelStep = 0;
    private float strokeWidth = 0;
    private float strokeLength = 0;

    /**
     * 最大的水平
     * */
    protected final static int MAX_LEVEL = 13;

    protected final static int CYLINDER_NUM = 20;

    protected Visualizer mVisualizer = null;

    protected Paint mPaint = null;

    protected byte[] mData = new byte[CYLINDER_NUM];

    boolean mDataEn = true;

    public BaseVisualizerView(Context context) {
        super(context);

        initView();
    }

    private void initView() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLUE);
        mPaint.setStrokeJoin(Join.ROUND);
        mPaint.setStrokeCap(Cap.ROUND);
    }

    public BaseVisualizerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        float w, h, xr, yr;
        w = right - left;
        h = bottom - top;
        xr = w / (float) DN_W;
        yr = h / (float) DN_H;

        strokeWidth = DN_SW * yr;
        strokeLength = DN_SL * xr;
        hgap = (int) ((w - strokeLength * CYLINDER_NUM) / (CYLINDER_NUM + 1));
        vgap = (int) (h / (MAX_LEVEL + 2));

        mPaint.setStrokeWidth(strokeWidth);
    }

    protected void drawCylinder(Canvas canvas, float x, byte value) {
        if (value < 0) {
            value = 0;
        }
        for (int i = 0; i < value; i++) {
            float y = getHeight() - i * vgap - vgap;
            canvas.drawLine(x, y, x + strokeLength, y, mPaint);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < CYLINDER_NUM; i++) {
            drawCylinder(canvas, strokeWidth / 2 + hgap + i * (hgap + strokeLength), mData[i]);
        }
    }

    /**
     * 它设置了视图的可视化工具。当退出程序时，请将viaulizer设置为null
     * */
    public void setVisualizer(Visualizer visualizer) {
        if (visualizer != null) {
            if (!visualizer.getEnabled()) {
                visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[0]);
            }
            levelStep = 128 / MAX_LEVEL;
            visualizer.setDataCaptureListener(this, Visualizer.getMaxCaptureRate() / 2, false, true);
        } else {
            if (mVisualizer != null) {
                mVisualizer.setEnabled(false);
                mVisualizer.release();
            }
        }
        mVisualizer = visualizer;
    }

    /**
     * 波形数据采集
     * */
    @Override
    public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {

    }

    @Override
    public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
        byte[] model = new byte[fft.length / 2 + 1];
        if (mDataEn) {
            model[0] = (byte) Math.abs(fft[1]);
            int j = 1;
            for (int i = 2; i < fft.length; ) {
                model[j] = (byte) Math.hypot(fft[i], fft[i + 1]);
                i += 2;
                j++;
            }
        } else {
            for (int i = 0; i < CYLINDER_NUM; i++) {
                model[i] = 0;
            }
        }
        for (int i = 0; i < CYLINDER_NUM; i++) {
            final byte a = (byte) (Math.abs(model[CYLINDER_NUM - i]) / levelStep);

            final byte b = mData[i];
            if (a > b) {
                mData[i] = a;
            } else {
                if (b > 0) {
                    mData[i]--;
                }
            }
        }
        postInvalidate();
    }

    /**
     * 启用或禁用数据处理程序
     * */
    public void enableDataProcess(boolean en) {
        mDataEn = en;
    }
}
