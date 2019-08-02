package com.zego.chatroom.demo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.zego.chatroom.demo.adapter.ChatroomListAdapter;
import com.zego.chatroom.demo.bean.ChatroomInfo;
import com.zego.chatroom.demo.data.ZegoDataCenter;
import com.zego.chatroom.demo.utils.ChatroomInfoHelper;
import com.zego.chatroom.demo.utils.UiUtils;
import com.zego.chatroom.demo.view.CreateRoomDialog;
import com.zego.chatroom.demo.view.PaddingDecoration;
import com.zego.chatroom.manager.log.ZLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatroomListActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener,
        ChatroomListAdapter.OnChatroomClickListener, View.OnClickListener {

    private final static String TAG = ChatroomListActivity.class.getSimpleName();

    private final static String BODY_KEY = "body";
    private final static String REQUEST_KEY = "req";
    private final static String REQUEST_CHATROOM_LIST = "room_list";
    private final static String BODY_ERROR = "error";

    private final static String RESPONCE_KEY_DATA = "data";

    private final static int PERMISSIONS_REQUEST_CODE = 101;

    private final static int MESSAGE_GET_CHATROOM_LIST = 0x10;

    private Handler mUiHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_GET_CHATROOM_LIST:
                    Map<String, String> map = (Map<String, String>) msg.obj;
                    httpReturn(map.get(BODY_KEY), map.get(REQUEST_KEY));
                    break;
            }
        }
    };

    // 下拉刷新View
    private SwipeRefreshLayout mSwipeLayout;

    // 业务排队入口 RecyclerView
    private RecyclerView mRecyclerView;

    // 提示 layout
    private ViewGroup mTipLayout;
    // 提示 Title TextView
    private TextView mTipTitleTv;
    // 提示 desc TextView
    private TextView mTipDescTv;
    // 创建房间 Dialog
    private CreateRoomDialog mCreateRoomDialog;

    private ChatroomListAdapter mChatroomListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom_list);

        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mSwipeLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                refresh();
            }
        }, 1000);
    }

    /**
     * 初始化 View
     */
    private void initView() {
        TextView welcomeTv = findViewById(R.id.tv_welcome);
        mTipLayout = findViewById(R.id.tip_layout);
        mTipTitleTv = findViewById(R.id.tip_title_tv);
        mTipDescTv = findViewById(R.id.tip_desc_tv);
        mSwipeLayout = findViewById(R.id.swipe);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        welcomeTv.setText(getResources().getString(R.string.welcome_user, ZegoDataCenter.ZEGO_USER.userName));

        // 初始化QueueAdapter
        mChatroomListAdapter = new ChatroomListAdapter();
        mChatroomListAdapter.setOnChatroomClickListener(this);
        mRecyclerView.setAdapter(mChatroomListAdapter);
        mRecyclerView.addItemDecoration(new PaddingDecoration(UiUtils.dp2px(25)));

        // 初始化 SwipeLayout 下拉刷新回调
        mSwipeLayout.setOnRefreshListener(this);

        // 初始化点击事件
        findViewById(R.id.bt_create_room).setOnClickListener(this);
    }

    @Override
    public void onRefresh() {
        fetchChatroomList();
    }

    @Override
    public void onChatroomClick(ChatroomInfo chatroomInfo) {
        if (chatroomInfo == null || TextUtils.isEmpty(chatroomInfo.room_name)) {
            Toast.makeText(this, "房间错误，进入房间失败！", Toast.LENGTH_SHORT).show();
            // 有错误的房间，刷新一下。
            refresh();
            return;
        }
        if (checkOrRequestPermission(PERMISSIONS_REQUEST_CODE)) {
            joinRoom(chatroomInfo);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_create_room:
                showCreateRoomDialog();
                break;
            case R.id.bt_create_now:
                if (checkOrRequestPermission(PERMISSIONS_REQUEST_CODE)) {
                    createRoom();
                    mCreateRoomDialog.dismiss();
                }
                break;
            default:
                break;
        }
    }

    private void createRoom() {
        String audioBitrateString = mCreateRoomDialog.mEtAudioBitrate.getText().toString();
        String audioChannelCountString = mCreateRoomDialog.mEtAudioChannelCount.getText().toString();
        String latencyModeString = mCreateRoomDialog.mEtLatencyMode.getText().toString();

        String roomID = ChatroomInfoHelper.getRoomID();
        String roomName = mCreateRoomDialog.mEtRoomName.getText().toString();
        String ownerID = ZegoDataCenter.ZEGO_USER.userID;
        String ownerName = ZegoDataCenter.ZEGO_USER.userName;
        int audioBitrate = ChatroomInfoHelper.getAudioBitrateFromString(audioBitrateString);
        int audioChannelCount = ChatroomInfoHelper.getAudioChannelCountFromString(audioChannelCountString);
        int latencyMode = ChatroomInfoHelper.getLatencyModeFromString(latencyModeString);

        mCreateRoomDialog.resetInput();

        startChatroomActivity(roomID, roomName, ownerID, ownerName, audioBitrate, audioChannelCount, latencyMode);
    }

    private void joinRoom(ChatroomInfo info) {
        String roomID = info.room_id;
        String roomName = info.room_name;
        String ownerID = info.anchor_id_name;
        String ownerName = info.anchor_nick_name;
        int audioBitrate = ChatroomInfoHelper.getBitrateFromRoomName(info.room_name);
        int audioChannelCount = ChatroomInfoHelper.getAudioChannelCountFromRoomName(info.room_name);
        int latencyMode = ChatroomInfoHelper.getLatencyModeFromRoomName(info.room_name);
        startChatroomActivity(roomID, roomName, ownerID, ownerName, audioBitrate, audioChannelCount, latencyMode);
    }

    private void startChatroomActivity(String roomID, String roomName, String ownerID, String ownerName, int audioBitrate, int audioChannelCount, int latencyMode) {
        Intent intent = new Intent(this, ChatroomActivity.class);

        intent.putExtra(ChatroomActivity.EXTRA_KEY_OWNER_ID, ownerID);
        intent.putExtra(ChatroomActivity.EXTRA_KEY_OWNER_NAME, ownerName);
        intent.putExtra(ChatroomActivity.EXTRA_KEY_ROOM_ID, roomID);
        intent.putExtra(ChatroomActivity.EXTRA_KEY_ROOM_NAME, ChatroomInfoHelper.getRoomName(roomName, audioBitrate, audioChannelCount, latencyMode));
        intent.putExtra(ChatroomActivity.EXTRA_KEY_AUDIO_BITRATE, audioBitrate);
        intent.putExtra(ChatroomActivity.EXTRA_KEY_AUDIO_CHANNEL_COUNT, audioChannelCount);
        intent.putExtra(ChatroomActivity.EXTRA_KEY_LATENCY_MODE, latencyMode);

        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                boolean allPermissionGranted = true;
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        allPermissionGranted = false;
                        Toast.makeText(this, String.format("获取%s权限失败 ", permissions[i]), Toast.LENGTH_LONG).show();
                    }
                }
                if (!allPermissionGranted) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + this.getPackageName()));
                    startActivity(intent);
                }
                break;
            }
        }
    }

    // ---------------- 创建房间Dialog ---------------- //
    private void showCreateRoomDialog() {
        if (mCreateRoomDialog == null) {
            initCreateRoomDialog();
        }

        mCreateRoomDialog.mEtAudioBitrate.setText("");
        mCreateRoomDialog.mEtRoomName.setText("");

        mCreateRoomDialog.show();
    }

    private void initCreateRoomDialog() {
        mCreateRoomDialog = new CreateRoomDialog(this);
        mCreateRoomDialog.mBtCreateNow.setOnClickListener(this);
    }


    // ---------------- 获取房间列表 ---------------- //


    protected void httpUrl(final String url, final String req) {
        StringRequest request = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String body) {
                        Map<String, String> map = new HashMap<>();
                        map.put(BODY_KEY, body);
                        map.put(REQUEST_KEY, req);
                        mUiHandler.sendMessage(mUiHandler.obtainMessage(MESSAGE_GET_CHATROOM_LIST, map));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ZLog.d(TAG, "onErrorResponse error: " + error.getMessage());
                Map<String, String> map = new HashMap<>();
                map.put(BODY_KEY, BODY_ERROR);
                map.put(REQUEST_KEY, req);
                mUiHandler.sendMessage(mUiHandler.obtainMessage(MESSAGE_GET_CHATROOM_LIST, map));
            }
        });

        request.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 5000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 0;
            }

            @Override
            public void retry(VolleyError error) {

            }
        });
        getRequestQueue().add(request);
    }

    private void refresh() {
        mSwipeLayout.setRefreshing(true);
        fetchChatroomList();
    }

    private void fetchChatroomList() {
        String url = String.format(Locale.ENGLISH, ZegoDataCenter.getRoomListUrl(), ZegoDataCenter.APP_ID, ZegoDataCenter.APP_ID);
        httpUrl(url, REQUEST_CHATROOM_LIST);
    }

    private void httpReturn(String body, String req) {
        ZLog.d(TAG, "httpReturn body: " + body + " req: " + req);
        if (body != null && !BODY_ERROR.equals(body) && REQUEST_CHATROOM_LIST.equals(req)) {
            try {
                JSONArray jsonArray = JSON.parseObject(body).getJSONObject(RESPONCE_KEY_DATA).getJSONArray(REQUEST_CHATROOM_LIST);
                List<ChatroomInfo> roomListValue = JSON.parseArray(jsonArray.toJSONString(), ChatroomInfo.class);
                List<ChatroomInfo> chatroomList = new ArrayList<>();
                for (ChatroomInfo room : roomListValue) {
                    if (room.room_id.startsWith(ChatroomInfoHelper.CHATROOM_PREFIX)) {
                        chatroomList.add(room);
                    }
                }
                mChatroomListAdapter.setChatrooms(chatroomList);
                if (chatroomList.size() == 0) {
                    showNoChatroom();
                } else {
                    mTipLayout.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                }

            } catch (Exception e) {
                ZLog.w(TAG, "-->:: httpReturn error e: " + e.getMessage());
                showNoChatroom();
            }
        } else {
            showErrorTip();
        }
        // 获取到结果，停止刷新
        mSwipeLayout.setRefreshing(false);
    }

    private void showNoChatroom() {
        mRecyclerView.setVisibility(View.GONE);
        mTipLayout.setVisibility(View.VISIBLE);
        mTipTitleTv.setText("暂无房间");
        mTipDescTv.setText("您可以尝试下拉刷新，拉取最新信息\n也可以点击下方按钮创建房间");
    }

    private void showErrorTip() {
        mRecyclerView.setVisibility(View.GONE);
        mTipLayout.setVisibility(View.VISIBLE);
        mTipTitleTv.setText("拉取信息异常");
        mTipDescTv.setText("您可以尝试下拉刷新，重新拉取房间列表信息");
    }
}
