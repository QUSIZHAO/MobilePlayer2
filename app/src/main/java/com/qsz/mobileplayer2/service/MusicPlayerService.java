package com.qsz.mobileplayer2.service;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.qsz.mobileplayer2.IMusicPlayerService;
import com.qsz.mobileplayer2.R;
import com.qsz.mobileplayer2.activity.AudioPlayerActivity;
import com.qsz.mobileplayer2.domain.MediaItem;
import com.qsz.mobileplayer2.utils.CacheUtils;

import java.io.IOException;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by QSZ on 2018/7/16 12:34
 *
 * @author QSZ
 */
public class MusicPlayerService extends Service {

    public static final String OPEAUDIO = "com.qsz.mobileplayer_OPENAUDIO";
    private ArrayList<MediaItem> mMediaItems;
    private int position;

    /**
     * 播放音乐
     */
    private MediaPlayer mMediaPlayer;
    /**
     * 当前播放的音频文件对象
     */
    private MediaItem mMediaItem;
    /**
     * 顺序播放
     */
    public static final int REPEAT_NORMAL = 1;
    /**
     * 单曲循环
     */
    public static final int REPEAT_SINGLE = 2;
    /**
     * 全部循环
     */
    public static final int REPEAT_ALL = 3;
    /**
     * 播放模式
     */
    private int playmode = REPEAT_NORMAL;

    @Override
    public void onCreate() {
        super.onCreate();
        playmode = CacheUtils.getPlaymode(this, "playmode");
        // 加载音乐列表
        getDataFromLocal();
    }

