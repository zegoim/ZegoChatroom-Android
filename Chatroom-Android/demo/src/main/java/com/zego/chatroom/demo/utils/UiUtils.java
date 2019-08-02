package com.zego.chatroom.demo.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import com.zego.chatroom.manager.log.ZLog;
import android.util.TypedValue;
import android.view.Window;
import android.view.WindowManager;

import com.zego.chatroom.demo.application.BaseApplication;

import java.lang.reflect.Field;


/**
 * Copyright © 2016 Zego. All rights reserved.
 * des:
 */
public class UiUtils {
    @TargetApi(19)
    public static boolean setImmersedWindow(Window window, boolean immersive) {
        boolean result = false;
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();

            if (Build.VERSION.SDK_INT < 19) {
                try {
                    int trans_status = 64;
                    Field flags = lp.getClass().getDeclaredField("meizuFlags");
                    flags.setAccessible(true);
                    int value = flags.getInt(lp);
                    if (immersive) {
                        value |= trans_status;
                    } else {
                        value &= ~trans_status;
                    }

                    flags.setInt(lp, value);
                    result = true;
                } catch (Exception var7) {
                    ZLog.e("StatusBar", "setImmersedWindow: failed");
                }
            } else {
                lp.flags |= 67108864; // WindowManager.LayoutParams.FLAG_LAYOUT_ATTACHED_IN_DECOR
                window.setAttributes(lp);
                result = true;
            }
        }

        return result;
    }

    // 沉浸式状态栏
    public static int getStatusBarHeight(Context context) {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int height = Integer.parseInt(field.get(obj).toString());
            return context.getResources().getDimensionPixelSize(height);
        } catch (Exception var5) {
            var5.printStackTrace();
            return 75;
        }
    }

    public static int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                BaseApplication.sApplication.getResources().getDisplayMetrics());
    }
}
