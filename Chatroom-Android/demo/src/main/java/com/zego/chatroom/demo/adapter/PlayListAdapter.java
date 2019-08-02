package com.zego.chatroom.demo.adapter;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zego.chatroom.demo.R;
import com.zego.chatroom.manager.musicplay.ZegoMusicResource;

import java.util.List;

public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.ViewHolder> {

    private List<ZegoMusicResource> mPlayList;

    private OnMusicItemClickListener mOnMusicItemClickListener;

    private ZegoMusicResource mCurrentPlayingResource = null;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_music_layout, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        ZegoMusicResource musicResource = mPlayList.get(position);

        viewHolder.mTvMusicName.setText(musicResource.getName());
        viewHolder.mTvMusicName.setTextColor(musicResource == mCurrentPlayingResource ? Color.RED : Color.BLACK);

        viewHolder.itemView.setTag(musicResource);
    }

    @Override
    public int getItemCount() {
        return mPlayList == null ? 0 : mPlayList.size();
    }

    public void setPlayList(List<ZegoMusicResource> playList) {
        this.mPlayList = playList;
        notifyDataSetChanged();
    }

    public void setOnMusicItemClickListener(OnMusicItemClickListener onMusicItemClickListener) {
        this.mOnMusicItemClickListener = onMusicItemClickListener;
    }

    public void setCurrentPlayingResource(ZegoMusicResource resource) {
        mCurrentPlayingResource = resource;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mTvMusicName;

        private ViewHolder(View view) {
            super(view);

            mTvMusicName = view.findViewById(R.id.tv_music_name);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mOnMusicItemClickListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mOnMusicItemClickListener.OnMusicItemClick(getAdapterPosition());
                }
            }
        }
    }

    public interface OnMusicItemClickListener {
        void OnMusicItemClick(int position);
    }
}
