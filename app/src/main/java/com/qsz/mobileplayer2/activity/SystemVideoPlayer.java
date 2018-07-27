package com.qsz.mobileplayer2.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.qsz.mobileplayer2.R;
import com.qsz.mobileplayer2.domain.MediaItem;
import com.qsz.mobileplayer2.utils.LogUtil;
import com.qsz.mobileplayer2.utils.Utils;
import com.qsz.mobileplayer2.view.VideoView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * 系统播放器
 * Created by QSZ on 2018/7/9 17:25
 *
 * @author Administrator
 */
public class SystemVideoPlayer extends Activity implements View.OnClickListener {

    private boolean isUseSystem = true;
    /**
     * 视频进度的更新
     */
    private static final int PROGRESS = 1;
    /**
     * 隐藏控制面板
     */
    private static final int HIDE_MEDIACONTROLLER = 2;
    /**
     * 显示的网络速度
     */
    private static final int SHOW_SPEED = 3;
    /**
     * 全屏
     */
    private static final int FULL_SCREEN = 1;
    /**
     * 默认屏幕
     */
    private static final int DEFAULT_SCREEN = 2;
    private VideoView mVideoView;
    private Uri mUri;
    private LinearLayout llTop;
    private TextView tvName;
    private ImageView ivBattery;
    private TextView tvSystemTime;
    private Button btnVoice;
    private SeekBar seekbarVoice;
    private Button btnSwichPlayer;
    private LinearLayout llBottom;
    private RelativeLayout media_controller;
    private TextView tvCurrentTime;
    private SeekBar seekbarVideo;
    private TextView tvDuration;
    private Button btnExit;
    private Button btnVideoPre;
    private Button btnVideoStartPause;
    private Button btnVideoNext;
    private Button btnVideoSiwchScreen;
    private TextView tv_buffer_netspeed;
    private LinearLayout ll_buffer;
    private TextView tv_loading_netspeed;
    private LinearLayout ll_loading;

    private Utils mUtils;
    /**
     * 监听电量变化的广播
     */
    private MyReceiver mReceiver;
    /**
     * 传入进来的视频列表
     */
    private ArrayList<MediaItem> mMediaItems;
    /**
     * 要播放的列表中的具体位置
     */
    private int position;
    /**
     * 1.定义手势识别器
     */
    private GestureDetector mDetector;
    /**
     * 是否显示控制面板
     */
    private boolean isshowMediaController = false;
    /**
     * 是否全屏
     */
    private boolean isFullScreen = false;
    /**
     * 屏幕的宽
     */
    private int screenWidth = 0;
    /**
     * 屏幕的高
     */
    private int screenHeight = 0;
    /**
     * 真实视频的宽
     */
    private int videoWidth;
    /**
     * 真实视频的高
     */
    private int videoHeight;
    /**
     * 调用声音
     */
    private AudioManager am;
    /**
     * 当前的音量
     */
    private int currentVoice;
    /**
     * 0~15
     * 最大音量
     */
    private int maxVoice;
    /**
     * 是否是静音
     */
    private boolean isMute = false;
    /**
     * 是否是网络uri
     */
    private boolean isNetUri;
    /**
     * 上一次的播放进度
     */
    private int precurrentPosition;

    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2018-07-10 16:30:50 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        setContentView(R.layout.activity_system_video_player);
        llTop = findViewById(R.id.ll_top);
        tvName = findViewById(R.id.tv_name);
        ivBattery = findViewById(R.id.iv_battery);
        tvSystemTime = findViewById(R.id.tv_system_time);
        btnVoice = findViewById(R.id.btn_voice);
        seekbarVoice = findViewById(R.id.seekbar_voice);
        btnSwichPlayer = findViewById(R.id.btn_swich_player);
        llBottom = findViewById(R.id.ll_bottom);
        tvCurrentTime = findViewById(R.id.tv_current_time);
        seekbarVideo = findViewById(R.id.seekbar_video);
        tvDuration = findViewById(R.id.tv_duration);
        btnExit = findViewById(R.id.btn_exit);
        btnVideoPre = findViewById(R.id.btn_video_pre);
        btnVideoStartPause = findViewById(R.id.btn_video_start_pause);
        btnVideoNext = findViewById(R.id.btn_video_next);
        btnVideoSiwchScreen = findViewById(R.id.btn_video_siwch_screen);
        mVideoView = findViewById(R.id.videoview);
        media_controller = findViewById(R.id.media_controller);
        tv_buffer_netspeed = findViewById(R.id.tv_buffer_netspeed);
        ll_buffer = findViewById(R.id.ll_buffer);
        tv_loading_netspeed = findViewById(R.id.tv_loading_netspeed);
        ll_loading = findViewById(R.id.ll_loading);

