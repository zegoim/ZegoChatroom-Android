package com.zego.chatroom.demo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.zego.chatroom.demo.application.BaseApplication;
import com.zego.chatroom.demo.utils.UiUtils;
import com.zego.chatroom.demo.view.TipDialog;

/**
 * Activity 抽象基类
 */
public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();

    /**
     * 申请权限 code
     */
    private static final int PERMISSIONS_REQUEST_CODE = 1002;

    /**
     * 提示Dialog
     */
    private TipDialog mTipDialog;

    /**
     * volley 请求分发队列
     */
    private RequestQueue mQueue;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置沉浸式布局
        UiUtils.setImmersedWindow(getWindow(), true);

        // 申请权限
        checkOrRequestPermission(PERMISSIONS_REQUEST_CODE);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // 设置沉浸式布局
        if (Build.VERSION.SDK_INT > 18) {
            getContentView().setPadding(0, UiUtils.getStatusBarHeight(this), 0, 0);
        }
    }

    /**
     * 获取contentView
     *
     * @return 返回contentView
     */
    protected View getContentView() {
        ViewGroup contentLayout = getWindow().getDecorView().findViewById(android.R.id.content);
        return contentLayout != null && contentLayout.getChildCount() != 0 ? contentLayout.getChildAt(0) : null;
    }

    /**
     * 懒加载TipDialog
     *
     * @return 返回页面公用的TipDialog
     */
    public TipDialog getTipDialog() {
        if (mTipDialog == null) {
            mTipDialog = new TipDialog(this);
        }
        return mTipDialog;
    }

    protected RequestQueue getRequestQueue() {
        if (mQueue == null) {
            mQueue = Volley.newRequestQueue(BaseApplication.sApplication);
        }
        return mQueue;
    }

    // 相机存储音频权限申请
    private static String[] PERMISSIONS_REQUEST = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE", Manifest.permission.RECORD_AUDIO};

    /**
     * 检查并申请权限
     *
     * @param requestCode requestCode
     * @return 权限是否已经允许
     */
    protected boolean checkOrRequestPermission(int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(PERMISSIONS_REQUEST, requestCode);
                return false;
            }
        }
        return true;
    }
}
