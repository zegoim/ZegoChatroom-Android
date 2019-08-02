package com.zego.chatroom.demo.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zego.chatroom.constants.ZegoChatroomSeatStatus;
import com.zego.chatroom.constants.ZegoChatroomUserLiveStatus;
import com.zego.chatroom.demo.R;
import com.zego.chatroom.demo.bean.ChatroomSeatInfo;
import com.zego.chatroom.demo.data.ZegoDataCenter;

import java.text.DecimalFormat;
import java.util.List;

public class ChatroomSeatsAdapter extends RecyclerView.Adapter<ChatroomSeatsAdapter.ViewHolder> {

    private List<ChatroomSeatInfo> mSeats;

    private StringBuilder mSeatStatus = new StringBuilder();

    private OnChatroomSeatClickListener mOnChatroomSeatClickListener;

    private DecimalFormat mSoundLevelFormat = new DecimalFormat("0.00");

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chatroom_seat_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        ChatroomSeatInfo seat = mSeats.get(position);

        if (seat.mStatus == ZegoChatroomSeatStatus.Empty || seat.mStatus == ZegoChatroomSeatStatus.Closed) {
            String status = (seat.mStatus == ZegoChatroomSeatStatus.Empty ? "空，" : "封，");
            mSeatStatus.append(status);
            viewHolder.mTvUserName.setText("none");
            viewHolder.mTvDelay.setText("delay");
            viewHolder.mTvSoundLevel.setText("soundLevel");
        } else if (seat.mStatus == ZegoChatroomSeatStatus.Used) {
            mSeatStatus.append("占，");
            viewHolder.mTvUserName.setText(seat.mUser.userName);
            if (ZegoDataCenter.ZEGO_USER.equals(seat.mUser)) {
                viewHolder.mTvDelay.setText("delay");
            } else {
                viewHolder.mTvDelay.setText(seat.mDelay + "");
            }
            viewHolder.mTvSoundLevel.setText(mSoundLevelFormat.format(seat.mSoundLevel));
        }
        if (seat.isMute) {
            mSeatStatus.append("禁");
        }

        viewHolder.mTvSeatStatus.setText(mSeatStatus.toString());
        mSeatStatus.setLength(0);

        viewHolder.mTvLiveStatus.setText(getLiveStatusString(seat.mLiveStatus));

        viewHolder.itemView.setTag(seat);
    }

    @Override
    public int getItemCount() {
        return mSeats == null ? 0 : mSeats.size();
    }

    public void setSeats(List<ChatroomSeatInfo> seats) {
        mSeats = seats;
        notifyDataSetChanged();
    }

    public void setOnChatroomSeatClickListener(OnChatroomSeatClickListener onChatroomSeatClickListener) {
        mOnChatroomSeatClickListener = onChatroomSeatClickListener;
    }

    private String getLiveStatusString(int liveStatus) {
        switch (liveStatus) {
            case ZegoChatroomUserLiveStatus.WAIT_CONNECT:
                return "待连接";
            case ZegoChatroomUserLiveStatus.CONNECTING:
                return "连接中";
            case ZegoChatroomUserLiveStatus.LIVE:
                return "已连接";
            default:
                return "";

        }
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mTvUserName;
        private TextView mTvSeatStatus;
        private TextView mTvLiveStatus;
        private TextView mTvDelay;
        private TextView mTvSoundLevel;

        private ViewHolder(View view) {
            super(view);
            mTvUserName = view.findViewById(R.id.tv_user_name);
            mTvSeatStatus = view.findViewById(R.id.tv_seat_status);
            mTvLiveStatus = view.findViewById(R.id.tv_live_status);
            mTvDelay = view.findViewById(R.id.tv_delay);
            mTvSoundLevel = view.findViewById(R.id.tv_sound_level);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mOnChatroomSeatClickListener != null) {
                mOnChatroomSeatClickListener.onChatroomSeatClick((ChatroomSeatInfo) v.getTag());
            }
        }
    }

    public interface OnChatroomSeatClickListener {
        void onChatroomSeatClick(ChatroomSeatInfo chatroomSeatInfo);
    }
}
