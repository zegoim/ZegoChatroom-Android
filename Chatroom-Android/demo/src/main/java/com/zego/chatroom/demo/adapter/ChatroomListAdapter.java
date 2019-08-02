package com.zego.chatroom.demo.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zego.chatroom.demo.R;
import com.zego.chatroom.demo.bean.ChatroomInfo;
import com.zego.chatroom.demo.utils.ChatroomInfoHelper;

import java.util.List;

public class ChatroomListAdapter extends RecyclerView.Adapter<ChatroomListAdapter.ViewHolder> {

    private List<ChatroomInfo> mChatrooms;

    private OnChatroomClickListener mOnChatroomClickListener;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chatroom_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        ChatroomInfo chatroomInfo = mChatrooms.get(position);

        viewHolder.mTvRoomName.setText(ChatroomInfoHelper.getDisplayRoomNameFromRoomName(chatroomInfo.room_name));
        viewHolder.mTvOwnerName.setText(viewHolder.mTvOwnerName.getResources().getString(R.string.room_owner_name, chatroomInfo.anchor_nick_name));

        viewHolder.itemView.setTag(chatroomInfo);
    }

    @Override
    public int getItemCount() {
        return mChatrooms == null ? 0 : mChatrooms.size();
    }

    public void setChatrooms(List<ChatroomInfo> chatrooms) {
        mChatrooms = chatrooms;
        notifyDataSetChanged();
    }

    public void setOnChatroomClickListener(OnChatroomClickListener onChatroomClickListener) {
        this.mOnChatroomClickListener = onChatroomClickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mTvRoomName;
        private TextView mTvOwnerName;

        ViewHolder(View view) {
            super(view);
            mTvRoomName = view.findViewById(R.id.tv_room_name);
            mTvOwnerName = view.findViewById(R.id.tv_owner_name);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mOnChatroomClickListener != null) {
                mOnChatroomClickListener.onChatroomClick((ChatroomInfo) v.getTag());
            }
        }
    }

    public interface OnChatroomClickListener {
        void onChatroomClick(ChatroomInfo chatroomInfo);
    }
}
