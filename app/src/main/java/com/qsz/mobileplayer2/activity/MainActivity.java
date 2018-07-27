package com.qsz.mobileplayer2.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.qsz.mobileplayer2.R;
import com.qsz.mobileplayer2.base.BasePager;
import com.qsz.mobileplayer2.fragment.ReplaceFragment;
import com.qsz.mobileplayer2.pager.AudioPager;
import com.qsz.mobileplayer2.pager.NetAudioPager;
import com.qsz.mobileplayer2.pager.NetVideoPager;
import com.qsz.mobileplayer2.pager.VideoPager;

import java.util.ArrayList;

/**
 * 主页面
 *
 * @author QSZ
 */
public class MainActivity extends FragmentActivity {

    private RadioGroup rg_bottom_tag;

    /**
     * 页面的集合
     */

    private ArrayList<BasePager> mBasePagers;

    /**
     * 选中的位置
     */
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rg_bottom_tag = findViewById(R.id.rg_bottom_tag);

        mBasePagers = new ArrayList<>();
        mBasePagers.add(new VideoPager(this));
        mBasePagers.add(new AudioPager(this));
        mBasePagers.add(new NetVideoPager(this));
        mBasePagers.add(new NetAudioPager(this));

        // 设置RadioGroup的监听
        rg_bottom_tag.setOnCheckedChangeListener(new MyOnCheckedChangeListener());
        // 默认选中首页
        rg_bottom_tag.check(R.id.rb_video);
    }

    private class MyOnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                default:
                    position = 0;
                    break;
                case R.id.rb_audio:
                    position = 1;
                    break;
                case R.id.rb_net_video:
                    position = 2;
                    break;
                case R.id.rb_net_audio:
                    position = 3;
                    break;
            }
            setFragment();
        }
    }

    /**
     * 把页面添加到Fragment
     */
    private void setFragment() {
        // 1. 得到FragmentManger
        FragmentManager manager = getSupportFragmentManager();
        // 2. 开启事务
        FragmentTransaction ft = manager.beginTransaction();
        // 3. 替换
       /* ft.replace(R.id.fl_main_content, new Fragment() {
            @Nullable
            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
                BasePager basePager = getBasePager();
                if (basePager != null) {
                    // 各个页面的视图
                    return basePager.rootView;
                }
                return null;
            }
        });*/
        ft.replace(R.id.fl_main_content, new ReplaceFragment(getBasePager()));
        // 4. 提交事务
        ft.commit();
    }

    /**
     * 根据位置得到对应的页面
     */
    private BasePager getBasePager() {
        BasePager basePager = mBasePagers.get(position);
        if (basePager != null && !basePager.isInitData) {
            // 联网请求或者绑定数据
            basePager.initData();
            basePager.isInitData = true;
        }
        return basePager;
    }

    /**
     * 是否已经退出
     */
    private boolean isExit = false;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 不是第一页面
            if (position != 0) {
                position = 0;
                // 首页
                rg_bottom_tag.check(R.id.rb_video);
                return true;
            } else if (!isExit) {
                isExit = true;
                Toast.makeText(MainActivity.this, "再按一次退出", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isExit = false;
                    }
                }, 2000);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
