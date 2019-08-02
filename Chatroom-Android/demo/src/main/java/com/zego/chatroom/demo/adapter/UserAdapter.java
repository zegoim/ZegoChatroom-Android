package com.zego.chatroom.demo.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zego.chatroom.demo.R;
import com.zego.chatroom.entity.ZegoChatroomUser;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private List<ZegoChatroomUser> mUserList;

    private OnUserItemClickListener mOnUserItemClickListener;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_user_layout, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        ZegoChatroomUser user = mUserList.get(position);
        viewHolder.mTvUserName.setText(user.userName);
        viewHolder.itemView.setTag(user);
    }

    @Override
    public int getItemCount() {
        return mUserList == null ? 0 : mUserList.size();
    }

    public void setUserList(List<ZegoChatroomUser> userList) {
        mUserList = userList;
        notifyDataSetChanged();
    }

    public void setOnUserItemClickListener(OnUserItemClickListener onUserItemClickListener) {
        this.mOnUserItemClickListener = onUserItemClickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mTvUserName;

        ViewHolder(View view) {
            super(view);

            mTvUserName = view.findViewById(R.id.tv_user_name);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mOnUserItemClickListener != null) {
                mOnUserItemClickListener.OnUserItemClick((ZegoChatroomUser) v.getTag());
            }
        }
    }

    public interface OnUserItemClickListener {
        void OnUserItemClick(ZegoChatroomUser user);
    }
}
