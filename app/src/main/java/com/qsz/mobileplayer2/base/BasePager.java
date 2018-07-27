package com.qsz.mobileplayer2.base;

import android.content.Context;
import android.view.View;

/**
 * 基类，公共类
 * Created by QSZ on 2018/7/9 10:53
 ** VideoPager
 * <p/>
 * AudioPager
 * <p/>
 * NetVideoPager
 * <p/>
 * NetAudioPager
 * 都继承该类
 * @author QSZ
 */
public abstract class BasePager {

    /**
     * 上下文
     */
    public final Context mContext;
    /**
     * 接收各个页面的实例
     */
    public View rootView;
    public boolean isInitData;

    protected BasePager(Context context) {
        this.mContext = context;
        rootView = initView();
    }

    /**
     * 强制子页面实现该方法，实现想要的特定的效果
     *
     * @return
     */
    public abstract View initView();

    /**
     * 当子页面，需要绑定数据，或者联网请求数据并且绑定的时候，重写该方法
     */
    public void initData() {

    }
}