        btnVoice.setOnClickListener(this);
        btnSwichPlayer.setOnClickListener(this);
        btnExit.setOnClickListener(this);
        btnVideoPre.setOnClickListener(this);
        btnVideoStartPause.setOnClickListener(this);
        btnVideoNext.setOnClickListener(this);
        btnVideoSiwchScreen.setOnClickListener(this);

        // 最大音量和SeekBar关联
        seekbarVoice.setMax(maxVoice);
        // 设置当前进度-当前音量
        seekbarVoice.setProgress(currentVoice);

        // 开始更新网络速度
        mHandler.sendEmptyMessage(SHOW_SPEED);
    }

    /**
     * Handle button click events<br />
     * <br />
     * Auto-created on 2018-07-10 16:30:50 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    @Override
    public void onClick(View v) {
        if (v == btnVoice) {
            isMute = !isMute;
            // Handle clicks for btnVoice
            updataVoice(currentVoice, isMute);
        } else if (v == btnSwichPlayer) {
            // Handle clicks for btnSwichPlayer
            showSwichPlayerDialog();
        } else if (v == btnExit) {
            // Handle clicks for btnExit
            finish();
        } else if (v == btnVideoPre) {
            // Handle clicks for btnVideoPre
            playPreVideo();
        } else if (v == btnVideoStartPause) {
            // Handle clicks for btnVideoStartPause
            startAndPause();
        } else if (v == btnVideoNext) {
            // Handle clicks for btnVideoNext
            playNextVideo();
        } else if (v == btnVideoSiwchScreen) {
            // Handle clicks for btnVideoSiwchScreen
            setFullScreenAndDefault();
        }
        mHandler.removeMessages(HIDE_MEDIACONTROLLER);
        mHandler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
    }

    private void showSwichPlayerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("系统播放器提醒您");
        builder.setMessage("当您播放视频，有声音没有画面的时候，请切换万能播放器播放");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startVitamioPlayer();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void startAndPause() {
        if (mVideoView.isPlaying()) {
            // 视频在播放-设置暂停
            mVideoView.pause();
            // 按钮状态设置播放
            btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_start_selector);
        } else {
            // 视频播放
            mVideoView.start();
            // 按钮状态设置暂停
            btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_pause_selector);
        }
    }

    /**
     * 播放上一个视频
     */
    private void playPreVideo() {
        if (mMediaItems != null && mMediaItems.size() > 0) {
            // 播放上一个视频
            position--;
            if (position >= 0) {
                ll_loading.setVisibility(View.VISIBLE);
                MediaItem mediaItem = mMediaItems.get(position);
                tvName.setText(mediaItem.getName());
                isNetUri = mUtils.isNetUri(mediaItem.getData());
                mVideoView.setVideoPath(mediaItem.getData());

                // 设置按钮状态
                setButtonState();
            }
        } else if (mUri != null) {
            // 设置按钮状态-上一个和下一个按钮设置灰色并且不可以点击
            setButtonState();
        }
    }

    /**
     * 播放下一个视频
     */
    private void playNextVideo() {
        if (mMediaItems != null && mMediaItems.size() > 0) {
            // 播放下一个
            position++;
            if (position < mMediaItems.size()) {
                ll_loading.setVisibility(View.VISIBLE);
                MediaItem mediaItem = mMediaItems.get(position);
                tvName.setText(mediaItem.getName());
                isNetUri = mUtils.isNetUri(mediaItem.getData());
                mVideoView.setVideoPath(mediaItem.getData());

                // 设置按钮状态
                setButtonState();
            }
        } else if (mUri != null) {
            // 设置按钮状态-上一个和下一个按钮设置灰色并且不可以点击
            setButtonState();
        }
    }

    private void setButtonState() {
        if (mMediaItems != null && mMediaItems.size() > 0) {
            if (mMediaItems.size() == 1) {
                setEnable(false);
            } else if (mMediaItems.size() == 2) {
                if (position == 0) {
                    btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
                    btnVideoPre.setEnabled(false);

                    btnVideoNext.setBackgroundResource(R.drawable.btn_video_next_selector);
                    btnVideoNext.setEnabled(true);
                } else if (position == mMediaItems.size() - 1) {
                    btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
                    btnVideoNext.setEnabled(false);

                    btnVideoPre.setBackgroundResource(R.drawable.btn_video_pre_selector);
                    btnVideoPre.setEnabled(true);
                }
            } else {
                if (position == 0) {
                    btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
                    btnVideoPre.setEnabled(false);
                } else if (position == mMediaItems.size() - 1) {
                    btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
                    btnVideoNext.setEnabled(false);
                } else {
                    setEnable(true);
                }
            }
        } else if (mUri != null) {
            // 两个按钮设置灰色
            setEnable(false);
        }
    }

    private void setEnable(boolean isEnable) {
        if (isEnable) {
            btnVideoPre.setBackgroundResource(R.drawable.btn_video_pre_selector);
            btnVideoPre.setEnabled(true);
            btnVideoNext.setBackgroundResource(R.drawable.btn_video_next_selector);
            btnVideoNext.setEnabled(true);
        } else {
            // 两个按钮设置灰色
            btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
            btnVideoPre.setEnabled(false);
            btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
            btnVideoNext.setEnabled(false);
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                // 显示网速
                case SHOW_SPEED:
                    // 1.得到网络速度
                    String netSpeed = mUtils.getNetSpeed(SystemVideoPlayer.this);

                    // 显示网络速
                    tv_loading_netspeed.setText("玩命加载中..." + netSpeed);
                    tv_buffer_netspeed.setText("缓存中..." + netSpeed);

                    // 2.每2s更新一次
                    mHandler.removeMessages(SHOW_SPEED);
                    mHandler.sendEmptyMessageDelayed(SHOW_SPEED, 2000);

                    break;
                // 隐藏控制面板
                case HIDE_MEDIACONTROLLER:
                    hideMediaController();
                    break;
                case PROGRESS:

                    // 1.得到当前的视频播放进程/0
                    int currentPosition = mVideoView.getCurrentPosition();

                    // 2.SeekBar.setProgress(当前进度)；
                    seekbarVideo.setProgress(currentPosition);

                    // 更新文本播放进度
                    tvCurrentTime.setText(mUtils.stringForTime(currentPosition));

                    // 设置系统时间
                    tvSystemTime.setText(getSystemTime());

                    // 缓存进度的更新
                    if (isNetUri) {
                        // 只有网络资源才有缓存效果
                        int buffer = mVideoView.getBufferPercentage();
                        int totalBuffer = buffer * seekbarVideo.getMax();
                        int secondaryProgress = totalBuffer / 100;
                        seekbarVideo.setSecondaryProgress(secondaryProgress);
                    } else {
                        // 本地视频没有缓冲效果
                        seekbarVideo.setSecondaryProgress(0);
                    }
                    // 监听卡
                    if (!isUseSystem) {
                        if (mVideoView.isPlaying()) {
                            int buffer = currentPosition - precurrentPosition;
                            if (buffer < 500) {
                                // 视频卡了
                                ll_buffer.setVisibility(View.VISIBLE);
                            } else {
                                // 视频不卡了
                                ll_buffer.setVisibility(View.GONE);
                            }
                        } else {
                            ll_buffer.setVisibility(View.GONE);
                        }
                    }
                    precurrentPosition = currentPosition;

                    // 3.每秒更新一次
                    mHandler.removeMessages(PROGRESS);
                    mHandler.sendEmptyMessageDelayed(PROGRESS, 1000);
                    break;
                default:
            }
        }
    };

    /**
     * 得到系统时间
     */
    public String getSystemTime() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.e("onCreate---");

        initData();
        findViews();

        setListener();

        getData();

        setData();
    }

    private void setData() {
        if (mMediaItems != null && mMediaItems.size() > 0) {
            MediaItem mediaItem = mMediaItems.get(position);
            // 设置视频的名称
            tvName.setText(mediaItem.getName());
            isNetUri = mUtils.isNetUri(mediaItem.getData());
            mVideoView.setVideoPath(mediaItem.getData());
        } else if (mUri != null) {
            // 设置视频的名称
            tvName.setText(mUri.toString());
            isNetUri = mUtils.isNetUri(mUri.toString());
            mVideoView.setVideoURI(mUri);
        } else {
            Toast.makeText(SystemVideoPlayer.this, "美女你没有传递数据", Toast.LENGTH_SHORT).show();
        }
        setButtonState();
    }

    private void getData() {
        // 得到播放地址
        // 文件夹，图片浏览器，QQ空间
        mUri = getIntent().getData();
        mMediaItems = (ArrayList<MediaItem>) getIntent().getSerializableExtra("videolist");
        position = getIntent().getIntExtra("position", 0);
    }

    private void initData() {
        mUtils = new Utils();
        // 注册电量广播
        mReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        // 当电量变化的时候发送这个广播
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mReceiver, intentFilter);

        // 2. 实例化手势识别器，并 且重写双击，点击，长按
        mDetector = new GestureDetector(this, new MySimpleOnGestureListener());
        // 得到屏幕的宽和高
        /**
         * 方式1（过时的方法）
         *  screenWidth = getWindowManager().getDefaultDisplay().getWidth();
         *  screenHeight = getWindowManager().getDefaultDisplay().getHeight();
         * */
        // 方式2：
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;

        // 得到音量
        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        assert am != null;
        currentVoice = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVoice = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    private class MySimpleOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
            // Toast.makeText(SystemVideoPlayer.this, "我被长按了", Toast.LENGTH_SHORT).show();
            startAndPause();
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            setFullScreenAndDefault();
            // Toast.makeText(SystemVideoPlayer.this, "我被长按了", Toast.LENGTH_SHORT).show();
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            // Toast.makeText(SystemVideoPlayer.this, "我被长按了", Toast.LENGTH_SHORT).show();
            if (isshowMediaController) {
                // 隐藏
                hideMediaController();
                // 把隐藏消息移除
                mHandler.removeMessages(HIDE_MEDIACONTROLLER);
            } else {
                // 显示
                showMediaController();
                // 发消息隐藏
                mHandler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
            }
            return super.onSingleTapConfirmed(e);
        }
    }

    private void setFullScreenAndDefault() {
        if (isFullScreen) {
            // 默认
            setVideoType(DEFAULT_SCREEN);
        } else {
            // 全屏
            setVideoType(FULL_SCREEN);
        }
    }

    private void setVideoType(int defaultScreen) {
        switch (defaultScreen) {
            // 全屏
            case FULL_SCREEN:
                // 设置视频画面的大小-屏幕有多大就是多大
                mVideoView.setVideoSize(screenWidth, screenHeight);
                // 设置按钮的状态-默认
                btnVideoSiwchScreen.setBackgroundResource(R.drawable.btn_video_siwch_screen_default_selector);
                isFullScreen = true;
                break;
            // 默认
            case DEFAULT_SCREEN:
                // 1.设置视频画面的大小
                // 视频真实的宽和高
                int mVideoWidth = videoWidth;
                int mVideoHeight = videoHeight;

                // 屏幕的宽和高
                int width = screenWidth;
                int height = screenHeight;

                if (mVideoWidth * height < width * mVideoHeight) {
                    width = height * mVideoWidth / mVideoHeight;
                } else if (mVideoWidth * height > width * mVideoHeight) {
                    height = width * mVideoHeight / mVideoWidth;
                }
                mVideoView.setVideoSize(width, height);
                // 2.设置按钮的状态-全屏
                btnVideoSiwchScreen.setBackgroundResource(R.drawable.btn_video_siwch_screen_full_selector);
                isFullScreen = false;
                break;
            default:
        }
    }

    class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // 0~100
            int level = intent.getIntExtra("level", 0);
            // 主线程
            setBattery(level);
        }
    }

    private void setBattery(int level) {
        if (level <= 0) {
            ivBattery.setImageResource(R.drawable.ic_battery_0);
        } else if (level <= 10) {
            ivBattery.setImageResource(R.drawable.ic_battery_10);
        } else if (level <= 20) {
            ivBattery.setImageResource(R.drawable.ic_battery_20);
        } else if (level <= 40) {
            ivBattery.setImageResource(R.drawable.ic_battery_40);
        } else if (level <= 60) {
            ivBattery.setImageResource(R.drawable.ic_battery_60);
        } else if (level <= 80) {
            ivBattery.setImageResource(R.drawable.ic_battery_80);
        } else if (level <= 100) {
            ivBattery.setImageResource(R.drawable.ic_battery_100);
        } else {
            ivBattery.setImageResource(R.drawable.ic_battery_100);
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private void setListener() {
        // 准备好的监听
        mVideoView.setOnPreparedListener(new MyOnPreparedListener());

        // 播放出错了的监听
        mVideoView.setOnErrorListener(new MyOnErrorListener());

        // 播放完成了的监听
        mVideoView.setOnCompletionListener(new MyOnCompletionListener());

        // 设置Seekbar状态变化的监听
        seekbarVideo.setOnSeekBarChangeListener(new VideoOnSeekBarChangeListener());

        seekbarVoice.setOnSeekBarChangeListener(new VoiceOnSeekBarChangeListener());

        if (isUseSystem) {
            // 监听视频播放卡-系统的api
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                mVideoView.setOnInfoListener(new MyOnInfoListener());
            }
        }
    }

    class MyOnInfoListener implements MediaPlayer.OnInfoListener {

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            switch (what) {
                // 视频卡了，拖动卡
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    ll_buffer.setVisibility(View.VISIBLE);
                    break;
                // 视频卡结束了，拖动卡 结束了
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                    ll_buffer.setVisibility(View.GONE);
                    break;
                default:
            }
            return true;
        }
    }

    class VoiceOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                if (progress > 0) {
                    isMute = false;
                } else {
                    isMute = true;
                }
                updataVoice(progress, isMute);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mHandler.removeMessages(HIDE_MEDIACONTROLLER);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mHandler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
        }
    }

    /**
     * 设置音量大小
     */
    private void updataVoice(int progress, boolean isMute) {
        if (isMute) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
            seekbarVoice.setProgress(0);
        } else {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            seekbarVoice.setProgress(progress);
            currentVoice = progress;
        }
    }

    class VideoOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        /**
         * 当手滑动的时候，会引起SeekBar进度变化，会回调这个方法
         * <p>
         * 如果是用户引起的true，不是false
         */
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                mVideoView.seekTo(progress);
            }
        }

        /**
         * 当手指触碰的时候调用回调这个方法
         */
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mHandler.removeMessages(HIDE_MEDIACONTROLLER);
        }

        /**
         * 当手指离开的时候回调这个方法
         */
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mHandler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
        }
    }

    class MyOnPreparedListener implements MediaPlayer.OnPreparedListener {

        /**
         * 当底层解码准备好的时候
         */
        @Override
        public void onPrepared(MediaPlayer mp) {
            videoWidth = mp.getVideoWidth();
            videoHeight = mp.getVideoHeight();
            // 开始播放
            mVideoView.start();
            // 1.视频的总时长，关联总长度
            int duration = mVideoView.getDuration();
            seekbarVideo.setMax(duration);
            tvDuration.setText(mUtils.stringForTime(duration));

            // 默认是隐藏控制面板
            hideMediaController();
            // 2.发消息
            mHandler.sendEmptyMessage(PROGRESS);
            // 屏幕的默认播放
            setVideoType(DEFAULT_SCREEN);
            // 把加载页面消失掉
            ll_loading.setVisibility(View.GONE);
        }
    }

    class MyOnErrorListener implements MediaPlayer.OnErrorListener {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            // 1.播放的视频格式不支持--跳转到万能播放器继续播放
            startVitamioPlayer();
            // 2.播放网络视频的时候，网络中断---1.如果网络确实断了，可以提示用于网络断了：2.网络断断续续的，重新播放
            // 3.播放的时候本地文件中间有空白---下载做完成
            return true;
        }
    }

    /**
     * a,把数据按照原样传入VtaimoVideoPlayer播放器
     * b,关闭系统播放器
     */
    private void startVitamioPlayer() {
        if (mVideoView != null) {
            mVideoView.stopPlayback();
        }

        Intent intent = new Intent(this, VitamioVideoPlayer.class);
        if (mMediaItems != null && mMediaItems.size() > 0) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("videolist", mMediaItems);
            intent.putExtras(bundle);
            intent.putExtra("position", position);
        } else if (mUri != null) {
            intent.setData(mUri);
        }
        startActivity(intent);
        // 关闭页面
        finish();
    }

    class MyOnCompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            playNextVideo();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        LogUtil.e("onRestart--");
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtil.e("onStart--");
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.e("onResume--");
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtil.e("onPause--");
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtil.e("onStop--");
    }

    @Override
    protected void onDestroy() {

        // 移除所有的消息
        mHandler.removeCallbacksAndMessages(null);

        // 释放资源的时候 ，先释放子类，在释放父类
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        LogUtil.e("onDestroy--");
        super.onDestroy();
    }

    private float startY;
    private float startX;
    /**
     * 屏幕的高
     */
    private float touchRang;
    /**
     * 当-按下的音量
     */
    private int mVol;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 3. 把事件传递给手势识别器
        mDetector.onTouchEvent(event);
        switch (event.getAction()) {
            // 手指按下
            case MotionEvent.ACTION_DOWN:
                // 1.按下记录值
                startX = event.getX();
                startY = event.getY();
                mVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                touchRang = Math.min(screenHeight, screenWidth);
                mHandler.removeMessages(HIDE_MEDIACONTROLLER);
                break;
            // 手指移动
            case MotionEvent.ACTION_MOVE:
                // 2.移动的记录相关值
                float endY = event.getY();
                float endX = event.getX();
                float distanceY = startY - endY;

                if (endX < screenWidth / 2) {
                    // 左边屏幕-调节亮度
                    final double FLING_MIN_DISTANCE = 0.5;
                    final double FLING_MIN_VELOCITY = 0.5;
                    if (distanceY > FLING_MIN_DISTANCE && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
                        setBrightness(20);
                    }
                    if (distanceY < FLING_MIN_DISTANCE && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
                        setBrightness(-20);
                    }
                } else {
                    // 右边屏幕-调节声音
                    // 改变声音 =(滑动屏幕的距离 / 总距离)*音量最大值
                    float delta = (distanceY / touchRang) * maxVoice;
                    // 最终声音 = 原来的 + 改变声音
                    int voice = (int) Math.min(Math.max(mVol + delta, 0), maxVoice);
                    if (delta != 0) {
                        isMute = false;
                        updataVoice(voice, isMute);
                    }
                }
                break;
            // 手指离开
            case MotionEvent.ACTION_UP:
                mHandler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
                break;
            default:
        }
        return super.onTouchEvent(event);
    }

    private Vibrator mVibrator;

    /**
     * 设置屏幕亮度 lp = 0全暗，1p = -1，根据系统设置，1p = 1；最亮
     */
    private void setBrightness(float brightness) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = lp.screenBrightness + brightness / 255.0f;
        if (lp.screenBrightness > 1) {
            lp.screenBrightness = 1;
            mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] pattern = {10, 200};
            assert mVibrator != null;
            mVibrator.vibrate(pattern, -1);
        } else if (lp.screenBrightness < 0.2) {
            lp.screenBrightness = (float) 0.2;
            mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] pattern = {10, 200};
            assert mVibrator != null;
            mVibrator.vibrate(pattern, -1);
        }
        getWindow().setAttributes(lp);
    }

    /**
     * 显示控制面板
     */
    private void showMediaController() {
        media_controller.setVisibility(View.VISIBLE);
        isshowMediaController = true;
    }

    /**
     * 隐藏控制面板
     */
    private void hideMediaController() {
        media_controller.setVisibility(View.GONE);
        isshowMediaController = false;
    }

    /**
     * 监听物理键，实现声音的调节大小
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            currentVoice--;
            updataVoice(currentVoice, false);
            mHandler.removeMessages(HIDE_MEDIACONTROLLER);
            mHandler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            currentVoice++;
            updataVoice(currentVoice, false);
            mHandler.removeMessages(HIDE_MEDIACONTROLLER);
            mHandler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