    public void getDataFromLocal() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                mMediaItems = new ArrayList<>();
                ContentResolver resolver = getContentResolver();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] objs = {
                        // 视频文件在sdcard的名称
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        // 视频的总时长
                        MediaStore.Audio.Media.DURATION,
                        // 视频的文件大小
                        MediaStore.Audio.Media.SIZE,
                        // 视频的绝对地址
                        MediaStore.Audio.Media.DATA,
                        // 歌曲的演唱者
                        MediaStore.Audio.Media.ARTIST,
                };
                @SuppressLint("Recycle") Cursor cursor = resolver.query(uri, objs, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        MediaItem mediaItem = new MediaItem();
                        mMediaItems.add(mediaItem);
                        // 视频的名称
                        String name = cursor.getString(0);
                        mediaItem.setName(name);
                        // 视频的时长
                        long duration = cursor.getLong(1);
                        mediaItem.setDuration(duration);
                        // 视频的文件大小
                        long size = cursor.getLong(2);
                        mediaItem.setSize(size);
                        // 视频的播放地址
                        String data = cursor.getString(3);
                        mediaItem.setData(data);
                        // 艺术家
                        String artist = cursor.getString(4);
                        mediaItem.setArtist(artist);
                    }
                    cursor.close();
                }
            }
        }.start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return stub;
    }

    private IMusicPlayerService.Stub stub = new IMusicPlayerService.Stub() {

        MusicPlayerService mService = MusicPlayerService.this;

        @Override
        public void openAudio(int position) throws RemoteException {
            mService.openAudio(position);
        }

        @Override
        public void start() throws RemoteException {
            mService.start();
        }

        @Override
        public void pause() throws RemoteException {
            mService.pause();
        }

        @Override
        public void stop() throws RemoteException {
            mService.stop();
        }

        @Override
        public int getCurrentPosition() throws RemoteException {
            return mService.getCurrentPosition();
        }

        @Override
        public int getDuration() throws RemoteException {
            return mService.getDuration();
        }

        @Override
        public String getArtist() throws RemoteException {
            return mService.getArtist();
        }

        @Override
        public String getName() throws RemoteException {
            return mService.getName();
        }

        @Override
        public String getAudioPath() throws RemoteException {
            return mService.getAudioPath();
        }

        @Override
        public void next() throws RemoteException {
            mService.next();
        }

        @Override
        public void pre() throws RemoteException {
            mService.pre();
        }

        @Override
        public void setPlayMode(int playmode) throws RemoteException {
            mService.setPlayMode(playmode);
        }

        @Override
        public int getPlayMode() throws RemoteException {
            return mService.getPlayMode();
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return mService.isPlaying();
        }

        @Override
        public void seekTo(int position) throws RemoteException {
            mMediaPlayer.seekTo(position);
        }

        @Override
        public int getAudioSessionId() throws RemoteException {
            return mMediaPlayer.getAudioSessionId();
        }
    };

    /**
     * 根据位置打开对应的音频文件,并且播放
     */
    public void openAudio(int position) {
        this.position = position;
        if (mMediaItems != null && mMediaItems.size() > 0) {
            mMediaItem = mMediaItems.get(position);
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
            }
            try {
                mMediaPlayer = new MediaPlayer();
                // 设置监听：播放出错，播放完成，准备好
                mMediaPlayer.setOnPreparedListener(new MyOnPreparedListener());
                mMediaPlayer.setOnCompletionListener(new MyOnCompletionListener());
                mMediaPlayer.setOnErrorListener(new MyOnErrorListener());
                mMediaPlayer.setDataSource(mMediaItem.getData());
                mMediaPlayer.prepareAsync();

                if (playmode == MusicPlayerService.REPEAT_SINGLE) {
                    // 单曲循环播放-不会触发播放完成的回调
                    mMediaPlayer.setLooping(true);
                } else {
                    // 不循环播放
                    mMediaPlayer.setLooping(false);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(MusicPlayerService.this, "还没有数据", Toast.LENGTH_SHORT).show();
        }
    }

    class MyOnErrorListener implements MediaPlayer.OnErrorListener {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            next();
            return true;
        }
    }

    class MyOnCompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            next();
        }
    }

    class MyOnPreparedListener implements MediaPlayer.OnPreparedListener {

        @Override
        public void onPrepared(MediaPlayer mp) {
            // 通知Activity来获取信息--广播
            // notifyChange(OPEAUDIO);
            EventBus.getDefault().post(mMediaItem);
            start();
        }
    }

    /**
     * 根据动作发广告
     */
    private void notifyChange(String action) {
        Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private NotificationManager mManager;

    /**
     * 播放音乐
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void start() {
        mMediaPlayer.start();

        // 当播放歌曲的时候，在状态显示正在播放，点击的时候，可以进入音乐播放页
        mManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // 最主要
        Intent intent = new Intent(this, AudioPlayerActivity.class);
        // 标识来自状态栏
        intent.putExtra("notification", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.notification_music_playing)
                .setContentTitle("321音乐")
                .setContentText("正在播放：" + getName())
                .setContentIntent(pendingIntent)
                .build();
        mManager.notify(1, notification);
    }

    /**
     * 播放暂停音乐
     */
    private void pause() {
        mMediaPlayer.pause();
        mManager.cancel(1);
    }

    /**
     * 停止
     */
    private void stop() {

    }

    /**
     * 得到当前的播放进度
     */
    private int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    /**
     * 得到当前音频的总时长
     */
    private int getDuration() {
        return mMediaPlayer.getDuration();
    }

    /**
     * 得到艺术家
     */
    private String getArtist() {
        return mMediaItem.getArtist();
    }

    /**
     * 得到歌曲名字
     */
    private String getName() {
        return mMediaItem.getName();
    }

    /**
     * 得到歌曲播放的路径
     */
    private String getAudioPath() {
        return mMediaItem.getData();
    }

    /**
     * 播放下一个视频
     */
    private void next() {
        // 1.根据当前的播放模式，设置下一个的位置
        setNextPosition();
        // 2.根据当前的播放模式和下标位置去播放音频
        openNextAudio();
    }

    private void openNextAudio() {
        int playmode = getPlayMode();
        if (playmode == MusicPlayerService.REPEAT_NORMAL) {
            if (position < mMediaItems.size()) {
                // 正常范围
                openAudio(position);
            } else {
                position = mMediaItems.size() - 1;
            }
        } else if (playmode == MusicPlayerService.REPEAT_SINGLE) {
            openAudio(position);
        } else if (playmode == MusicPlayerService.REPEAT_ALL) {
            openAudio(position);
        } else {
            if (position < mMediaItems.size()) {
                // 正常范围
                openAudio(position);
            } else {
                position = mMediaItems.size() - 1;
            }
        }
    }

    private void setNextPosition() {
        int playmode = getPlayMode();
        if (playmode == MusicPlayerService.REPEAT_NORMAL) {
            position++;
        } else if (playmode == MusicPlayerService.REPEAT_SINGLE) {
            position++;
            if (position >= mMediaItems.size()) {
                position = 0;
            }
        } else if (playmode == MusicPlayerService.REPEAT_ALL) {
            position++;
            if (position >= mMediaItems.size()) {
                position = 0;
            }
        } else {
            position++;
        }
    }

    /**
     * 播放上一个视频
     */
    private void pre() {
        // 1.根据当前的播放模式，设置上一个的位置
        setPrePosition();
        // 2.根据当前的播放模式和下标位置去播放音频
        openPreAudio();
    }

    private void openPreAudio() {
        int playmode = getPlayMode();
        if (playmode == MusicPlayerService.REPEAT_NORMAL) {
            if (position >= 0) {
                // 正常范围
                openAudio(position);
            } else {
                position = 0;
            }
        } else if (playmode == MusicPlayerService.REPEAT_SINGLE) {
            openAudio(position);
        } else if (playmode == MusicPlayerService.REPEAT_ALL) {
            openAudio(position);
        } else {
            if (position >= 0) {
                // 正常范围
                openAudio(position);
            } else {
                position = 0;
            }
        }
    }

    private void setPrePosition() {
        int playmode = getPlayMode();
        if (playmode == MusicPlayerService.REPEAT_NORMAL) {
            position--;
        } else if (playmode == MusicPlayerService.REPEAT_SINGLE) {
            position--;
            if (position < 0) {
                position = mMediaItems.size() - 1;
            }
        } else if (playmode == MusicPlayerService.REPEAT_ALL) {
            position--;
            if (position < 0) {
                position = mMediaItems.size() - 1;
            }
        } else {
            position--;
        }
    }

    /**
     * 设置播放模式
     */
    private void setPlayMode(int playmode) {
        this.playmode = playmode;
        CacheUtils.putPlaymode(this, "playmode", playmode);

        if (playmode == MusicPlayerService.REPEAT_SINGLE) {
            // 单曲循环播放-不会触发播放完成的回调
            mMediaPlayer.setLooping(true);
        } else {
            // 不循环播放
            mMediaPlayer.setLooping(false);
        }
    }

    /**
     * 得到播放模式
     */
    private int getPlayMode() {
        return playmode;
    }

    /**
     * 是否正在播放音频
     */
    private boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

}
