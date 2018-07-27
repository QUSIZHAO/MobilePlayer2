package com.qsz.mobileplayer2.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.qsz.mobileplayer2.R;
import com.qsz.mobileplayer2.domain.SearchBean;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by QSZ on 2018/7/9 15:07
 *
 * @author QSZ
 */
public class SearchAdapter extends BaseAdapter {

    private Context mContext;
    private final List<SearchBean.ItemData> mediaItems;

    public SearchAdapter(Context context, List<SearchBean.ItemData> mediaItems) {
        this.mContext = context;
        this.mediaItems = mediaItems;
    }

    @Override
    public int getCount() {
        return mediaItems.size();
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
            convertView = View.inflate(mContext, R.layout.item_netvideo_pager, null);
            viewHoder = new ViewHoder();
            viewHoder.iv_icon = convertView.findViewById(R.id.iv_icon);
            viewHoder.tv_name = convertView.findViewById(R.id.tv_name);
            viewHoder.tv_desc = convertView.findViewById(R.id.tv_desc);

            convertView.setTag(viewHoder);
        } else {
            viewHoder = (ViewHoder) convertView.getTag();
        }
        // 根据position得到列表中对应位置的数据
        SearchBean.ItemData mediaItem = mediaItems.get(position);
        viewHoder.tv_name.setText(mediaItem.getItemTitle());
        viewHoder.tv_desc.setText(mediaItem.getKeywords());
        //1.使用xUtils3请求图片
//        x.image().bind(viewHoder.iv_icon,mediaItem.getImageUrl());
        //2.使用Glide请求图片
//        Glide.with(context).load(mediaItem.getImageUrl())
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .placeholder(R.drawable.video_default)
//                .error(R.drawable.video_default)
//                .into(viewHoder.iv_icon);

        //3.使用Picasso 请求图片
        Picasso.with(mContext).load(mediaItem.getItemImage().getImgUrl1())
                .placeholder(R.drawable.video_default)
                .error(R.drawable.video_default)
                .into(viewHoder.iv_icon);
        return convertView;
    }

    static class ViewHoder {
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_desc;
    }
}
