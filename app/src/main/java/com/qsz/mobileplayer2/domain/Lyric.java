package com.qsz.mobileplayer2.domain;

/**
 * 作用：歌词类
 * Created by QSZ on 2018/7/18 10:13
 * 例：[01:21.35]我在这里寻找
 * @author QSZ
 */
public class Lyric {
    /**
     * 歌词内容
     * */
    private String content;
    /**
     * 时间戳
     * */
    private long timePoint;
    /**
     * 休眠时间或者高亮显示的时间
     * */
    private long sleepTime;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimePoint() {
        return timePoint;
    }

    public void setTimePoint(long timePoint) {
        this.timePoint = timePoint;
    }

    public long getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    @Override
    public String toString() {
        return "Lyric{" +
                "content='" + content + '\'' +
                ", timePoint=" + timePoint +
                ", sleepTime=" + sleepTime +
                '}';
    }
}
