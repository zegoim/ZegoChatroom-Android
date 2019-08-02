package com.zego.chatroom.demo.log;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zego.chatroom.demo.R;
import com.zego.chatroom.manager.log.ZegoLogInfo;

import java.util.List;

class ZegoLogAdapter extends RecyclerView.Adapter<ZegoLogAdapter.ViewHolder> {

    private List<ZegoLogInfo> mLogInfoList;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_log_info_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        ZegoLogInfo logInfo = mLogInfoList.get(position);
        viewHolder.mVLogLevel.setBackgroundColor(logInfo.getLogLevelColor());
        viewHolder.mTvLogMsg.setText(logInfo.getLogTag() + " : " + logInfo.getLogMsg());
    }

    @Override
    public int getItemCount() {
        return mLogInfoList == null ? 0 : mLogInfoList.size();
    }

    public void setLogInfoList(List<ZegoLogInfo> logInfoList) {
        this.mLogInfoList = logInfoList;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private View mVLogLevel;
        private TextView mTvLogMsg;

        private ViewHolder(View view) {
            super(view);
            mVLogLevel = view.findViewById(R.id.v_log_level);
            mTvLogMsg = view.findViewById(R.id.tv_log_msg);
        }
    }
}
