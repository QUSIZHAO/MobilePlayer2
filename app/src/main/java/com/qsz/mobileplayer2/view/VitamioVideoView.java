package com.qsz.mobileplayer2.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import io.vov.vitamio.widget.VideoView;

/**
 * 自定义VitamioVideoView
 * Created by QSZ on 2018/7/10 15:52
 *
 * @author QSZ
 */
public class VitamioVideoView extends VideoView{

    /**
     * 在代码中创建的时候一般用这个方法
     */
    public VitamioVideoView(Context context) {
        this(context, null);
    }

    /**
     * 当这个类在布局文件的时候，系统通过该构造方法实例化该类
     */
    public VitamioVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * 当需要设置样式的时候调用该方法
     */
    public VitamioVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 设置视频的宽和高
     */
    public void setVideoSize(int videoWidth, int videoHeight) {
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = videoWidth;
        params.height = videoHeight;
        setLayoutParams(params);
    }
}
