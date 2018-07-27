// IMusicPlayerService.aidl
package com.qsz.mobileplayer2;

// Declare any non-default types here with import statements

interface IMusicPlayerService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
     /**
         * 根据位置打开对应的音频文件
         */
        void openAudio(int position);

        /**
         * 播放音乐
         * */
        void start();

        /**
         * 播放暂停音乐
         * */
        void pause();

        /**
         * 停止
         */
        void stop();

        /**
         * 得到当前的播放进度
         */
        int getCurrentPosition();

        /**
         * 得到当前音频的总时长
         */
        int getDuration();

        /**
         * 得到艺术家
         */
        String getArtist();

        /**
         * 得到歌曲名字
         */
        String getName();


        /**
         * 得到歌曲播放的路径
         */
        String getAudioPath();

        /**
         * 播放下一个视频
         */
        void next();


        /**
         * 播放上一个视频
         */
        void pre();

        /**
         * 设置播放模式
         */
        void setPlayMode(int playmode);

        /**
         * 得到播放模式
         */
        int getPlayMode();

        /**
         * 是否正在播放
         */
        boolean isPlaying();

        /**
         拖动音频
         */
        void seekTo(int position);

        int getAudioSessionId();
}
