package com.zego.chatroom.demo.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zego.chatroom.demo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zego on 2018/2/6.
 */

public class MsgAdapter extends RecyclerView.Adapter {

    private List<String> mMsgList = new ArrayList<>();

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_msg_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int i) {
        ((TextView) (holder.itemView)).setText(mMsgList.get(i));
    }

    @Override
    public int getItemCount() {
        return mMsgList.size();
    }

    public void addRoomMsg(String message) {
        mMsgList.add(message);
        notifyDataSetChanged();
        if (recyclerView != null) {
            recyclerView.scrollToPosition(this.getItemCount() - 1);
        }
    }

    public void clear() {
        mMsgList.clear();
    }

    private RecyclerView recyclerView;

    public void setRecyclerView(RecyclerView view) {
        this.recyclerView = view;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}

