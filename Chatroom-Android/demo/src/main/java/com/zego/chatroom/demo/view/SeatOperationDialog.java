package com.zego.chatroom.demo.view;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.zego.chatroom.constants.ZegoChatroomSeatStatus;
import com.zego.chatroom.demo.R;
import com.zego.chatroom.demo.bean.ChatroomSeatInfo;
import com.zego.chatroom.demo.data.ZegoDataCenter;

public class SeatOperationDialog extends Dialog implements View.OnClickListener {

    public static final int OPERATION_TYPE_TAKE_SEAT = 0;
    public static final int OPERATION_TYPE_CHANGE_SEAT = 1;
    public static final int OPERATION_TYPE_LEAVE_SEAT = 2;
    public static final int OPERATION_TYPE_PICK_UP = 3;
    public static final int OPERATION_TYPE_KIT_OUT = 4;
    public static final int OPERATION_TYPE_MUTE_SEAT = 5;
    public static final int OPERATION_TYPE_CLOSE_SEAT = 6;
    public static final int OPERATION_TYPE_MUTE_ALL_SEATS = 7;

    private TextView mTvSeatIndex;
    private TextView mTvTakeSeat;
    private View mVTakeSeatLine;
    private TextView mTvChangeSeat;
    private View mVChangeSeatLine;
    private TextView mTvLeaveSeat;
    private View mVLeaveSeatLine;
    private TextView mTvPickUp;
    private View mVPickUpLine;
    private TextView mTvKitOut;
    private View mVKitOutLine;
    private TextView mTvMuteSeat;
    private View mVMuteSeatLine;
    private TextView mTvCloseSeat;
    private View mVCloseSeatLine;
    private TextView mTvMuteAllSeats;

    private OnOperationItemClickListener mOnOperationItemClickListener;

    private ChatroomSeatInfo mSeatInfo;
    private int mPosition;

    public SeatOperationDialog(@NonNull Context context) {
        super(context, R.style.CommonDialog);
        initView(context);
    }


