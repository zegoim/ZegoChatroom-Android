package com.zego.chatroom.demo.log;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.zego.chatroom.manager.log.ZegoLogInfo;

import java.util.List;

public class LogShareShowManager implements Application.ActivityLifecycleCallbacks, SensorEventListener {

    private final static String TAG = LogShareShowManager.class.getSimpleName();


    private static final int SHAKE_TIME_TO_SHOW_LOG_DIALOG = 8;
    private static final long SHAKE_TIME_LIMIT = 1000;


    private SensorManager mSensorManager;
    private Sensor mAccelerometerSensor;
    private ZegoLogDialog mLogDialog;

    private int mShakeTime = 1;
    private long mShakeStartTime;

    private List<ZegoLogInfo> mLogInfoList;

    private static class LogShareShowManagerHolder {
        private final static LogShareShowManager sInstance = new LogShareShowManager();
    }

    public static LogShareShowManager getInstance() {
        return LogShareShowManager.LogShareShowManagerHolder.sInstance;
    }

    /**
     * 启动摇一摇显示日志功能
     */
    public void enableShareShowLog(@NonNull Application application, List<ZegoLogInfo> logInfoList) {
        if (logInfoList == null) {
            Log.e(TAG, "not yet start traceType，please call startTrace method first");
            return;
        }
        mLogInfoList = logInfoList;
        application.registerActivityLifecycleCallbacks(this);
        mSensorManager = (SensorManager) application.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    private void initLogDialog(Context context) {
        mLogDialog = new ZegoLogDialog(context);
        mLogDialog.setLogInfoList(mLogInfoList);
    }

    // ------------------ implements ActivityLifecycleCallbacks ------------------ //
    @Override
    public void onActivityResumed(Activity activity) {
        initLogDialog(activity);
        mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        // DO NOTHING
    }

    @Override
    public void onActivityStarted(Activity activity) {
        // DO NOTHING
    }


    @Override
    public void onActivityStopped(Activity activity) {
        // DO NOTHING
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        // DO NOTHING
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        // DO NOTHING
    }

    // ------------------ implements SensorEventListener ------------------ //

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mLogDialog.isShowing()) {
            return;
        }

        int type = event.sensor.getType();

        if (type == Sensor.TYPE_ACCELEROMETER) {
            //获取三个方向值
            float[] values = event.values;
            float x = values[0];
            float y = values[1];
            float z = values[2];

            if ((Math.abs(x) > 17 || Math.abs(y) > 17 || Math.abs(z) > 17)) {
                if (mShakeTime == 1) {
                    mShakeStartTime = System.currentTimeMillis();
                }
                if (System.currentTimeMillis() - mShakeStartTime > SHAKE_TIME_LIMIT) {
                    mShakeTime = 1;
                    return;
                }
                if (mShakeTime >= SHAKE_TIME_TO_SHOW_LOG_DIALOG) {
                    mLogDialog.show();
                    mShakeTime = 1;
                    return;
                }
                mShakeTime++;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // DO NOTHING
    }
}
