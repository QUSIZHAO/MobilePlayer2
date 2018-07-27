package com.qsz.mobileplayer2.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.qsz.mobileplayer2.R;
import com.qsz.mobileplayer2.domain.NetAudioPagerData;
import com.qsz.mobileplayer2.utils.Utils;

import org.xutils.common.util.DensityUtil;
import org.xutils.x;

import java.util.List;

import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import pl.droidsonroids.gif.GifImageView;

/**
 * NetAudioPager的适配器
 * Created by QSZ on 2018/7/19 10:30
 *
 * @author QSZ
 */
public class NetAudioPagerAdapter extends BaseAdapter {

    /**
     * 视频
     */
    private static final int TYPE_VIDEO = 0;

    /**
     * 图片
     */
    private static final int TYPE_IMAGE = 1;

    /**
     * 文字
     */
    private static final int TYPE_TEXT = 2;

    /**
     * GIF图片
     */
    private static final int TYPE_GIF = 3;

    /**
     * 软件推广
     */
    private static final int TYPE_AD = 4;

    private Context mContext;
    private final List<NetAudioPagerData.ListEntity> mDatas;
    private Utils mUtils;

    public NetAudioPagerAdapter(Context context, List<NetAudioPagerData.ListEntity> datas) {
        this.mContext = context;
        this.mDatas = datas;
        mUtils = new Utils();
    }

    @Override
    public int getViewTypeCount() {
        return 5;
    }

    /**
     * 根据位置得到对应的类型
     */
    @Override
    public int getItemViewType(int position) {
        NetAudioPagerData.ListEntity listEntity = mDatas.get(position);
        String type = listEntity.getType();
        int itemViewType;
        if ("video".equals(type)) {
            itemViewType = TYPE_VIDEO;
        } else if ("image".equals(type)) {
            itemViewType = TYPE_IMAGE;
        } else if ("text".equals(type)) {
            itemViewType = TYPE_TEXT;
        } else if ("gif".equals(type)) {
            itemViewType = TYPE_GIF;
        } else {
            // 广播
            itemViewType = TYPE_AD;
        }
        return itemViewType;
    }

