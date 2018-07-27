package com.qsz.mobileplayer2.pager;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.qsz.mobileplayer2.R;
import com.qsz.mobileplayer2.adapter.NetAudioPagerAdapter;
import com.qsz.mobileplayer2.base.BasePager;
import com.qsz.mobileplayer2.domain.NetAudioPagerData;
import com.qsz.mobileplayer2.utils.CacheUtils;
import com.qsz.mobileplayer2.utils.Constants;
import com.qsz.mobileplayer2.utils.LogUtil;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.List;

/**
 * 网络音频页面
 * Created by QSZ on 2018/7/9 11:11
 *
 * @author QSZ
 */
public class NetAudioPager extends BasePager {

    @ViewInject(R.id.listview)
    private ListView mListView;

    @ViewInject(R.id.tv_nonet)
    private TextView mTv_nonet;

    @ViewInject(R.id.pb_loading)
    private ProgressBar pb_loading;

    /**
     * 页面的数据
     */
    private List<NetAudioPagerData.ListEntity> datas;

    private NetAudioPagerAdapter mAdapter;

    public NetAudioPager(Context context) {
        super(context);
    }

    /**
     * 初始化当前页面的控件，由父类调用
     */
    @Override
    public View initView() {
        View view = View.inflate(mContext, R.layout.netaudio_pager, null);
        // 第一个参数是：NetVideoPager.this,第二个参数是： 布局
        x.view().inject(NetAudioPager.this, view);
        return view;
    }

    @Override
    public void initData() {
        super.initData();
        LogUtil.e("网络音频的数据被初始化了。。。");
        String saveJson = CacheUtils.getString(mContext, Constants.ALL_RES_URL);
        if (!TextUtils.isEmpty(saveJson)) {
            // 解析数据
            processData(saveJson);
        }
        // 联网
        getDataFromNet();
    }

    private void getDataFromNet() {
        // 联网
        // 视频内容
        RequestParams params = new RequestParams(Constants.ALL_RES_URL);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                LogUtil.e("请求数成功==" + result);
                // 缓存数据
                CacheUtils.putString(mContext, Constants.ALL_RES_URL, result);
                // 主线程
                processData(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                LogUtil.e("请求数据失败==" + ex.getMessage());
            }

            @Override
            public void onCancelled(CancelledException cex) {
                LogUtil.e("onCancelled==" + cex.getMessage());
            }

            @Override
            public void onFinished() {
                LogUtil.e("onFinished==");
            }
        });
    }

    /**
     * 解析json数据和显示数据
     * 解析数据：1.GsonFormat生成bean对象；2.用gson解析数据
     */
    private void processData(String json) {
        // 解析数据
        NetAudioPagerData data = parsedJson(json);
        datas = data.getList();

        if (datas != null && datas.size() > 0) {
            // 有数据
            mTv_nonet.setVisibility(View.GONE);
            // 设置适配器
            mAdapter = new NetAudioPagerAdapter(mContext, datas);
            mListView.setAdapter(mAdapter);
        } else {
            mTv_nonet.setText("没有对应的数据...");
            // 没有数据
            mTv_nonet.setVisibility(View.VISIBLE);
        }
        pb_loading.setVisibility(View.GONE);
    }

    /**
     * Gson解析数据
     */
    private NetAudioPagerData parsedJson(String json) {
        return new Gson().fromJson(json, NetAudioPagerData.class);
    }

}