    private void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_seat_operation_layout, null);
        setContentView(view);
        // 设置可以取消
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        // 设置Dialog高度位置
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        // 经计算，dialog_enqueue_mode_choose_layout的高度为250dp
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.gravity = Gravity.BOTTOM;
        // 设置没有边框
        getWindow().getDecorView().setPadding(0, 0, 0, 0);
        getWindow().setAttributes(layoutParams);

        mTvSeatIndex = findViewById(R.id.tv_seat_index);
        mTvTakeSeat = findViewById(R.id.tv_take_seat);
        mTvChangeSeat = findViewById(R.id.tv_change_seat);
        mTvLeaveSeat = findViewById(R.id.tv_leave_seat);
        mTvPickUp = findViewById(R.id.tv_pick_up);
        mTvKitOut = findViewById(R.id.tv_kick_out);
        mTvMuteSeat = findViewById(R.id.tv_mute_seat);
        mTvCloseSeat = findViewById(R.id.tv_close_seat);
        mTvMuteAllSeats = findViewById(R.id.tv_mute_all_seats);


        mVTakeSeatLine = findViewById(R.id.v_take_seat_line);
        mVChangeSeatLine = findViewById(R.id.v_change_seat_line);
        mVLeaveSeatLine = findViewById(R.id.v_leave_seat_line);
        mVPickUpLine = findViewById(R.id.v_pick_up_line);
        mVKitOutLine = findViewById(R.id.v_kick_out_line);
        mVMuteSeatLine = findViewById(R.id.v_mute_seat_line);
        mVCloseSeatLine = findViewById(R.id.v_close_seat_line);

        mTvTakeSeat.setOnClickListener(this);
        mTvChangeSeat.setOnClickListener(this);
        mTvLeaveSeat.setOnClickListener(this);
        mTvPickUp.setOnClickListener(this);
        mTvKitOut.setOnClickListener(this);
        mTvMuteSeat.setOnClickListener(this);
        mTvCloseSeat.setOnClickListener(this);
        mTvMuteAllSeats.setOnClickListener(this);
        findViewById(R.id.tv_cancel).setOnClickListener(this);
    }

    public void setOnOperationItemClickListener(OnOperationItemClickListener onOperationItemClickListener) {
        mOnOperationItemClickListener = onOperationItemClickListener;
    }

    public void adaptBySeatInfo(int position, ChatroomSeatInfo seatInfo, boolean isOwner, boolean isOnMic) {
        mTvSeatIndex.setText(position + "号麦");
        this.mSeatInfo = seatInfo;
        this.mPosition = position;

        mTvTakeSeat.setVisibility(View.GONE);
        mVTakeSeatLine.setVisibility(View.GONE);
        mTvChangeSeat.setVisibility(View.GONE);
        mVChangeSeatLine.setVisibility(View.GONE);
        mTvLeaveSeat.setVisibility(View.GONE);
        mVLeaveSeatLine.setVisibility(View.GONE);
        mTvPickUp.setVisibility(View.GONE);
        mVPickUpLine.setVisibility(View.GONE);
        mTvKitOut.setVisibility(View.GONE);
        mVKitOutLine.setVisibility(View.GONE);
        mTvMuteSeat.setVisibility(View.GONE);
        mVMuteSeatLine.setVisibility(View.GONE);
        mTvCloseSeat.setVisibility(View.GONE);
        mVCloseSeatLine.setVisibility(View.GONE);
        mTvMuteAllSeats.setVisibility(View.GONE);

        boolean isClosed = seatInfo.mStatus == ZegoChatroomSeatStatus.Closed;
        boolean isMute = seatInfo.isMute;

        int visibility = isOwner ? View.VISIBLE : View.GONE;
        mTvMuteSeat.setVisibility(visibility);
        mVMuteSeatLine.setVisibility(visibility);
        mTvCloseSeat.setVisibility(visibility);
        mVCloseSeatLine.setVisibility(visibility);
        mTvMuteAllSeats.setVisibility(visibility);
        if (isOwner) {
            mTvMuteSeat.setText(isMute ? "解禁" : "禁麦");
            mTvCloseSeat.setText(isClosed ? "解封" : "封麦");
        }

        if (seatInfo.mStatus == ZegoChatroomSeatStatus.Empty) {
            if (isOnMic) {
                // 换麦
                mTvChangeSeat.setVisibility(View.VISIBLE);
                mVChangeSeatLine.setVisibility(View.VISIBLE);
            } else {
                // 上麦
                mTvTakeSeat.setVisibility(View.VISIBLE);
                mVTakeSeatLine.setVisibility(View.VISIBLE);
            }
            if (isOwner) {
                // 抱麦
                mTvPickUp.setVisibility(View.VISIBLE);
                mVPickUpLine.setVisibility(View.VISIBLE);
            }
        } else if (seatInfo.mStatus == ZegoChatroomSeatStatus.Used) {
            if (ZegoDataCenter.ZEGO_USER.equals(seatInfo.mUser)) {
                mTvLeaveSeat.setVisibility(View.VISIBLE);
                mVLeaveSeatLine.setVisibility(View.VISIBLE);
            } else {
                if (isOwner) {
                    mTvKitOut.setVisibility(View.VISIBLE);
                    mVKitOutLine.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        dismiss();
        if (mOnOperationItemClickListener == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.tv_take_seat:
                mOnOperationItemClickListener.onOperationItemClick(mPosition, OPERATION_TYPE_TAKE_SEAT, mSeatInfo);
                break;
            case R.id.tv_change_seat:
                mOnOperationItemClickListener.onOperationItemClick(mPosition, OPERATION_TYPE_CHANGE_SEAT, mSeatInfo);
                break;
            case R.id.tv_leave_seat:
                mOnOperationItemClickListener.onOperationItemClick(mPosition, OPERATION_TYPE_LEAVE_SEAT, mSeatInfo);
                break;
            case R.id.tv_pick_up:
                mOnOperationItemClickListener.onOperationItemClick(mPosition, OPERATION_TYPE_PICK_UP, mSeatInfo);
                break;
            case R.id.tv_kick_out:
                mOnOperationItemClickListener.onOperationItemClick(mPosition, OPERATION_TYPE_KIT_OUT, mSeatInfo);
                break;
            case R.id.tv_mute_seat:
                mOnOperationItemClickListener.onOperationItemClick(mPosition, OPERATION_TYPE_MUTE_SEAT, mSeatInfo);
                break;
            case R.id.tv_close_seat:
                mOnOperationItemClickListener.onOperationItemClick(mPosition, OPERATION_TYPE_CLOSE_SEAT, mSeatInfo);
                break;
            case R.id.tv_mute_all_seats:
                mOnOperationItemClickListener.onOperationItemClick(mPosition, OPERATION_TYPE_MUTE_ALL_SEATS, mSeatInfo);
                break;
        }
    }

    public interface OnOperationItemClickListener {
        void onOperationItemClick(int position, int operationType, ChatroomSeatInfo seat);
    }

}