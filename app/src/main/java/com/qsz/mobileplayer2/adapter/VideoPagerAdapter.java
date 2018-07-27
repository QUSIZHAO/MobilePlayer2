package com.qsz.mobileplayer2.adapter;

import android.content.Context;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.qsz.mobileplayer2.R;
import com.qsz.mobileplayer2.domain.MediaItem;
import com.qsz.mobileplayer2.utils.Utils;

import java.util.ArrayList;

/**
 * VideoPager的适配器
 * Created by QSZ on 2018/7/9 11:07
 *
 * @author Administrator
 */
public class VideoPagerAdapter extends BaseAdapter{

    private final boolean isVideo;
    private Context mContext;
    private final ArrayList<MediaItem> mMediaItems;
    private Utils mUtils;

    public VideoPagerAdapter(Context context, ArrayList<MediaItem> mediaItems, boolean isVideo) {
        this.mContext = context;
        this.mMediaItems = mediaItems;
        this.isVideo = isVideo;
        mUtils = new Utils();
    }

    @Override
    public int getCount() {
        return mMediaItems.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHoder viewHoder;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_video_pager, null);
            viewHoder = new ViewHoder();
            viewHoder.iv_icon = convertView.findViewById(R.id.iv_icon);
            viewHoder.tv_name = convertView.findViewById(R.id.tv_name);
            viewHoder.tv_size = convertView.findViewById(R.id.tv_size);
            viewHoder.tv_time = convertView.findViewById(R.id.tv_time);

            convertView.setTag(viewHoder);
        } else {
            viewHoder = (ViewHoder) convertView.getTag();
        }
        // 根据position得到列表中对应位置的数据
        MediaItem mediaItem = mMediaItems.get(position);
        viewHoder.tv_name.setText(mediaItem.getName());
        viewHoder.tv_size.setText(Formatter.formatFileSize(mContext, mediaItem.getSize()));
        viewHoder.tv_time.setText(mUtils.stringForTime((int) mediaItem.getDuration()));

        if (!isVideo) {
            // 音频
            viewHoder.iv_icon.setImageResource(R.drawable.music_default_bg);
        }
        return convertView;
    }

    static class ViewHoder {
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_time;
        TextView tv_size;
    }
}
