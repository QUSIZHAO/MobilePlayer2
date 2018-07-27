package com.qsz.mobileplayer2.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qsz.mobileplayer2.base.BasePager;

/**
 * 主页面，替换 Fragment方法
 * Created by QSZ on 2018/7/9 13:48
 *
 * @author QSZ
 */
@SuppressLint("ValidFragment")
public class ReplaceFragment extends Fragment {

    private BasePager currPager;

    public ReplaceFragment(BasePager basePager) {
        this.currPager = basePager;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return currPager.rootView;
    }
}
