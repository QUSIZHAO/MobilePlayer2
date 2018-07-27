package com.qsz.mobileplayer2.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.qsz.mobileplayer2.service.MusicPlayerService;

/**
 * 缓存工具类
 * Created by QSZ on 2018/7/16 12:25
 *
 * @author QSZ
 */
public class CacheUtils {
    /**
     * 保持播放模式
     */
    public static void putPlaymode(Context context, String key, int values) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("atguigu", Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt(key, values).apply();
    }

    /**
     * 得到播放模式
     */
    public static int getPlaymode(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("atguigu", Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key, MusicPlayerService.REPEAT_NORMAL);
    }

    /**
     * 保持数据
     */
    public static void putString(Context context, String key, String values) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("atguigu", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(key, values).apply();
    }

    /**
     * 得到缓存的数据
     */
    public static String getString(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("atguigu", Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, "");
    }
}
