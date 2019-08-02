package com.zego.chatroom.demo.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import com.zego.chatroom.manager.log.ZLog;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;


/**
 * Copyright © 2016 Zego. All rights reserved.
 * des:
 */
public class SystemUtil {


    /**
     * 将文件从assets目录，考贝到 /data/data/包名/files/ 目录中。assets 目录中的文件，会不经压缩打包至APK包中，使用时还应从apk包中导出来
     *
     * @param fileName 文件名,如aaa.txt
     */
    static public String copyAssetsFile2Phone(Activity activity, String fileName) {
        Activity mActivity = activity;
        try {
            //getFilesDir() 获得当前APP的安装路径 /data/data/包名/files 目录
            File file = new File(mActivity.getFilesDir().getAbsolutePath() + File.separator + fileName);
            if (!file.exists() || file.length() == 0) {
                InputStream inputStream = mActivity.getAssets().open(fileName);
                FileOutputStream fos = new FileOutputStream(file);//如果文件不存在，FileOutputStream会自动创建文件
                int len;
                byte[] buffer = new byte[1024];
                while ((len = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                fos.flush();//刷新缓存区
                inputStream.close();
                fos.close();

            } else {

            }
            return file.getPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

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
                lp.flags |= 67108864;
                window.setAttributes(lp);
                result = true;
            }
        }

        return result;
    }


}
