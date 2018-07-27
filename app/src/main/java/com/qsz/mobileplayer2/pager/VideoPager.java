package com.qsz.mobileplayer2.pager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.qsz.mobileplayer2.R;
import com.qsz.mobileplayer2.activity.SystemVideoPlayer;
import com.qsz.mobileplayer2.adapter.VideoPagerAdapter;
import com.qsz.mobileplayer2.base.BasePager;
import com.qsz.mobileplayer2.domain.MediaItem;
import com.qsz.mobileplayer2.utils.LogUtil;

import java.util.ArrayList;

/**
 * 本地视频页面
 * Created by QSZ on 2018/7/9 11:02
 *
 * @author Administrator
 */
public class VideoPager extends BasePager {

    private ListView mListView;
    private TextView tv_nomedia;
    private ProgressBar pb_loading;

    private VideoPagerAdapter mVideoPagerAdapter;

    /**
     * 装数据集合
     */
    private ArrayList<MediaItem> mMediaItems;

    public VideoPager(Context context) {
        super(context);
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mMediaItems != null && mMediaItems.size() > 0) {
                // 有数据
                // 设置适配器
                mVideoPagerAdapter = new VideoPagerAdapter(mContext, mMediaItems, true);
                mListView.setAdapter(mVideoPagerAdapter);
                // 把文本隐藏
                tv_nomedia.setVisibility(View.GONE);
            } else {
                // 没有数据
                // 文本显示
                tv_nomedia.setVisibility(View.VISIBLE);
            }
            // ProgressBar隐藏
            pb_loading.setVisibility(View.GONE);
        }
    };

    /**
     * 初始化当前页面的控件，由父类调用
     */
    @Override
    public View initView() {
        View view = View.inflate(mContext, R.layout.video_pager, null);
        mListView = view.findViewById(R.id.list_view);
        tv_nomedia = view.findViewById(R.id.tv_nomedia);
        pb_loading = view.findViewById(R.id.pb_loading);
        // 设置ListView的Item的点击事件
        mListView.setOnItemClickListener(new MyOnItemClickListener());
        return view;
    }

    class MyOnItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            MediaItem mediaItem = mMediaItems.get(position);
            //            Toast.makeText(context, "mediaItem=="+mediaItem.toString(), Toast.LENGTH_SHORT).show();

            //1.调起系统所有的播放-隐式意图
//            Intent intent = new Intent();
//            intent.setDataAndType(Uri.parse(mediaItem.getData()),"video/*");
//            context.startActivity(intent);

            //2.调用自己写的播放器-显示意图--一个播放地址
//            Intent intent = new Intent(context,SystemVideoPlayer.class);
//            intent.setDataAndType(Uri.parse(mediaItem.getData()),"video/*");
//            context.startActivity(intent);
            //3.传递列表数据-对象-序列化
            Intent intent = new Intent(mContext, SystemVideoPlayer.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("videolist", mMediaItems);
            intent.putExtras(bundle);
            intent.putExtra("position", position);
            mContext.startActivity(intent);
        }
    }

    @Override
    public void initData() {
        super.initData();
        LogUtil.e("本地视频的数据被初始化了...");
        // 加载本地数据
        getDataFromLocal();
    }

    /**
     * 从本地的sdcard得到数据
     * //1.遍历sdcard,后缀名
     * //2.从内容提供者里面获取视频
     * //3.如果是6.0的系统，动态获取读取sdcard的权限
     */
    private void getDataFromLocal() {

        new Thread() {
            @Override
            public void run() {
                super.run();
                mMediaItems = new ArrayList<>();
                ContentResolver resolver = mContext.getContentResolver();
                Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                String[] objs = {
                        // 视频文件在sd卡的名称
                        MediaStore.Video.Media.DISPLAY_NAME,
                        // 视频总时长
                        MediaStore.Video.Media.DURATION,
                        // 视频的文件大小
                        MediaStore.Video.Media.SIZE,
                        // 绝对地址
                        MediaStore.Video.Media.DATA,
                        // 歌曲的演唱者
                        MediaStore.Video.Media.ARTIST,
                };
                Cursor cursor = resolver.query(uri, objs, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        MediaItem mediaItem = new MediaItem();
                        // 写在上面
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
                // Handler发消息
                mHandler.sendEmptyMessage(10);
            }
        }.start();
    }

    /**
     * 解决安卓6.0以上版本不能读取外部存储权限的问题
     *
     * @param activity
     * @return
     */
    public static boolean isGrantExternalRW(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            activity.requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);

            return false;
        }

        return true;
    }
}
