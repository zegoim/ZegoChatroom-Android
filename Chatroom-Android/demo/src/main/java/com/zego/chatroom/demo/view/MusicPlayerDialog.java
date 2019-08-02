package com.zego.chatroom.demo.view;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.zego.chatroom.ZegoChatroom;
import com.zego.chatroom.demo.R;
import com.zego.chatroom.demo.adapter.PlayListAdapter;
import com.zego.chatroom.manager.entity.ResultCode;
import com.zego.chatroom.manager.log.ZLog;
import com.zego.chatroom.manager.musicplay.ZegoMusicPlayCallback;
import com.zego.chatroom.manager.musicplay.ZegoMusicPlayMode;
import com.zego.chatroom.manager.musicplay.ZegoMusicPlayState;
import com.zego.chatroom.manager.musicplay.ZegoMusicPlayer;
import com.zego.chatroom.manager.musicplay.ZegoMusicResource;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MusicPlayerDialog extends Dialog implements ZegoMusicPlayCallback, View.OnClickListener, PlayListAdapter.OnMusicItemClickListener, SeekBar.OnSeekBarChangeListener {

    private final static String TAG = MusicPlayerDialog.class.getSimpleName();

    private TextView mTvCurrentMusic;

    private SeekBar mSbMusic;

    private ProgressBar mProgressBar;

    private TextView mTvPlayPause;

    private Timer mDurationTimer;

    private PlayListAdapter mPlayListAdapter;

    private ZegoMusicResource mSoundEffectResource;

    private ZegoMusicPlayer mMusicPlayer;

    public MusicPlayerDialog(Context context) {
        super(context, R.style.CommonDialog);

        getMusicPlayer().addMusicPlayCallback(this);

        init(context);
    }

    public void setPlayList(List<ZegoMusicResource> playList) {
        getMusicPlayer().setPlayList(playList);
        mPlayListAdapter.setPlayList(playList);
    }

    public void setSoundEffectResource(ZegoMusicResource resource) {
        mSoundEffectResource = resource;
    }

    private ZegoMusicPlayer getMusicPlayer() {
        if (mMusicPlayer == null) {
            mMusicPlayer = ZegoChatroom.shared().getMusicPlayer();
        }
        return mMusicPlayer;
    }

    private void init(Context context) {
        initDialog(context);

        initView();
    }

    private void initDialog(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_music_player_layout, null);
        setContentView(view);

        // 设置可以取消
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        // 设置Dialog高度位置
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();

        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.gravity = Gravity.BOTTOM;
        // 设置没有边框
        getWindow().getDecorView().setPadding(0, 0, 0, 0);
        getWindow().setAttributes(layoutParams);
    }

    private void initView() {
        mTvCurrentMusic = findViewById(R.id.tv_current_music);
        mSbMusic = findViewById(R.id.sb_music);
        mSbMusic.setOnSeekBarChangeListener(this);

        mProgressBar = findViewById(R.id.pb_loading);

        initPlayModelRadioGroup();

        initPlayListRecyclerView();

        initPlayerView();
    }

    private void initPlayModelRadioGroup() {
        RadioGroup rgMusicPlayModel = findViewById(R.id.rg_play_mode);
        int playMode = getMusicPlayer().getPlayMode();
        int checkId = playMode == ZegoMusicPlayMode.LOOP ? R.id.rb_loop : (playMode == ZegoMusicPlayMode.RANDOM ? R.id.rb_random : R.id.rb_repeat);
        rgMusicPlayModel.check(checkId);
        rgMusicPlayModel.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_loop:
                        getMusicPlayer().setPlayMode(ZegoMusicPlayMode.LOOP);
                        break;
                    case R.id.rb_random:
                        getMusicPlayer().setPlayMode(ZegoMusicPlayMode.RANDOM);
                        break;
                    case R.id.rb_repeat:
                        getMusicPlayer().setPlayMode(ZegoMusicPlayMode.REPEAT);
                        break;
                }
            }
        });
    }

    private void initPlayListRecyclerView() {
        RecyclerView rvPlayList = findViewById(R.id.rv_play_list);
        mPlayListAdapter = new PlayListAdapter();
        mPlayListAdapter.setOnMusicItemClickListener(this);
        rvPlayList.setAdapter(mPlayListAdapter);
        rvPlayList.setLayoutManager(new LinearLayoutManager(rvPlayList.getContext(), LinearLayoutManager.VERTICAL, false));
    }

    private void initPlayerView() {
        mTvPlayPause = findViewById(R.id.tv_play_pause);

        mTvPlayPause.setOnClickListener(this);
        findViewById(R.id.tv_stop).setOnClickListener(this);
        findViewById(R.id.tv_previous_music).setOnClickListener(this);
        findViewById(R.id.tv_next_music).setOnClickListener(this);
        findViewById(R.id.tv_play_sound_effect).setOnClickListener(this);
    }

    private void startDurationTimer() {
        if (mDurationTimer != null) {
            return;
        }
        mDurationTimer = new Timer("Timer-Music-Player-Duration");
        mDurationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                setSeekToCurrent();
            }
        }, 0, 1000);
    }

    private void endDurationTimer() {
        if (mDurationTimer != null) {
            mDurationTimer.cancel();
            mDurationTimer = null;
        }
    }

    private void setSeekToCurrent() {
        long totalDuration = getMusicPlayer().getTotalDuration();
        long currentTime = getMusicPlayer().getCurrentTime();
        if (totalDuration == 0 || currentTime <= 0) {
            mSbMusic.setProgress(0);
            return;
        }
        long currentProgress = currentTime * 100 / totalDuration;
        mSbMusic.setProgress((int) currentProgress);
    }

    @Override
    public void show() {
        if (getMusicPlayer() == null) {
            ZLog.w(TAG, "show getMusicPlayer() == null, has already been call releasePlayerManager method");
            return;
        }
        super.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_play_pause:
                if (getMusicPlayer().isPlaying()) {
                    getMusicPlayer().pause();
                } else {
                    getMusicPlayer().play();
                }
                break;
            case R.id.tv_stop:
                getMusicPlayer().stop();
                break;
            case R.id.tv_previous_music:
                getMusicPlayer().playPreviousMusic();
                break;
            case R.id.tv_next_music:
                getMusicPlayer().playNextMusic();
                break;
            case R.id.tv_play_sound_effect:
                getMusicPlayer().playSoundEffect(mSoundEffectResource);
                break;
        }
    }

    @Override
    public void onDetachedFromWindow() {
        mMusicPlayer = null;
    }

    // ---------------- implements PlayListAdapter.OnMusicItemClickListener ---------------- //
    @Override
    public void OnMusicItemClick(int position) {
        getMusicPlayer().playMusicFromIndex(position);
    }

    // ---------------- implements ZegoMusicPlayCallback ---------------- //
    @Override
    public boolean shouldPlayMusicResource(ZegoMusicResource resource) {
        return true;
    }

    @Override
    public void onMusicPlayStateChange(ZegoMusicResource resource, int playState, ResultCode errorCode) {
        if (resource == null) {
            return;
        }
        String msg;
        if (playState == ZegoMusicPlayState.PLAYING) {
            msg = "开始播放-" + resource.getName();
            mTvCurrentMusic.setText(msg);
            mTvPlayPause.setText("暂停");
            startDurationTimer();
        } else {
            mTvPlayPause.setText("开始");
            endDurationTimer();
            if (playState == ZegoMusicPlayState.PAUSED) {
                msg = "暂停播放-" + resource.getName();
                mTvCurrentMusic.setText(msg);
            } else {
                msg = "停止播放-" + resource.getName();
                mTvCurrentMusic.setText("");
            }
        }
        ZegoChatroom.shared().setLiveExtraInfo(msg);
        mPlayListAdapter.setCurrentPlayingResource(getMusicPlayer().getCurrentResource());
    }

    @Override
    public void onMusicBufferStateChange(boolean isBuffering) {
        mProgressBar.setVisibility(isBuffering ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onMusicSeekTo(long time) {
        ZLog.d(TAG, "onMusicSeekTo time: " + time);
    }

    @Override
    public void onSoundEffectStart(ZegoMusicResource resource) {
        ZLog.d(TAG, "onSoundEffectStart resource: " + resource.getName());
    }

    // ----------------- implements SeekBar.OnSeekBarChangeListener ----------------- //
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            if (getMusicPlayer().isPlaying()) {
                long totalDuration = getMusicPlayer().getTotalDuration();
                long seekToTime = totalDuration * progress / 100;
                getMusicPlayer().seekToTime(seekToTime);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // DO NOTHING
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // DO NOTHING
    }
}
