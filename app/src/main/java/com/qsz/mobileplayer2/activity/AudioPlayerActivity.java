package com.qsz.mobileplayer2.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.qsz.mobileplayer2.IMusicPlayerService;
import com.qsz.mobileplayer2.R;
import com.qsz.mobileplayer2.domain.MediaItem;
import com.qsz.mobileplayer2.service.MusicPlayerService;
import com.qsz.mobileplayer2.utils.LyricUtils;
import com.qsz.mobileplayer2.utils.Utils;
import com.qsz.mobileplayer2.view.BaseVisualizerView;
import com.qsz.mobileplayer2.view.ShowLyricView;

import java.io.File;

import de.greenrobot.event.EventBus;

/**
 * 音乐播放
 * Created by QSZ on 2018/7/17 09:48
 *
 * @author QSZ
 */
public class AudioPlayerActivity extends Activity implements View.OnClickListener {

    /**
     * 更新进度
     */
    private static final int PROGRESS = 1;
    /**
     * 显示歌词
     */
    private static final int SHOW_LYRIC = 2;
    private int position;
    /**
     * true:从状态栏进入的，不需要重新播放
     * false：从播放列表进入的
     */
    private boolean notification;
    // 服务的代理类，通过它可以调用服务的方法
    private IMusicPlayerService mService;
    private ImageView ivIcon;
    private TextView tvArtist;
    private TextView tvName;
    private TextView tvTime;
    private SeekBar seekbarAudio;
    private Button btnAudioPlaymode;
    private Button btnAudioPre;
    private Button btnAudioStartPause;
    private Button btnAudioNext;
    private Button btnLyrc;
    private ShowLyricView showLyricView;
    private BaseVisualizerView mBaseVisualizerView;

    private MyReceiver mMyReceiver;
    private Utils mUtils;

    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2018-07-17 10:29:37 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        setContentView(R.layout.activity_audioplayer);

        ivIcon = findViewById(R.id.iv_icon);
        ivIcon.setBackgroundResource(R.drawable.animation_list);
        AnimationDrawable rocketAnimation = (AnimationDrawable) ivIcon.getBackground();
        rocketAnimation.start();
        tvArtist = findViewById(R.id.tv_artist);
        tvName = findViewById(R.id.tv_name);
        tvTime = findViewById(R.id.tv_time);
        seekbarAudio = findViewById(R.id.seekbar_audio);
        btnAudioPlaymode = findViewById(R.id.btn_audio_playmode);
        btnAudioPre = findViewById(R.id.btn_audio_pre);
        btnAudioStartPause = findViewById(R.id.btn_audio_start_pause);
        btnAudioNext = findViewById(R.id.btn_audio_next);
        btnLyrc = findViewById(R.id.btn_lyrc);
        showLyricView = findViewById(R.id.showLyricView);
        mBaseVisualizerView = findViewById(R.id.baseVisualizerView);

        btnAudioPlaymode.setOnClickListener(this);
        btnAudioPre.setOnClickListener(this);
        btnAudioStartPause.setOnClickListener(this);
        btnAudioNext.setOnClickListener(this);
        btnLyrc.setOnClickListener(this);

