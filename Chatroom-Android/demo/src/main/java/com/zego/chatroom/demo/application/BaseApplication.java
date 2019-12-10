package com.zego.chatroom.demo.application;

import android.app.Application;
import android.content.Context;

import com.zego.chatroom.ZegoChatroom;
import com.zego.chatroom.demo.data.ZegoDataCenter;
import com.zego.chatroom.demo.log.LogShareShowManager;
import com.zego.chatroom.manager.log.ZLog;
import com.zego.chatroom.manager.log.ZegoLogManager;

public class BaseApplication extends Application {

    private final static String TAG = BaseApplication.class.getSimpleName();

    public static Context sApplication;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        sApplication = base;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ZegoChatroom.setLogVerbose(true);

        boolean ref = ZegoChatroom.setupContext(this, ZegoDataCenter.ZEGO_USER, ZegoDataCenter.APP_ID, ZegoDataCenter.APP_SIGN);
        // 设置测试环境
        ZegoChatroom.setUseTestEnv(ZegoDataCenter.IS_TEST_ENV);

        initLogShareShowManager();

        if (!ref) {
            ZLog.e(TAG, "-->:: ZegoChatroom.init false");
        }
    }

    /**
     * 初始化摇一摇显示日志管理
     */
    private void initLogShareShowManager() {
        LogShareShowManager.getInstance().enableShareShowLog(this, ZegoLogManager.getInstance().getLogInfoList());
    }
}
