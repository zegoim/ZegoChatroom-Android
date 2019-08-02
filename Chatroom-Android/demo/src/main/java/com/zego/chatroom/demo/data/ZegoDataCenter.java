package com.zego.chatroom.demo.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;

import com.zego.chatroom.demo.application.BaseApplication;
import com.zego.chatroom.entity.ZegoChatroomUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

/**
 * Zego App ID 和 Sign Data，需从Zego主页申请。
 */
public class ZegoDataCenter {

    public static final boolean IS_TEST_ENV = true;  // TODO 默认使用测试环境，正式上线的时候，需修改为正式环境

    private static final String SP_NAME = "sp_name_base";
    private static final String SP_KEY_USER_ID = "sp_key_user_id";
    private static final String SP_KEY_USER_NAME = "sp_key_user_name";

    private static final String GET_ROOM_LIST_URL_DEV = "https://liveroom%d-api.zego.im/demo/roomlist?appid=%d";

    private static  final String GET_ROOM_LIST_URL_TEST = "https://test2-liveroom-api.zego.im/demo/roomlist?appid=%d";

    public static final long APP_ID = ;  // 请联系技术支持获取APP_ID


    public static final byte[] APP_SIGN = ;   // 请联系技术支持获取APP_SIGN

    public static final ZegoChatroomUser ZEGO_USER = new ZegoChatroomUser(); // 根据自己情况初始化唯一识别USER

    static {
        ZEGO_USER.userID = getUserID(); // 使用 SERIAL 作为用户的唯一识别
        ZEGO_USER.userName = getUserName();
    }

    /**
     * 获取保存的UserName，如果没有，则新建
     */
    private static String getUserID() {
        SharedPreferences sp = BaseApplication.sApplication.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        String userID = sp.getString(SP_KEY_USER_ID, "");
        if (TextUtils.isEmpty(userID)) {
            userID = UUID.randomUUID().toString();
            // 保存用户名
            sp.edit().putString(SP_KEY_USER_ID, userID).apply();
        }
        return userID;
    }

    /**
     * 获取保存的UserName，如果没有，则新建
     */
    private static String getUserName() {
        SharedPreferences sp = BaseApplication.sApplication.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        String userName = sp.getString(SP_KEY_USER_NAME, "");
        if (TextUtils.isEmpty(userName)) {
            String monthAndDay = new SimpleDateFormat("MMdd", Locale.CHINA).format(new Date());
            // 以设备名称 + 时间日期 + 一位随机数  作为用户名
            userName = Build.MODEL + monthAndDay + new Random().nextInt(10);
            // 保存用户名
            sp.edit().putString(SP_KEY_USER_NAME, userName).apply();
        }
        return userName;
    }

    public static String getRoomListUrl() {
        if (IS_TEST_ENV) {
            return GET_ROOM_LIST_URL_TEST;
        } else {
            return GET_ROOM_LIST_URL_DEV;
        }
    }
}
