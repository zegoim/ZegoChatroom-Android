package com.zego.chatroom.demo;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zego.chatroom.ZegoChatroom;
import com.zego.chatroom.block.ZegoOperationGroupBlock;
import com.zego.chatroom.callback.ZegoChatroomCMDCallback;
import com.zego.chatroom.callback.ZegoChatroomCallback;
import com.zego.chatroom.callback.ZegoChatroomIMCallback;
import com.zego.chatroom.callback.ZegoChatroomSendRoomMessageCallback;
import com.zego.chatroom.callback.ZegoSeatUpdateCallback;
import com.zego.chatroom.config.ZegoChatroomAudioReverbConfig;
import com.zego.chatroom.config.ZegoChatroomLiveConfig;
import com.zego.chatroom.constants.ZegoChatroomLoginEvent;
import com.zego.chatroom.constants.ZegoChatroomLoginStatus;
import com.zego.chatroom.constants.ZegoChatroomReconnectStopReason;
import com.zego.chatroom.constants.ZegoChatroomSeatStatus;
import com.zego.chatroom.constants.ZegoChatroomUserLiveStatus;
import com.zego.chatroom.demo.adapter.ChatroomSeatsAdapter;
import com.zego.chatroom.demo.adapter.MsgAdapter;
import com.zego.chatroom.demo.adapter.SoundEffectViewAdapter;
import com.zego.chatroom.demo.bean.ChatroomSeatInfo;
import com.zego.chatroom.demo.data.ZegoDataCenter;
import com.zego.chatroom.demo.utils.ChatroomInfoHelper;
import com.zego.chatroom.demo.utils.SystemUtil;
import com.zego.chatroom.demo.utils.UiUtils;
import com.zego.chatroom.demo.view.GridItemDecoration;
import com.zego.chatroom.demo.view.MusicPlayerDialog;
import com.zego.chatroom.demo.view.PickUpUserSelectDialog;
import com.zego.chatroom.demo.view.SeatOperationDialog;
import com.zego.chatroom.demo.view.SoundEffectDialog;
import com.zego.chatroom.demo.view.TipDialog;
import com.zego.chatroom.entity.ZegoChatroomMessage;
import com.zego.chatroom.entity.ZegoChatroomSeat;
import com.zego.chatroom.entity.ZegoChatroomUser;
import com.zego.chatroom.manager.entity.ResultCode;
import com.zego.chatroom.manager.musicplay.ZegoMusicResource;
import com.zego.chatroom.manager.room.ZegoUserLiveQuality;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ChatroomActivity extends BaseActivity implements ZegoChatroomCallback, View.OnClickListener,
        SoundEffectViewAdapter.OnSoundEffectAuditionCheckedListener, SoundEffectViewAdapter.OnSoundEffectChangedListener,
        ChatroomSeatsAdapter.OnChatroomSeatClickListener, SeatOperationDialog.OnOperationItemClickListener,
        PickUpUserSelectDialog.OnPickUserUpListener, ZegoChatroomIMCallback, ZegoChatroomCMDCallback {

    private final static String TAG = ChatroomActivity.class.getSimpleName();

    private final static int DEFAULT_SEATS_COUNT = 9;

    private final static String USER_MESSAGE_FORMAT = "%1$s:%2$s";


    /**
     * Intent extra info
     */
    final static String EXTRA_KEY_OWNER_ID = "owner_id";
    final static String EXTRA_KEY_OWNER_NAME = "owner_name";
    final static String EXTRA_KEY_ROOM_ID = "room_id";
    final static String EXTRA_KEY_ROOM_NAME = "room_name";
    final static String EXTRA_KEY_AUDIO_BITRATE = "audio_bitrate";
    final static String EXTRA_KEY_AUDIO_CHANNEL_COUNT = "audio_channel_count";
    final static String EXTRA_KEY_LATENCY_MODE = "latency_mode";

    private ZegoChatroomUser mOwner;

    private View mFlLoading;

    private List<ChatroomSeatInfo> mSeats = new ArrayList<>();

    private ChatroomSeatsAdapter mSeatsAdapter;
    private MsgAdapter mMsgAdapter;


    private TextView mTvMicState;
    private TextView mTvSpeakerState;
    private TextView mTvBackgroundState;

    private SoundEffectDialog mSoundEffectDialog;
    private SeatOperationDialog mSeatOperationDialog;
    private PickUpUserSelectDialog mPickUpUserSelectDialog;
    private MusicPlayerDialog mMusicPlayerDialog;
    private TipDialog mTipDialog;

    private Set<ZegoChatroomUser> mRoomUsers = new HashSet<>();


    private int screenHeight = 0;
    private FrameLayout mFlInput;
    private EditText mEtComment;

    // 当前房间支持声道数
    private int mAudioChannelCount;

    // 是否正在离开房间
    private boolean isLeavingRoom = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        // 将房间配置设置成默认状态：Mic开，静音关
        ZegoChatroom.shared().muteSpeaker(false);
        ZegoChatroom.shared().muteMic(false);
        // 无限重试
        ZegoChatroom.shared().setReconnectTimeoutSec(0);
        // 允许接收用户更新回调
        ZegoChatroom.shared().setEnableUserStateUpdate(true);
        ZegoChatroom.shared().addZegoChatroomCallback(this);
        ZegoChatroom.shared().addIMCallback(this);
        ZegoChatroom.shared().addCMDCallback(this);

        initData();
        initView();

        loginChatroomWithIntent(getIntent());

        // 到这才能获取到房主到信息。。
        mTvBackgroundState.setVisibility(isOwner() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 当暂停时，强制隐藏输入框和输入法
        mFlInput.setVisibility(View.GONE);
        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(mEtComment.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void initData() {
        mSeats = createDefaultSeats();
        mSeatsAdapter = new ChatroomSeatsAdapter();
        mSeatsAdapter.setSeats(mSeats);
        mSeatsAdapter.setOnChatroomSeatClickListener(this);

        mMsgAdapter = new MsgAdapter();
    }

    private void initView() {
        mFlLoading = findViewById(R.id.fl_loading);
        mFlLoading.setVisibility(View.VISIBLE);

        mTvMicState = findViewById(R.id.tv_mic_state);
        mTvSpeakerState = findViewById(R.id.tv_speaker_state);
        mTvBackgroundState = findViewById(R.id.tv_background_music_state);
        mFlInput = findViewById(R.id.fl_input);
        mEtComment = findViewById(R.id.comment_edit_text);


        mTvMicState.setText("Mic开");
        mTvMicState.setTag(false);

        mTvSpeakerState.setText("静音关");
        mTvSpeakerState.setTag(false);

        // 背景音乐当前状态由开发者自行控制。
        mTvBackgroundState.setText("背景音乐");

        mTvMicState.setOnClickListener(this);
        mTvSpeakerState.setOnClickListener(this);
        mTvBackgroundState.setOnClickListener(this);
        findViewById(R.id.tv_exit_room).setOnClickListener(this);
        findViewById(R.id.tv_sound_effect).setOnClickListener(this);
        findViewById(R.id.tv_comment).setOnClickListener(this);
        findViewById(R.id.tv_send_msg).setOnClickListener(this);

        mPickUpUserSelectDialog = new PickUpUserSelectDialog(this);
        mPickUpUserSelectDialog.setOnPickUpUserListener(this);

        initGridRecyclerView();
        initMessageRecyclerView();

        initInputLayout();
    }

    /**
     * 初始化输入框显示layout显示、隐藏逻辑
     * 主要是监控输入法的显示，然后将输入框显示或者隐藏
     */
    private void initInputLayout() {
        final ViewGroup rootLayout = findViewById(R.id.root_layout);
        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootLayout.getWindowVisibleDisplayFrame(r);
                // 存储r.bottom
                screenHeight = Math.max(screenHeight, r.bottom);
                //r.top 是状态栏高度
                final int softHeight = screenHeight - r.bottom;
                if (softHeight > 100) {//当输入法高度大于100判定为输入法打开了
                    mFlInput.postDelayed(new Runnable() {
                                             @Override
                                             public void run() {
                                                 mFlInput.scrollTo(0, softHeight);
                                             }
                                         }
                            , 100);
                    mFlInput.setVisibility(View.VISIBLE);
                    mEtComment.requestFocus();
                } else {//否则判断为输入法隐藏了
                    mFlInput.scrollTo(0, 0);
                    mFlInput.setVisibility(View.GONE);
                }
            }
        });
    }

    private void initGridRecyclerView() {
        RecyclerView rvSeats = findViewById(R.id.rv_seats);
        rvSeats.setAdapter(mSeatsAdapter);
        rvSeats.setLayoutManager(new GridLayoutManager(this, 3));

        GridItemDecoration.Builder builder = new GridItemDecoration.Builder(this);
        builder.setColor(Color.BLACK);
        builder.setVerticalSpan(UiUtils.dp2px(1));
        builder.setHorizontalSpan(UiUtils.dp2px(1));
        rvSeats.addItemDecoration(builder.build());
    }

    private void initMessageRecyclerView() {
        RecyclerView rvMessage = findViewById(R.id.rv_message);
        rvMessage.setAdapter(mMsgAdapter);
        mMsgAdapter.setRecyclerView(rvMessage);
        rvMessage.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }


    private List<ChatroomSeatInfo> createDefaultSeats() {
        ArrayList<ChatroomSeatInfo> seats = new ArrayList<>(DEFAULT_SEATS_COUNT);

        for (int i = 0; i < DEFAULT_SEATS_COUNT; i++) {
            seats.add(ChatroomSeatInfo.emptySeat());
        }

        return seats;
    }

    /**
     * 登入房间内部实现
     */
    private void loginChatroomWithIntent(Intent intent) {
        mOwner = new ZegoChatroomUser();
        mOwner.userID = intent.getStringExtra(EXTRA_KEY_OWNER_ID);
        mOwner.userName = intent.getStringExtra(EXTRA_KEY_OWNER_NAME);

        joinChatroomWithIntent(intent);
    }

    private List<ZegoChatroomSeat> createDefaultZegoSeats() {
        ArrayList<ZegoChatroomSeat> seats = new ArrayList<>(DEFAULT_SEATS_COUNT);
        // 默认房主上麦
        ZegoChatroomSeat ownerSeat = ZegoChatroomSeat.seatForUser(ZegoDataCenter.ZEGO_USER);
        seats.add(ownerSeat);

        for (int i = 1; i < DEFAULT_SEATS_COUNT; i++) {
            seats.add(ZegoChatroomSeat.emptySeat());
        }

        return seats;
    }

    private void joinChatroomWithIntent(Intent intent) {
        String roomID = intent.getStringExtra(EXTRA_KEY_ROOM_ID);
        String roomName = intent.getStringExtra(EXTRA_KEY_ROOM_NAME);
        int audioBitrate = intent.getIntExtra(EXTRA_KEY_AUDIO_BITRATE, ChatroomInfoHelper.DEFAULT_AUDIO_BITRATE);
        mAudioChannelCount = intent.getIntExtra(EXTRA_KEY_AUDIO_CHANNEL_COUNT, ChatroomInfoHelper.DEFAULT_AUDIO_CHANNEL_COUNT);
        int latencyMode = intent.getIntExtra(EXTRA_KEY_LATENCY_MODE, ChatroomInfoHelper.DEFAULT_LATENCY_MODE);

        ZegoChatroomLiveConfig config = new ZegoChatroomLiveConfig();
        config.setBitrate(audioBitrate);
        config.setAudioChannelCount(mAudioChannelCount);
        config.setLatencyMode(latencyMode);

        ZegoChatroom.shared().joinChatroom(roomID, roomName, createDefaultZegoSeats(), config);
    }

    private void exitRoom() {
        if (isLeavingRoom) {
            return;
        }
        isLeavingRoom = true;

        boolean shouldLeaveSeat = (getSeatForUser(ZegoDataCenter.ZEGO_USER) != null);
        if (shouldLeaveSeat) {
            ZegoChatroom.shared().leaveSeat(new ZegoSeatUpdateCallbackWrapper() {
                @Override
                public void onCompletion(ResultCode resultCode) {
                    super.onCompletion(resultCode);
                    boolean isSuccess = resultCode.isSuccess();
                    if (!isSuccess) {
                        Toast.makeText(ChatroomActivity.this, "下麦失败", Toast.LENGTH_SHORT).show();
                    } else {
                        ZegoChatroom.shared().getMusicPlayer().stop();
                    }
                    exitRoomInner();
                }
            });
        } else {
            exitRoomInner();
        }
    }

    private void exitRoomInner() {
        releaseDialog();

        // 重置音效相关设置
        ZegoChatroom.shared().setVoiceChangeValue(0.0f);
        ZegoChatroom.shared().setVirtualStereoAngle(90);
        ZegoChatroom.shared().setAudioReverbConfig(null);
        ZegoChatroom.shared().setEnableLoopback(false);

        ZegoChatroom.shared().leaveRoom();
        ZegoChatroom.shared().removeZegoChatroomCallback(this);
        ZegoChatroom.shared().removeIMCallback(this);
        ZegoChatroom.shared().removeCMDCallback(this);

        finish();
    }

    private void releaseDialog() {
        mTipDialog = null;
        mMusicPlayerDialog = null;
        if (mSoundEffectDialog != null) {
            mSoundEffectDialog.release();
            mSoundEffectDialog = null;

        }
        if (mSeatOperationDialog != null) {
            mSeatOperationDialog.setOnOperationItemClickListener(null);
            mSeatOperationDialog = null;
        }
        if (mPickUpUserSelectDialog != null) {
            mPickUpUserSelectDialog.setOnPickUpUserListener(null);
            mPickUpUserSelectDialog = null;
        }
    }

    private void playBackgroundMusic() {
        if (!isOwner()) {
            Toast.makeText(this, "只有房主才能播放背景音乐", Toast.LENGTH_SHORT).show();
            return;
        }
        if (getSeatForUser(ZegoDataCenter.ZEGO_USER) == null) {
            Toast.makeText(this, "上麦成功后才能播放背景音乐", Toast.LENGTH_SHORT).show();
        } else {
            showMusicPlayerDialog();
        }
    }

    private void changeMicState() {
        boolean isMicMute = isMicMute();
        if (isMicMute) {
            mTvMicState.setText("Mic开");
            mTvMicState.setTag(false);
            ZegoChatroom.shared().muteMic(false);
        } else {
            mTvMicState.setText("Mic关");
            mTvMicState.setTag(true);
            ZegoChatroom.shared().muteMic(true);
        }
    }

    private boolean isMicMute() {
        return (boolean) mTvMicState.getTag();
    }

    private boolean isSpeakerMute() {
        return (boolean) mTvSpeakerState.getTag();
    }

    private void changeSpeakerState() {
        boolean isSpeakerMute = isSpeakerMute();
        if (isSpeakerMute) {
            mTvSpeakerState.setText("静音关");
            mTvSpeakerState.setTag(false);
            ZegoChatroom.shared().muteSpeaker(false);
        } else {
            mTvSpeakerState.setText("静音开");
            mTvSpeakerState.setTag(true);
            ZegoChatroom.shared().muteSpeaker(true);
        }
    }

    private void showSoundEffectDialog() {
        if (mSoundEffectDialog == null) {
            // 该房间是否支持立体声
            boolean isStereo = mAudioChannelCount == 2;
            mSoundEffectDialog = new SoundEffectDialog(this, isStereo);
            mSoundEffectDialog.setOnSoundEffectAuditionCheckedListener(this);
            mSoundEffectDialog.setOnSoundEffectChangedListener(this);
        }
        mSoundEffectDialog.show();
    }

    private void showMusicPlayerDialog() {
        if (mMusicPlayerDialog == null) {
            mMusicPlayerDialog = new MusicPlayerDialog(this);

            List<ZegoMusicResource> playList = getPlayList();
            mMusicPlayerDialog.setPlayList(playList);

            mMusicPlayerDialog.setSoundEffectResource(new ZegoMusicResource(SystemUtil.copyAssetsFile2Phone(this, "laugth.wav"), "大笑"));
        }
        mMusicPlayerDialog.show();
    }

    private List<ZegoMusicResource> getPlayList() {
        List<ZegoMusicResource> playList = new ArrayList<>(4);

        playList.add(new ZegoMusicResource(SystemUtil.copyAssetsFile2Phone(this, "zhuoniqiu.mp3"), "抓泥鳅"));
        playList.add(new ZegoMusicResource(SystemUtil.copyAssetsFile2Phone(this, "haikuotiankong.mp3"), "海阔天空"));
        playList.add(new ZegoMusicResource(SystemUtil.copyAssetsFile2Phone(this, "shishangzhiyoumamahao.mp3"), "世上只有妈妈好"));
        playList.add(new ZegoMusicResource("http://www.ytmp3.cn/down/59249.mp3", "假如-网络资源"));

        return playList;
    }

    /**
     * @return 是否发送消息成功，只要输入框有内容即输入成功
     */
    public boolean send() {
        final String msg = getEditTextContent();
        if (msg == null) {
            Toast.makeText(this, "请输入需要发送的内容", Toast.LENGTH_LONG).show();
            return false;
        } else {
            ZegoChatroom.shared().sendRoomMessage(msg, 11, new ZegoChatroomSendRoomMessageCallback() {
                @Override
                public void onSendRoomMessage(ResultCode resultCode, ZegoChatroomMessage message) {
                    if (resultCode.isSuccess()) {
                        mMsgAdapter.addRoomMsg(String.format(Locale.CHINA, USER_MESSAGE_FORMAT, ZegoDataCenter.ZEGO_USER.userName, message.mContent));
                    } else {
                        mMsgAdapter.addRoomMsg("系统:消息发送失败");
                    }
                }
            });
            return true;
        }
    }

    private String getEditTextContent() {
        Editable msg = mEtComment.getText();
        if (msg != null && !TextUtils.isEmpty(msg.toString())) {
            String s = msg.toString();
            mEtComment.setText("");
            return s;
        }
        return null;
    }

    private void showOperationMenu(ChatroomSeatInfo seatInfo) {
        int position = mSeats.indexOf(seatInfo);
        if (position == -1) {
            return;
        }
        if (mSeatOperationDialog == null) {
            mSeatOperationDialog = new SeatOperationDialog(this);
            mSeatOperationDialog.setOnOperationItemClickListener(this);
        }
        boolean isOnMic = getSeatForUser(ZegoDataCenter.ZEGO_USER) != null;
        mSeatOperationDialog.adaptBySeatInfo(position, seatInfo, isOwner(), isOnMic);
        mSeatOperationDialog.show();
    }

    private void showPickUpUserSelectDialog(int pickUpTargetIndex) {
        mPickUpUserSelectDialog.setPickUpTargetIndex(pickUpTargetIndex);
        mPickUpUserSelectDialog.show();
    }

    private void showPickedUpTipDialog(int seatIndex) {
        // 获取麦克风当前状态，如果是打开的，则改变其状态
        final boolean shouldChangeMicState = !isMicMute();
        if (shouldChangeMicState) {
            changeMicState();
        }

        if (mTipDialog == null) {
            mTipDialog = new TipDialog(this);
        }
        mTipDialog.reset();
        mTipDialog.mTitleTv.setText("你被抱上" + seatIndex + "号麦位");
        mTipDialog.mDescTv.setText("快打开麦克风聊天吧");
        mTipDialog.mButton1.setText("下麦");
        mTipDialog.mButton1.setVisibility(View.VISIBLE);
        mTipDialog.mButtonOk.setText("确定");
        mTipDialog.mButtonOk.setVisibility(View.VISIBLE);
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.button1:
                        ZegoChatroom.shared().leaveSeat(new ZegoSeatUpdateCallbackWrapper() {
                            @Override
                            public void onCompletion(ResultCode resultCode) {
                                super.onCompletion(resultCode);
                                if (resultCode.isSuccess()) {
                                    mTipDialog.dismiss();
                                    // 将 麦克风状态回复到执行之前到状态
                                    if (shouldChangeMicState) {
                                        changeMicState();
                                    }
                                } else {
                                    Toast.makeText(ChatroomActivity.this, "下麦失败，请重试！", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        break;
                    case R.id.button_ok:
                        mTipDialog.dismiss();
                        // 将 麦克风状态回复到执行之前到状态
                        if (shouldChangeMicState) {
                            changeMicState();
                        }
                        break;
                }
            }
        };
        mTipDialog.mButton1.setOnClickListener(onClickListener);
        mTipDialog.mButtonOk.setOnClickListener(onClickListener);
        mTipDialog.show();
    }

    private void notifyPickUpUserDataSet() {
        List<ZegoChatroomUser> mPickUsers = new ArrayList<>();
        for (ZegoChatroomUser user : mRoomUsers) {
            if (getSeatForUser(user) == null) {
                mPickUsers.add(user);
            }
        }
        mPickUpUserSelectDialog.setUserList(mPickUsers);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_exit_room:
                exitRoom();
                break;
            case R.id.tv_background_music_state:
                playBackgroundMusic();
                break;
            case R.id.tv_mic_state:
                changeMicState();
                break;
            case R.id.tv_speaker_state:
                changeSpeakerState();
                break;
            case R.id.tv_sound_effect:
                showSoundEffectDialog();
                break;
            case R.id.tv_comment:
                // 点击评论按钮，显示输入法
                mFlInput.setVisibility(View.VISIBLE);
                mEtComment.requestFocus();
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                        .showSoftInput(mEtComment, InputMethodManager.SHOW_IMPLICIT);
                break;
            case R.id.tv_send_msg:
                if (send()) {
                    // 隐藏输入法
                    InputMethodManager im = ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE));
                    if (im.isActive()) {
                        im.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        exitRoom();
    }

    // --------------- implements ZegoChatroomCallback --------------- //
    @Override
    public void onSeatsUpdate(List<ZegoChatroomSeat> zegoChatroomSeats) {
        if (zegoChatroomSeats.size() != mSeats.size()) {
            Log.w(TAG, "onSeatsUpdate zegoChatroomSeats.size() != mSeats.size() ");
            return;
        }
        for (int i = 0; i < zegoChatroomSeats.size(); i++) {
            ChatroomSeatInfo seatInfo = mSeats.get(i);
            ZegoChatroomSeat zegoChatroomSeat = zegoChatroomSeats.get(i);
            if (zegoChatroomSeat.mStatus == ZegoChatroomSeatStatus.Used) {
                if (zegoChatroomSeat.mZegoUser.equals(seatInfo.mUser)) {
                    seatInfo.isMute = zegoChatroomSeat.isMute;
                    continue;
                }
            }
            seatInfo.mStatus = zegoChatroomSeat.mStatus;
            seatInfo.mUser = zegoChatroomSeat.mZegoUser;
            seatInfo.isMute = zegoChatroomSeat.isMute;
            seatInfo.mSoundLevel = 0;
            seatInfo.mDelay = 0;
            seatInfo.mLiveStatus = ZegoChatroomUserLiveStatus.WAIT_CONNECT;
        }
        mSeatsAdapter.notifyDataSetChanged();

        notifyPickUpUserDataSet();
    }

    @Override
    public void onLoginEventOccur(int event, int status, ResultCode errorCode) {
        Log.d(TAG, "onLoginEventOccur event: " + event + " status: " + status + " errorCode: " + errorCode);

        mMsgAdapter.addRoomMsg("系统:onLoginEventOccur  event: " + ZegoChatroomLoginEvent.getLoginEventString(event) + " status: " + ZegoChatroomLoginStatus.getLoginStatusString(status) + " errorCode: " + errorCode);

        if (event == ZegoChatroomLoginEvent.LOGIN_SUCCESS) {
            mFlLoading.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAutoReconnectStop(int stopReason) {
        Log.d(TAG, "onAutoReconnectStop stopReason: " + stopReason);

        mMsgAdapter.addRoomMsg("系统:onAutoReconnectStop  stopReason: " + ZegoChatroomReconnectStopReason.getReconnectStopReasonString(stopReason));
        mFlLoading.setVisibility(View.GONE);
    }

    @Override
    public void onLiveStatusUpdate(ZegoChatroomUser user, int liveStatus) {
        Log.d(TAG, "onLiveStatusUpdate user: " + user + " liveStatus: " + liveStatus);
        setLiveStatusForUser(user, liveStatus);
    }

    @Override
    public void onLiveQualityUpdate(ZegoChatroomUser user, ZegoUserLiveQuality quality) {
        Log.v(TAG, "onLiveQualityUpdate user: " + user + " quality: " + quality);
        setSeatDelayForUser(user, quality.mAudioDelay);
    }

    @Override
    public void onSoundLevelUpdate(ZegoChatroomUser user, float soundLevel) {
        Log.v(TAG, "onSoundLevelUpdate user: " + user + " soundLevel: " + soundLevel);
        setSeatSoundLevelForUser(user, soundLevel);
    }

    @Override
    public void onLiveExtraInfoUpdate(ZegoChatroomUser user, String extraInfo) {
        Log.d(TAG, "onLiveExtraInfoUpdate user: " + user + " extraInfo: " + extraInfo);
        mMsgAdapter.addRoomMsg("extraInfoUpdate user: " + user.userName + " extraInfo: " + extraInfo);
    }

    @Override
    public void onUserTakeSeat(ZegoChatroomUser user, int index) {
        Log.d(TAG, "onUserTakeSeat user: " + user + " index: " + index);

        mMsgAdapter.addRoomMsg("user: " + user.userName + "，上麦，位置:" + index);
    }

    @Override
    public void onUserLeaveSeat(ZegoChatroomUser user, int fromIndex) {
        Log.d(TAG, "onUserLeaveSeat user: " + user + " fromIndex: " + fromIndex);

        mMsgAdapter.addRoomMsg("user: " + user.userName + "，下麦，位置:" + fromIndex);
    }

    @Override
    public void onUserChangeSeat(ZegoChatroomUser user, int fromIndex, int toIndex) {
        Log.d(TAG, "onUserChangeSeat user: " + user + " fromIndex: " + fromIndex + " toIndex: " + toIndex);

        mMsgAdapter.addRoomMsg("user: " + user.userName + "，换麦，从:" + fromIndex + "->" + toIndex);
    }

    @Override
    public void onUserPickUp(ZegoChatroomUser fromUser, ZegoChatroomUser toUser, int index) {
        Log.d(TAG, "onUserPickUp fromUser: " + fromUser + " toUser: " + toUser + " index: " + index);

        String fromUserName = fromUser == null ? null : fromUser.userName;
        mMsgAdapter.addRoomMsg("user: " + fromUserName + "，将user: " + toUser.userName + "，抱上麦，位置:" + index);

        if (ZegoDataCenter.ZEGO_USER.equals(toUser)) {
            showPickedUpTipDialog(index);
        }
    }

    @Override
    public void onUserKickOut(ZegoChatroomUser fromUser, ZegoChatroomUser toUser, int fromIndex) {
        Log.d(TAG, "onUserKickOut fromUser: " + fromUser + " toUser: " + toUser + " fromIndex: " + fromIndex);
        if (ZegoDataCenter.ZEGO_USER.equals(toUser) && isOwner()) {
            ZegoChatroom.shared().getMusicPlayer().stop();
        }
        String fromUserName = fromUser == null ? null : fromUser.userName;
        mMsgAdapter.addRoomMsg("user: " + fromUserName + "，将user: " + toUser.userName + "，抱下麦，位置:" + fromIndex);
    }

    @Override
    public void onSeatMute(ZegoChatroomUser fromUser, boolean isMute, int index) {
        Log.d(TAG, "onSeatMute fromUser: " + fromUser + " isMute: " + isMute + " index: " + index);

        String muteOperation = isMute ? "禁" : "解禁";
        mMsgAdapter.addRoomMsg("user: " + fromUser.userName + "，" + muteOperation + "麦位，位置:" + index);
    }

    @Override
    public void onSeatClose(ZegoChatroomUser fromUser, boolean isClose, int index) {
        Log.d(TAG, "onSeatClose fromUser: " + fromUser + " isMute: " + isClose + " index: " + index);

        String muteOperation = isClose ? "封" : "解封";
        mMsgAdapter.addRoomMsg("user: " + fromUser.userName + "，" + muteOperation + "麦位，位置:" + index);
    }

    @Override
    public void onAVEngineStop() {

    }

    // --------------- implements SoundEffectViewAdapter.OnSoundEffectChangedListener --------------- //

    /**
     * 设置相关变声
     *
     * @param soundEffectType 音效类型
     */
    @Override
    public void onSoundEffectChanged(int soundEffectType) {
        switch (soundEffectType) {
            case SoundEffectViewAdapter.SOUND_EFFECT_TYPE_VOICE_CHANGE_NO:
                // 变声-无
                ZegoChatroom.shared().setVoiceChangeValue(0.0f);
                break;
            case SoundEffectViewAdapter.SOUND_EFFECT_TYPE_VOICE_CHANGE_LOLI:
                // 变声-萝莉
                ZegoChatroom.shared().setVoiceChangeValue(7f);
                break;
            case SoundEffectViewAdapter.SOUND_EFFECT_TYPE_VOICE_CHANGE_UNCLE:
                // 变声-大叔
                ZegoChatroom.shared().setVoiceChangeValue(-3f);
                break;
            case SoundEffectViewAdapter.SOUND_EFFECT_TYPE_STEREO_NO:
                // 立体声-无
                ZegoChatroom.shared().setVirtualStereoAngle(90);
                break;
            case SoundEffectViewAdapter.SOUND_EFFECT_TYPE_STEREO_LEFT_SIDE:
                // 立体声-左侧声
                ZegoChatroom.shared().setVirtualStereoAngle(120);
                break;
            case SoundEffectViewAdapter.SOUND_EFFECT_TYPE_STEREO_RIGHT_SIDE:
                // 立体声-右侧声
                ZegoChatroom.shared().setVirtualStereoAngle(60);
                break;
            case SoundEffectViewAdapter.SOUND_EFFECT_TYPE_MIXED_VOICE_NO:
                // 混响-无
                ZegoChatroom.shared().setAudioReverbConfig(null);
                break;
            case SoundEffectViewAdapter.SOUND_EFFECT_TYPE_MIXED_VOICE_LOBBY:
                //混响-大堂场景
                ZegoChatroom.shared().setAudioReverbConfig(ZegoChatroomAudioReverbConfig.configWithModeLargeRoom());
                break;
            case SoundEffectViewAdapter.SOUND_EFFECT_TYPE_MIXED_VOICE_VALLEY:
                // 混响-山谷场景
                ZegoChatroom.shared().setAudioReverbConfig(ZegoChatroomAudioReverbConfig.configWithModeValley());
                break;
        }
    }

    // --------------- implements SoundEffectViewAdapter.OnSoundEffectAuditionCheckedListener --------------- //
    @Override
    public void onSoundEffectAuditionChecked(boolean isChecked) {
        ZegoChatroom.shared().setEnableLoopback(isChecked);
        if (!isChecked) {
            Toast.makeText(this, R.string.sound_effect_audition_close_tip, Toast.LENGTH_LONG).show();
        }
    }

    // --------------- implements ChatroomSeatsAdapter.OnChatroomSeatClickListener --------------- //
    @Override
    public void onChatroomSeatClick(ChatroomSeatInfo chatroomSeatInfo) {
        showOperationMenu(chatroomSeatInfo);
    }

    // --------------- implements SeatOperationDialog.OnOperationItemClickListener --------------- //
    @Override
    public void onOperationItemClick(int position, int operationType, ChatroomSeatInfo seat) {
        switch (operationType) {
            case SeatOperationDialog.OPERATION_TYPE_TAKE_SEAT:
                ZegoChatroom.shared().takeSeatAtIndex(position, new ZegoSeatUpdateCallbackWrapper() {
                    @Override
                    public void onCompletion(ResultCode resultCode) {
                        super.onCompletion(resultCode);
                        if (!resultCode.isSuccess()) {
                            Toast.makeText(ChatroomActivity.this, "操作失败！", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            case SeatOperationDialog.OPERATION_TYPE_CHANGE_SEAT:
                ZegoChatroom.shared().changeSeatTo(position, new ZegoSeatUpdateCallbackWrapper() {
                    @Override
                    public void onCompletion(ResultCode resultCode) {
                        super.onCompletion(resultCode);
                        if (!resultCode.isSuccess()) {
                            Toast.makeText(ChatroomActivity.this, "操作失败！", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            case SeatOperationDialog.OPERATION_TYPE_LEAVE_SEAT:
                ZegoChatroom.shared().leaveSeat(new ZegoSeatUpdateCallbackWrapper() {
                    @Override
                    public void onCompletion(ResultCode resultCode) {
                        super.onCompletion(resultCode);
                        if (!resultCode.isSuccess()) {
                            Toast.makeText(ChatroomActivity.this, "操作失败！", Toast.LENGTH_SHORT).show();
                        } else {
                            ZegoChatroom.shared().getMusicPlayer().stop();
                        }
                    }
                });
                break;
            case SeatOperationDialog.OPERATION_TYPE_PICK_UP:
                showPickUpUserSelectDialog(position);
                break;
            case SeatOperationDialog.OPERATION_TYPE_KIT_OUT:
                ZegoChatroom.shared().kickOut(seat.mUser, new ZegoSeatUpdateCallbackWrapper() {
                    @Override
                    public void onCompletion(ResultCode resultCode) {
                        super.onCompletion(resultCode);
                        if (!resultCode.isSuccess()) {
                            Toast.makeText(ChatroomActivity.this, "操作失败！", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            case SeatOperationDialog.OPERATION_TYPE_MUTE_SEAT:
                ZegoChatroom.shared().muteSeat(!seat.isMute, position, new ZegoSeatUpdateCallbackWrapper() {
                    @Override
                    public void onCompletion(ResultCode resultCode) {
                        super.onCompletion(resultCode);
                        if (!resultCode.isSuccess()) {
                            Toast.makeText(ChatroomActivity.this, "操作失败！", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            case SeatOperationDialog.OPERATION_TYPE_CLOSE_SEAT:
                boolean isClosed = seat.mStatus == ZegoChatroomSeatStatus.Closed;
                ZegoChatroom.shared().closeSeat(!isClosed, position, new ZegoSeatUpdateCallbackWrapper() {
                    @Override
                    public void onCompletion(ResultCode resultCode) {
                        super.onCompletion(resultCode);
                        if (!resultCode.isSuccess()) {
                            Toast.makeText(ChatroomActivity.this, "操作失败！", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            case SeatOperationDialog.OPERATION_TYPE_MUTE_ALL_SEATS:
                ZegoChatroom.shared().runSeatOperationGroup(new ZegoOperationGroupBlock() {
                    @Override
                    public void execute() {
                        for (int i = 1; i < DEFAULT_SEATS_COUNT; i++) {
                            ZegoChatroom.shared().muteSeat(true, i, null);
                        }
                    }
                }, new ZegoSeatUpdateCallbackWrapper() {
                    @Override
                    public void onCompletion(ResultCode resultCode) {
                        super.onCompletion(resultCode);
                        if (!resultCode.isSuccess()) {
                            Toast.makeText(ChatroomActivity.this, "操作失败！", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
        }
    }

    // ----------------- implements PickUpSelectDialog.OnPickUserUpListener ----------------- //
    @Override
    public void onPickUpUser(ZegoChatroomUser user, int index) {
        ZegoChatroom.shared().pickUp(user, index, new ZegoSeatUpdateCallbackWrapper() {
            @Override
            public void onCompletion(ResultCode resultCode) {
                super.onCompletion(resultCode);
                if (!resultCode.isSuccess()) {
                    Toast.makeText(ChatroomActivity.this, "操作失败！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // ----------------- implements ZegoChatroomIMCallback ----------------- //
    @Override
    public void onRecvRoomMessage(ZegoChatroomMessage[] messageList) {
        Log.d(TAG, "onRecvRoomMessage messageList.length: " + messageList.length);
        for (ZegoChatroomMessage message : messageList) {
            Log.d(TAG, "onRecvRoomMessage message: " + message);
            mMsgAdapter.addRoomMsg(String.format(Locale.CHINA, USER_MESSAGE_FORMAT, message.mFromUser.userName, message.mContent));
        }
    }

    @Override
    public void onOnlineCountUpdate(int onlineCount) {
        Log.d(TAG, "onOnlineCountUpdate onlineCount: " + onlineCount);
    }

    @Override
    public void onUserJoin(ZegoChatroomUser[] users) {
        Log.d(TAG, "onUserJoin users.length: " + users.length);

        mRoomUsers.addAll(Arrays.asList(users));

        notifyPickUpUserDataSet();
    }

    @Override
    public void onUserLeave(ZegoChatroomUser[] users) {
        Log.d(TAG, "onUserLeave users.length: " + users.length);

        mRoomUsers.removeAll(Arrays.asList(users));

        notifyPickUpUserDataSet();
    }

    // ----------------- implements ZegoChatroomCMDCallback ----------------- //
    @Override
    public void onRecvCustomCommand(String content, ZegoChatroomUser fromUser) {

    }

    // ---------------- 工具方法 ---------------- //
    private boolean isOwner() {
        return mOwner.equals(ZegoDataCenter.ZEGO_USER);
    }

    private ChatroomSeatInfo getSeatForUser(ZegoChatroomUser user) {
        for (ChatroomSeatInfo seat : mSeats) {
            if (seat.mStatus == ZegoChatroomSeatStatus.Used && seat.mUser.equals(user)) {
                return seat;
            }
        }
        return null;
    }

    private void setSeatSoundLevelForUser(ZegoChatroomUser user, float soundLevel) {
        ChatroomSeatInfo seat = getSeatForUser(user);
        if (seat != null) {
            seat.mSoundLevel = soundLevel;
            mSeatsAdapter.notifyDataSetChanged();
        }
    }

    private void setSeatDelayForUser(ZegoChatroomUser user, int delay) {
        ChatroomSeatInfo seat = getSeatForUser(user);
        if (seat != null) {
            seat.mDelay = delay;
            mSeatsAdapter.notifyDataSetChanged();
        }
    }

    private void setLiveStatusForUser(ZegoChatroomUser user, int liveStatus) {
        ChatroomSeatInfo seat = getSeatForUser(user);
        if (seat != null) {
            seat.mLiveStatus = liveStatus;
            mSeatsAdapter.notifyDataSetChanged();
        }
    }

    // ---------------- 工具类 ---------------- //
    abstract class ZegoSeatUpdateCallbackWrapper implements ZegoSeatUpdateCallback {
        private ZegoSeatUpdateCallbackWrapper() {
            mFlLoading.setVisibility(View.VISIBLE);
        }

        @Override
        public void onCompletion(ResultCode resultCode) {
            mFlLoading.setVisibility(View.GONE);
        }
    }
}