        // 设置视频的拖动
        seekbarAudio.setOnSeekBarChangeListener(new MyOnSeekBarChangeListener());
    }

    class MyOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                // 拖动进度
                try {
                    mService.seekTo(progress);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    /**
     * Handle button click events<br />
     * <br />
     * Auto-created on 2018-07-17 10:29:37 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    @Override
    public void onClick(View v) {
        if (v == btnAudioPlaymode) {
            // Handle clicks for btnAudioPlaymode
            setPlaymode();
        } else if (v == btnAudioPre) {
            // Handle clicks for btnAudioPre
            if (mService != null) {
                try {
                    mService.pre();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else if (v == btnAudioStartPause) {
            // Handle clicks for btnAudioStartPause
            if (mService != null) {
                try {
                    if (mService.isPlaying()) {
                        // 暂停
                        mService.pause();
                        // 按钮-播放
                        btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_start_selector);
                    } else {
                        // 播放
                        mService.start();
                        // 按钮-暂停
                        btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_pause_selector);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else if (v == btnAudioNext) {
            // Handle clicks for btnAudioNext
            if (mService != null) {
                try {
                    mService.next();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else if (v == btnLyrc) {
            // Handle clicks for btnLyrc
        }
    }

    private void setPlaymode() {
        try {
            int playmode = mService.getPlayMode();
            if (playmode == MusicPlayerService.REPEAT_NORMAL) {
                playmode = MusicPlayerService.REPEAT_SINGLE;
            } else if (playmode == MusicPlayerService.REPEAT_SINGLE) {
                playmode = MusicPlayerService.REPEAT_ALL;
            } else if (playmode == MusicPlayerService.REPEAT_ALL) {
                playmode = MusicPlayerService.REPEAT_NORMAL;
            } else {
                playmode = MusicPlayerService.REPEAT_NORMAL;
            }
            // 保持
            mService.setPlayMode(playmode);
            // 设置图片
            showPlaymode();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void showPlaymode() {
        try {
            int playmode = mService.getPlayMode();
            if (playmode == MusicPlayerService.REPEAT_NORMAL) {
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);
                Toast.makeText(AudioPlayerActivity.this, "顺序播放", Toast.LENGTH_SHORT).show();
            } else if (playmode == MusicPlayerService.REPEAT_SINGLE) {
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_single_selector);
                Toast.makeText(AudioPlayerActivity.this, "单曲循环", Toast.LENGTH_SHORT).show();
            } else if (playmode == MusicPlayerService.REPEAT_ALL) {
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_all_selector);
                Toast.makeText(AudioPlayerActivity.this, "全部循环", Toast.LENGTH_SHORT).show();
            } else {
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);
                Toast.makeText(AudioPlayerActivity.this, "顺序播放", Toast.LENGTH_SHORT).show();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 效验状态
     */
    private void checkPlaymode() {
        try {
            int playmode = mService.getPlayMode();

            if (playmode == MusicPlayerService.REPEAT_NORMAL) {
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);
            } else if (playmode == MusicPlayerService.REPEAT_SINGLE) {
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_single_selector);
            } else if (playmode == MusicPlayerService.REPEAT_ALL) {
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_all_selector);
            } else {
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);
            }

            // 效验播放和暂停的按钮
            if (mService.isPlaying()) {
                btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_start_selector);
            } else {
                btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_pause_selector);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                // 显示歌词
                case SHOW_LYRIC:
                    // 1.得到当前的进度
                    try {
                        int currentPosition = mService.getCurrentPosition();
                        // 2.把进度传入ShowLyricView控件，并且计算该高度哪一句
                        showLyricView.setshowNextLyric(currentPosition);
                        // 3.实时的发消息
                        mHandler.removeMessages(SHOW_LYRIC);
                        mHandler.sendEmptyMessage(SHOW_LYRIC);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case PROGRESS:
                    try {
                        // 1.得到当前进度
                        int currentPosition = mService.getCurrentPosition();

                        // 2.设置SeekBar.setProgress(进度)
                        seekbarAudio.setProgress(currentPosition);

                        //3.时间进度更新
                        tvTime.setText(mUtils.stringForTime(currentPosition) + "/" + mUtils.stringForTime(mService.getDuration()));

                        // 4.每秒更新一次
                        mHandler.removeMessages(PROGRESS);
                        mHandler.sendEmptyMessageDelayed(PROGRESS, 1000);

                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        findViews();
        getData();
        bindAndStartService();
    }

    private void initData() {
        mUtils = new Utils();
        //        //注册广播
//        receiver = new MyReceiver();
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(MusicPlayerService.OPENAUDIO);
//        registerReceiver(receiver, intentFilter);

        //1.EventBus注册,this是当前类
        EventBus.getDefault().register(this);
    }

    private ServiceConnection con = new ServiceConnection() {

        /**
         * 当连接成功的时候回调这个方法
         * */
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            mService = IMusicPlayerService.Stub.asInterface(iBinder);

            if (mService != null) {
                try {
                    // 从列表
                    if (!notification) {
                        mService.openAudio(position);
                    } else {
                        System.out.println("onServiceConnected==Thread-name==" +
                                Thread.currentThread().getName());
                        showViewData();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 当断开连接的时候调这个方法
         * */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            try {
                if (mService != null) {
                    mService.stop();
                    mService = null;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            showData(null);
        }
    }

    //3.订阅方法
//    @Subscribe(threadMode = ThreadMode.MAIN,sticky = false,priority = 0)
    public void showData(MediaItem mediaItem) {
        // 发消息开始歌词同步
        showLyric();
        showViewData();
        checkPlaymode();
        setupVisualizerFxAndUi();
    }

    public void onEventMainThread(MediaItem mediaItem) {
        showLyric();
        showViewData();
        checkPlaymode();
        setupVisualizerFxAndUi();
    }

    private Visualizer mVisualizer;

    /**
     * 生成一个VisualizerView对象，使音频频谱的波段能够反映到VisualizerView上
     */
    private void setupVisualizerFxAndUi() {
        try {
            int audioSessionid = mService.getAudioSessionId();
            System.out.println("audioSessionid==" + audioSessionid);
            mVisualizer = new Visualizer(audioSessionid);
            // 参数内必须是2的位数
            mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
            // 设置允许波形表示，并且捕获它
            mBaseVisualizerView.setVisualizer(mVisualizer);
            mVisualizer.setEnabled(true);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void showLyric() {
        // 解析歌词
        LyricUtils lyricUtils = new LyricUtils();

        try {
            // 得到歌曲的绝对路径
            // 传歌词文件
            String path = mService.getAudioPath();
            path = path.substring(0, path.lastIndexOf("."));
            File file = new File(path + ".lrc");
            if (!file.exists()) {
                file = new File(path + ".txt");
            }
            // 解析歌词
            lyricUtils.readLyricFile(file);
            showLyricView.setLyrics(lyricUtils.getLyrics());
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        if (lyricUtils.isExistsLyric()) {
            mHandler.sendEmptyMessage(SHOW_LYRIC);
        }
    }

    private void showViewData() {
        try {
            tvArtist.setText(mService.getArtist());
            tvName.setText(mService.getName());
            // 设置进度条的最大值
            seekbarAudio.setMax(mService.getDuration());
            // 发消息
            mHandler.sendEmptyMessage(PROGRESS);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void bindAndStartService() {
        Intent intent = new Intent(this, MusicPlayerService.class);
        intent.setAction("com.qsz.mobileplayer_OPENAUDIO");
        bindService(intent, con, Context.BIND_AUTO_CREATE);
        // 不至于实例化多个服务
        startService(intent);
    }

    /**
     * 得到数据
     */
    private void getData() {
        notification = getIntent().getBooleanExtra("notification", false);
        if (!notification) {
            position = getIntent().getIntExtra("position", 0);
        }
    }

    @Override
    protected void onDestroy() {

        mHandler.removeCallbacksAndMessages(null);
        //        //取消注册广播
//        if(receiver != null){
//            unregisterReceiver(receiver);
//            receiver = null;
//        }

        //2.EventBus取消注册
        EventBus.getDefault().unregister(this);

        // 解绑服务
        if (con != null) {
            unbindService(con);
            con = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mVisualizer != null) {
            mVisualizer.release();
        }
    }
}
