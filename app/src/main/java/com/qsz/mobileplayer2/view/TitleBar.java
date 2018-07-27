package com.qsz.mobileplayer2.view;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.qsz.mobileplayer2.R;
import com.qsz.mobileplayer2.activity.SearchActivity;

/**
 * 自定义标题栏
 * Created by QSZ on 2018/7/9 10:40
 *
 * @author Administrator
 */
public class TitleBar extends LinearLayout implements View.OnClickListener {

    private View tv_search;

    private View rl_game;

    private View iv_record;

    private Context mContext;

    /**
     * 在代码中实例化改类的时候使用这个方法
     */
    public TitleBar(Context context) {
        this(context, null);
    }

    /**
     * 当在布局文件使用该类的时候，Android系统通过这个构造方法实例化该类
     */
    public TitleBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * 当需要设置样式的时候，可以使用该方法
     */
    public TitleBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
    }

    /**
     * 当布局文件加载完成的时候回调这个方法
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // 得到孩子的实例
        tv_search = getChildAt(1);
        rl_game = getChildAt(2);
        iv_record = getChildAt(3);

        // 设置点击事件
        tv_search.setOnClickListener(this);
        rl_game.setOnClickListener(this);
        iv_record.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 搜索
            case R.id.tv_search:
                Intent intent = new Intent(mContext, SearchActivity.class);
                mContext.startActivity(intent);
                break;
            // 游戏
            case R.id.rl_game:
                Toast.makeText(mContext, "游戏", Toast.LENGTH_SHORT).show();
                break;
            // 播放历史
            case R.id.iv_record:
                Toast.makeText(mContext, "播放历史", Toast.LENGTH_SHORT).show();
                break;
            default:
        }
    }
}
