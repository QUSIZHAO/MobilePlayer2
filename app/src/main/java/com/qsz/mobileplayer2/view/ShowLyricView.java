package com.qsz.mobileplayer2.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

import com.qsz.mobileplayer2.domain.Lyric;
import com.qsz.mobileplayer2.utils.LogUtil;
import com.qsz.mobileplayer2.utils.DensityUtil;

import java.util.ArrayList;

/**
 * 自定义歌词显示控件
 * Created by QSZ on 2018/7/17 10:26
 *
 * @author QSZ
 */
@SuppressLint("AppCompatCustomView")
public class ShowLyricView extends TextView {

    /**
     * 歌词列表
     */
    private ArrayList<Lyric> mLyrics;
    private Paint mPaint;
    private Paint whitepaint;

    private int width;
    private int height;
    /**
     * 歌词列表中的索引，是第几局歌词
     */
    private int index;
    /**
     * 每行的高
     */
    private float textHeight;
    /**
     * 当前播放进度
     */
    private float currentPosition;
    /**
     * 高亮显示时间或者休眠时间
     */
    private float sleepTime;
    /**
     * 时间戳，什么时刻到高亮哪句歌词
     */
    private float timePoint;

    /**
     * 设置歌词列表
     */
    public void setLyrics(ArrayList<Lyric> lyrics) {
        this.mLyrics = lyrics;
    }

    public ShowLyricView(Context context) {
        this(context, null);
    }

    public ShowLyricView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShowLyricView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    private void initView(Context context) {
        // 对应的偶数
        textHeight = DensityUtil.dip2px(context, 18);
        LogUtil.e("textHeight==" + textHeight);
        // 创建画笔
        mPaint = new Paint();
        mPaint.setColor(Color.GREEN);
        mPaint.setTextSize(DensityUtil.dip2px(context, 16));
        mPaint.setAntiAlias(true);
        // 设置居中对齐
        mPaint.setTextAlign(Paint.Align.CENTER);

        whitepaint = new Paint();
        whitepaint.setColor(Color.WHITE);
        whitepaint.setTextSize(DensityUtil.dip2px(context, 16));
        whitepaint.setAntiAlias(true);
        // 设置居中对齐
        whitepaint.setTextAlign(Paint.Align.CENTER);

//        lyrics = new ArrayList<>();
//        Lyric lyric = new Lyric();
//        for (int i = 0; i < 1000; i++) {
//
//            lyric.setTimePoint(1000 * i);
//            lyric.setSleepTime(1500 + i);
//            lyric.setContent(i + "aaaaaaaaaaaaaaa" + i);
//            //把歌词添加到集合中
//            lyrics.add(lyric);
//            lyric = new Lyric();
//        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mLyrics != null && mLyrics.size() > 0) {
            // 往上推移
            float plush = 0;
            if (sleepTime == 0) {
                plush = 0;
            } else {
                //平移
                //这一句所花的时间 ：休眠时间 = 移动的距离 ： 总距离（行高）
                //移动的距离 =  (这一句所花的时间 ：休眠时间)* 总距离（行高）
//                float delta = ((currentPositon-timePoint)/sleepTime )*textHeight;

                //屏幕的的坐标 = 行高 + 移动的距离
                plush = textHeight + ((currentPosition - timePoint) / sleepTime) * textHeight;
            }
            canvas.translate(0, -plush);
            // 绘制歌词：绘制当前句
            String currentText = mLyrics.get(index).getContent();
            canvas.drawText(currentText, width / 2, height / 2, mPaint);
            // 绘制前面部分
            // Y轴的中间坐标
            float tempY = height / 2;
            for (int i = index - 1; i >= 0; i--) {
                // 每一句歌词
                String preContent = mLyrics.get(i).getContent();
                tempY = tempY - textHeight;
                if (tempY < 0) {
                    break;
                }
                canvas.drawText(preContent, width / 2, tempY, whitepaint);
            }
            // 绘制后面部分
            // Y轴中间坐标
            tempY = height / 2;
            for (int i = index + 1; i < mLyrics.size(); i++) {
                // 每一句歌词
                String nextContent = mLyrics.get(i).getContent();
                tempY = tempY + textHeight;
                if (tempY > height) {
                    break;
                }
                canvas.drawText(nextContent, width / 2, tempY, whitepaint);
            }
        } else {
            // 没有歌词
            canvas.drawText("没有歌词", width / 2, height / 2, mPaint);
        }
    }

    /**
     * 根据当前播放的位置，找出该高亮显示哪句歌词
     */
    public void setshowNextLyric(int currentPosition) {
        this.currentPosition = currentPosition;
        if (mLyrics == null || mLyrics.size() == 0) {
            return;
        }
        for (int i = 1; i < mLyrics.size(); i++) {
            if (currentPosition < mLyrics.get(i).getTimePoint()) {
                int tempIndex = i - 1;
                if (currentPosition >= mLyrics.get(tempIndex).getTimePoint()) {
                    // 当前正在播放的哪句歌词
                    index = tempIndex;
                    sleepTime = mLyrics.get(index).getSleepTime();
                    timePoint = mLyrics.get(index).getTimePoint();
                }
            }
        }
        // 重新绘制
        // 在主线程中
        invalidate();
        // 子线程
        // postInvalidate();
    }
}
