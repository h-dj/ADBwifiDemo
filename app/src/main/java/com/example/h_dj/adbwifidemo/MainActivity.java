package com.example.h_dj.adbwifidemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.coorchice.library.SuperTextView;
import com.example.h_dj.adbwifidemo.utils.ADButils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.connAdb)
    SuperTextView mConnAdb;
    @BindView(R.id.stopAdb)
    SuperTextView mStopAdb;
    @BindView(R.id.reStartAdb)
    SuperTextView mReStartAdb;
    @BindView(R.id.et_packageName)
    EditText mEtPackageName;
    @BindView(R.id.uninstallAdb)
    SuperTextView mUninstallAdb;
    @BindView(R.id.killApp)
    SuperTextView mKillApp;
    @BindView(R.id.killCurrentApp)
    SuperTextView mKillCurrentApp;
    @BindView(R.id.startApp)
    SuperTextView mStartApp;
    @BindView(R.id.clearAppCache)
    SuperTextView mClearAppCache;
    @BindView(R.id.rebootPhone)
    SuperTextView mRebootPhone;
    @BindView(R.id.closePhone)
    SuperTextView mClosePhone;

    private Unbinder bind;

    private ADButils mADButils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bind = ButterKnife.bind(this);
        mADButils = ADButils.getInstance().with(getApplicationContext());
    }

    @OnClick({R.id.rebootPhone, R.id.closePhone, R.id.connAdb, R.id.stopAdb, R.id.reStartAdb, R.id.uninstallAdb, R.id.killApp, R.id.killCurrentApp, R.id.startApp, R.id.clearAppCache})
    public void onViewClicked(View view) {
        //获取包名
        String packageName = mEtPackageName.getText().toString().trim();
        switch (view.getId()) {
            case R.id.connAdb:
                Intent openAdb = new Intent(this, ConnectionActivity.class);
                openAdb.putExtra("isConn", true);
                startActivity(openAdb);
                break;
            case R.id.stopAdb:
                Intent closeAdb = new Intent(this, ConnectionActivity.class);
                closeAdb.putExtra("isConn", false);
                startActivity(closeAdb);
                break;
            case R.id.reStartAdb:
                mADButils.restartAdb();
                break;
            case R.id.uninstallAdb:
                if (!TextUtils.isEmpty(packageName)) {
                    mADButils.uninstallApk(packageName);
                } else {
                    Toast.makeText(MainActivity.this, "请填写包名", Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.killApp:
                if (!TextUtils.isEmpty(packageName)) {
                    mADButils.killApp(packageName);
                } else {
                    Toast.makeText(MainActivity.this, "请填写包名", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.killCurrentApp:
                mADButils.killProcess();
                break;
            case R.id.startApp:
                if (!TextUtils.isEmpty(packageName)) {
                    mADButils.startApp(packageName);
                } else {
                    Toast.makeText(MainActivity.this, "请填写包名", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.clearAppCache:
                if (!TextUtils.isEmpty(packageName)) {
                    mADButils.clearAppCache(packageName);
                } else {
                    Toast.makeText(MainActivity.this, "请填写包名", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.rebootPhone:
                mADButils.rebootPhone();
                break;
            case R.id.closePhone:
                mADButils.closePhone();
                break;
        }
    }


    @Override
    protected void onDestroy() {
        bind.unbind();
        super.onDestroy();
    }

}