    @Override
    public int getCount() {
        return mDatas.size();
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

        // 得到类型
        int itemViewType = getItemViewType(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            // 初始化
            // 初始化item布局
            viewHolder = new ViewHolder();
            convertView = initView(convertView, itemViewType, viewHolder);
            // 初始化公共的视图
            initCommonView(convertView, itemViewType, viewHolder);
            // 设置tag
            convertView.setTag(viewHolder);
        } else {
            // 获取tag
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // 绑定数据
        // 根据位置得到数据，绑定数据
        NetAudioPagerData.ListEntity mediaItem = mDatas.get(position);
        bindData(itemViewType, viewHolder, mediaItem);
        return convertView;
    }

    @SuppressLint("SetTextI18n")
    private void bindData(int itemViewType, ViewHolder viewHolder, NetAudioPagerData.ListEntity mediaItem) {
        switch (itemViewType) {
            case TYPE_VIDEO://视频
                bindData(viewHolder, mediaItem);
                //第一个参数是视频播放地址，第二个参数是显示封面的地址，第三参数是标题
                viewHolder.jcv_videoplayer.setUp(mediaItem.getVideo().getVideo().get(0), mediaItem.getVideo().getThumbnail().get(0), null);
                viewHolder.tv_play_nums.setText(mediaItem.getVideo().getPlaycount() + "次播放");
                viewHolder.tv_video_duration.setText(mUtils.stringForTime(mediaItem.getVideo().getDuration() * 1000) + "");

                break;
            case TYPE_IMAGE://图片
                bindData(viewHolder, mediaItem);
                viewHolder.iv_image_icon.setImageResource(R.drawable.bg_item);
                int height = mediaItem.getImage().getHeight() <= DensityUtil.getScreenHeight() * 0.75 ? mediaItem.getImage().getHeight() : (int) (DensityUtil.getScreenHeight() * 0.75);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(DensityUtil.getScreenWidth(), height);
                viewHolder.iv_image_icon.setLayoutParams(params);
                if (mediaItem.getImage() != null && mediaItem.getImage().getBig() != null && mediaItem.getImage().getBig().size() > 0) {
                    // x.image().bind(viewHolder.iv_image_icon, mediaItem.getImage().getBig().get(0));
                    Glide.with(mContext).load(mediaItem.getImage().getBig().get(0)).placeholder(R.drawable.bg_item).error(R.drawable.bg_item).diskCacheStrategy(DiskCacheStrategy.ALL).into(viewHolder.iv_image_icon);
                }
                break;
            case TYPE_TEXT:
                bindData(viewHolder, mediaItem);
                break;
            case TYPE_GIF:
                bindData(viewHolder, mediaItem);
                System.out.println("mediaItem.getGif().getImages().get(0)" + mediaItem.getGif().getImages().get(0));
                Glide.with(mContext).load(mediaItem.getGif().getImages().get(0)).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(viewHolder.iv_image_gif);

                break;
            case TYPE_AD:
                //软件广告
                break;
            default:
        }

        //设置文本
        viewHolder.tv_context.setText(mediaItem.getText());
    }

    private void initCommonView(View convertView, int itemViewType, ViewHolder viewHolder) {
        switch (itemViewType) {
            case TYPE_VIDEO:
            case TYPE_IMAGE:
            case TYPE_TEXT:
            case TYPE_GIF:
                // 加载除开广告部分的公共部分视图
                // user info
                viewHolder.iv_headpic = convertView.findViewById(R.id.iv_headpic);
                viewHolder.tv_name = convertView.findViewById(R.id.tv_name);
                viewHolder.tv_time_refresh = convertView.findViewById(R.id.tv_time_refresh);
                viewHolder.iv_right_more = convertView.findViewById(R.id.iv_right_more);
                //bottom
                viewHolder.iv_video_kind = convertView.findViewById(R.id.iv_video_kind);
                viewHolder.tv_video_kind_text = convertView.findViewById(R.id.tv_video_kind_text);
                viewHolder.tv_shenhe_ding_number = convertView.findViewById(R.id.tv_shenhe_ding_number);
                viewHolder.tv_shenhe_cai_number = convertView.findViewById(R.id.tv_shenhe_cai_number);
                viewHolder.tv_posts_number = convertView.findViewById(R.id.tv_posts_number);
                viewHolder.ll_download = convertView.findViewById(R.id.ll_download);

                break;
            default:
        }
        // 中间公共部分-所有的都有
        viewHolder.tv_context = convertView.findViewById(R.id.tv_context);
    }

    private View initView(View convertView, int itemViewType, ViewHolder viewHolder) {
        switch (itemViewType) {
            case TYPE_VIDEO:
                //视频
                convertView = View.inflate(mContext, R.layout.all_video_item, null);
                //在这里实例化特有的
                viewHolder.tv_play_nums = convertView.findViewById(R.id.tv_play_nums);
                viewHolder.tv_video_duration = convertView.findViewById(R.id.tv_video_duration);
                viewHolder.iv_commant = convertView.findViewById(R.id.iv_commant);
                viewHolder.tv_commant_context = convertView.findViewById(R.id.tv_commant_context);
                viewHolder.jcv_videoplayer = convertView.findViewById(R.id.jcv_videoplayer);
                break;
            case TYPE_IMAGE:
                //图片
                convertView = View.inflate(mContext, R.layout.all_image_item, null);
                viewHolder.iv_image_icon = convertView.findViewById(R.id.iv_image_icon);
                break;
            case TYPE_TEXT:
                //文字
                convertView = View.inflate(mContext, R.layout.all_text_item, null);
                break;
            case TYPE_GIF:
                //gif
                convertView = View.inflate(mContext, R.layout.all_gif_item, null);
                viewHolder.iv_image_gif = convertView.findViewById(R.id.iv_image_gif);
                break;
            case TYPE_AD:
                //软件广告
                convertView = View.inflate(mContext, R.layout.all_ad_item, null);
                viewHolder.btn_install = convertView.findViewById(R.id.btn_install);
                viewHolder.iv_image_icon = convertView.findViewById(R.id.iv_image_icon);
                break;
            default:
        }
        return convertView;
    }

    @SuppressLint("SetTextI18n")
    private void bindData(ViewHolder viewHolder, NetAudioPagerData.ListEntity mediaItem) {
        if (mediaItem.getU() != null && mediaItem.getU().getHeader() != null && mediaItem.getU().getHeader().get(0) != null) {
            x.image().bind(viewHolder.iv_headpic, mediaItem.getU().getHeader().get(0));
        }
        if (mediaItem.getU() != null && mediaItem.getU().getName() != null) {
            viewHolder.tv_name.setText(mediaItem.getU().getName() + "");
        }
        viewHolder.tv_time_refresh.setText(mediaItem.getPasstime());

        // 设置标签
        List<NetAudioPagerData.ListEntity.TagsEntity> tagsEntities = mediaItem.getTags();
        if (tagsEntities != null && tagsEntities.size() > 0) {
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < tagsEntities.size(); i++) {
                buffer.append(tagsEntities.get(i).getName());
            }
            viewHolder.tv_video_kind_text.setText(buffer.toString());
        }

        // 设置点赞，踩，转发
        viewHolder.tv_shenhe_ding_number.setText(mediaItem.getUp());
        viewHolder.tv_shenhe_cai_number.setText(mediaItem.getDown() + "");
        viewHolder.tv_posts_number.setText(mediaItem.getForward() + "");
    }

    static class ViewHolder {
        // user_info
        ImageView iv_headpic;
        TextView tv_name;
        TextView tv_time_refresh;
        ImageView iv_right_more;
        // bottom
        ImageView iv_video_kind;
        TextView tv_video_kind_text;
        TextView tv_shenhe_ding_number;
        TextView tv_shenhe_cai_number;
        TextView tv_posts_number;
        LinearLayout ll_download;

        // 中间公共部分 -所有的都有
        TextView tv_context;

        // Video
        // TextView tv_context;
        TextView tv_play_nums;
        TextView tv_video_duration;
        ImageView iv_commant;
        TextView tv_commant_context;
        JCVideoPlayer jcv_videoplayer;

        //Image
        ImageView iv_image_icon;
//        TextView tv_context;

        //Text
//        TextView tv_context;

        //Gif
        GifImageView iv_image_gif;
//        TextView tv_context;

        //软件推广
        Button btn_install;
//        TextView iv_image_icon;
        //TextView tv_context;
    }
}
