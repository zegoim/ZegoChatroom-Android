package com.zego.chatroom.demo.utils;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.zego.chatroom.demo.data.ZegoDataCenter;
import com.zego.zegoliveroom.constants.ZegoConstants;

import java.util.Locale;

public class ChatroomInfoHelper {

    public final static int DEFAULT_AUDIO_BITRATE = 64000; // 输入出错的默认码率为64000pbs
    public final static int DEFAULT_AUDIO_CHANNEL_COUNT = 2;     // 输入出错的默认声道数 为 2
    public final static int DEFAULT_LATENCY_MODE = 4;  // 输入出错的延时模式为 LOW3(4)

    public final static String CHATROOM_PREFIX = "#chatroom-";

    private final static String ROOM_NAME_FORMAT = "%1$s_%2$d_%3$d_%4$d";

    public static String getRoomID() {
        return CHATROOM_PREFIX + System.currentTimeMillis();
    }

    public static String getRoomName(String displayRoomName, int bitrate, int audioChannelCount, int latencyMode) {
        if (TextUtils.isEmpty(displayRoomName)) {
            displayRoomName = ZegoDataCenter.ZEGO_USER.userName;
        }
        return String.format(Locale.CHINA, ROOM_NAME_FORMAT, displayRoomName, bitrate, audioChannelCount, latencyMode);
    }

    public static String getDisplayRoomNameFromRoomName(@NonNull String roomName) {
        String[] splits = roomName.split("_");

        int size = splits.length;
        int roomNameSize = size - 3;
        StringBuilder displayRoomName = new StringBuilder();
        for (int i = 0; i < roomNameSize; i++) {
            displayRoomName.append(splits[i]);
        }
        return displayRoomName.toString();
    }

    public static int getBitrateFromRoomName(@NonNull String roomName) {
        String[] splits = roomName.split("_");

        int size = splits.length;
        int bitrateIndex = size - 3;
        return getAudioBitrateFromString(splits[bitrateIndex]);
    }

    public static int getAudioChannelCountFromRoomName(@NonNull String roomName) {
        String[] splits = roomName.split("_");

        int size = splits.length;
        int audioChannelCountIndex = size - 2;
        return getAudioChannelCountFromString(splits[audioChannelCountIndex]);
    }

    public static int getLatencyModeFromRoomName(@NonNull String roomName) {
        String[] splits = roomName.split("_");

        int size = splits.length;
        int latencyModeIndex = size - 1;
        return getLatencyModeFromString(splits[latencyModeIndex]);
    }

    public static int getAudioBitrateFromString(String audioBitrateString) {
        int audioBitrate;
        try {
            audioBitrate = Integer.parseInt(audioBitrateString);
            if (audioBitrate < 0) {
                audioBitrate = DEFAULT_AUDIO_BITRATE;
            }
        } catch (NumberFormatException e) {
            audioBitrate = DEFAULT_AUDIO_BITRATE;
        }
        return audioBitrate;
    }

    public static int getAudioChannelCountFromString(String audioChannelCountString) {
        int audioChannelCount;
        try {
            audioChannelCount = Integer.parseInt(audioChannelCountString);
            if (audioChannelCount != 1 && audioChannelCount != 2) {
                audioChannelCount = DEFAULT_AUDIO_CHANNEL_COUNT;
            }
        } catch (NumberFormatException e) {
            audioChannelCount = DEFAULT_AUDIO_CHANNEL_COUNT;
        }
        return audioChannelCount;
    }

    public static int getLatencyModeFromString(String latencyModeString) {
        int latencyMode;
        try {
            latencyMode = Integer.parseInt(latencyModeString);
            if (ZegoConstants.LatencyMode.Normal < 0 || latencyMode > ZegoConstants.LatencyMode.Normal3) {
                latencyMode = DEFAULT_LATENCY_MODE;
            }
        } catch (NumberFormatException e) {
            latencyMode = DEFAULT_LATENCY_MODE;
        }
        return latencyMode;
    }
}
