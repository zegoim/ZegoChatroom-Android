package com.zego.chatroom.demo.bean;

import com.zego.chatroom.constants.ZegoChatroomSeatStatus;
import com.zego.chatroom.entity.ZegoChatroomUser;

public class ChatroomSeatInfo {

    public int mStatus;

    public boolean isMute = false;

    public ZegoChatroomUser mUser;

    public float mSoundLevel;

    public int mDelay;

    public int mLiveStatus;

    /**
     * 获取一个新的 空 麦位
     *
     * @return 新的 空 麦位
     */
    public static ChatroomSeatInfo emptySeat() {
        ChatroomSeatInfo seat = new ChatroomSeatInfo();
        seat.mStatus = ZegoChatroomSeatStatus.Empty;
        return seat;
    }
}
