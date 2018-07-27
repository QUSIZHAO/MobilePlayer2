package com.qsz.mobileplayer2.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.qsz.mobileplayer2.R;
import com.qsz.mobileplayer2.adapter.SearchAdapter;
import com.qsz.mobileplayer2.domain.SearchBean;
import com.qsz.mobileplayer2.utils.Constants;
import com.qsz.mobileplayer2.utils.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;


/**
 * 搜索页面
 * Created by QSZ on 2018/7/9 15:03
 *
 * @author QSZ
 */
public class SearchActivity extends Activity {

    private EditText etInput;
    private ImageView ivVoice;
    private TextView tvSearch;
    private ListView mListView;
    private ProgressBar mProgressBar;
    private TextView tvNodata;
    private SearchAdapter mAdapter;

    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    private String url;
    private List<SearchBean.ItemData> items;

    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2018-07-20 09:10:08 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        etInput = findViewById(R.id.et_input);
        ivVoice = findViewById(R.id.iv_voice);
        tvSearch = findViewById(R.id.tv_search);
        mListView = findViewById(R.id.listview);
        mProgressBar = findViewById(R.id.progressBar);
        tvNodata = findViewById(R.id.tv_nodata);
        MyOnClickListener myOnClickListener = new MyOnClickListener();
        ivVoice.setOnClickListener(myOnClickListener);
        tvSearch.setOnClickListener(myOnClickListener);
    }

    class MyOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                // 语音输入
                case R.id.iv_voice:
                    showDialog();
//                    Toast.makeText(SearchActivity.this, "语音输入", Toast.LENGTH_SHORT).show();
                    break;
                // 搜索
                case R.id.tv_search:
//                    Toast.makeText(SearchActivity.this,"搜索",Toast.LENGTH_SHORT).show();
                    searchText();
                    break;
                default:
            }
        }
    }

    private void searchText() {
        String text = etInput.getText().toString().trim();
        if (!TextUtils.isEmpty(text)) {
            if (items != null && items.size() > 0) {
                items.clear();
            }
            try {
                text = URLEncoder.encode(text, "UTF-8");
                url = Constants.SEARCH_URL + text;
                getDataFromNet();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    private void getDataFromNet() {
        mProgressBar.setVisibility(View.VISIBLE);
        RequestParams params = new RequestParams(url);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                processData(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {
                mProgressBar.setVisibility(View.GONE);
            }
        });
    }

    private void processData(String result) {
        SearchBean searchBean = parsedJson(result);
        items = searchBean.getItems();

        showData();
    }

    private void showData() {
        if (items != null && items.size() > 0) {
            // 设置适配器
            mAdapter = new SearchAdapter(this, items);
            mListView.setAdapter(mAdapter);
            tvNodata.setVisibility(View.GONE);
        } else {
            tvNodata.setVisibility(View.VISIBLE);
            mAdapter.notifyDataSetChanged();
        }

        mProgressBar.setVisibility(View.GONE);
    }

    /**
     * 解析json数据
     */
    private SearchBean parsedJson(String result) {
        return new Gson().fromJson(result, SearchBean.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        findViews();
    }

    private void showDialog() {
        // 1.创建RecognizerDialog对象
        RecognizerDialog mDialog = new RecognizerDialog(this, new MyInitListener());
        // 2.设置accent、language等参数
        // 中文
        mDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        // 普通话
        mDialog.setParameter(SpeechConstant.ACCENT, "mandarin");
        // 若要将UI控件用于语义理解，必须添加以下参数设置，设置之后onResult回调返回将是语义理解
        // 结果
        // dialog.setParameter("asr_sch","1");
        // dialog.setParameter("nlp_version","2.0");
        // 3.设置回调接口
        mDialog.setListener(new MyRecognizerDialogListener());
        // 4.显示dialog，接收语音输入
        mDialog.show();
    }

    class MyRecognizerDialogListener implements RecognizerDialogListener {

        /**
         * 是否说话结束
         */
        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
            String result = recognizerResult.getResultString();
            android.util.Log.e("MainActivity", "result == " + result);
            String text = JsonParser.parseIatResult(result);
            // 解析好的
            Log.e("MainActivity", "text == " + text);

            String sn = null;
            // 读取json结果中的sn字段
            try {
                JSONObject resultJson = new JSONObject(recognizerResult.getResultString());
                sn = resultJson.optString("sn");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mIatResults.put(sn, text);

            // 拼成一句
            StringBuilder resultBuffer = new StringBuilder();
            for (String key : mIatResults.keySet()) {
                resultBuffer.append(mIatResults.get(key));
            }
            etInput.setText(resultBuffer.toString());
            etInput.setSelection(etInput.length());
        }

        /**
         * 出错了
         */
        @Override
        public void onError(SpeechError speechError) {
            Log.e("MainActivity", "onError == " + speechError.getMessage());
        }
    }

    class MyInitListener implements InitListener {

        @Override
        public void onInit(int i) {
            if (i != ErrorCode.SUCCESS) {
                Toast.makeText(SearchActivity.this, "初始化失败", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
