package com.example.h_dj.adbwifidemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.coorchice.library.SuperTextView;
import com.example.h_dj.adbwifidemo.utils.ADButils;
import com.example.h_dj.adbwifidemo.utils.LogUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by H_DJ on 2017/5/1.
 */
public class ConnectionActivity extends AppCompatActivity {

    @BindView(R.id.iv)
    ImageView mIv;
    @BindView(R.id.stv)
    SuperTextView mStv;
    @BindView(R.id.conn_msg)
    SuperTextView mConnMsg;

    private Unbinder bind;
    private Intent intent;
    private ADButils mADButils;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conn);
        bind = ButterKnife.bind(this);
        intent = getIntent();
        mADButils = ADButils.getInstance().with(getApplicationContext());
        initData();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        if (intent != null) {
            boolean isConn = intent.getBooleanExtra("isConn", false);
            LogUtil.e(isConn + "");
            if (isConn) {
                mConnMsg.setVisibility(View.VISIBLE);
                String s = mADButils.openAdb();
                mStv.setText("ADB 已开启");
                mConnMsg.setText(s);
                mIv.setImageResource(R.drawable.wifi);
            } else {
                if (mADButils.isConn) {
                    mADButils.closeAdb();
                    mStv.setText("adb 已关闭");
                } else {
                    mStv.setText("adb 已断开连接");
                }

                mConnMsg.setVisibility(View.GONE);
                mIv.setImageResource(R.drawable.adbwificlose);
            }
        }
    }

    @Override
    protected void onDestroy() {
        bind.unbind();
        super.onDestroy();
    }
}
