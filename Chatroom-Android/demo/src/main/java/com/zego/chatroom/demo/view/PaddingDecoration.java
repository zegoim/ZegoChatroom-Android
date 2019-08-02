package com.zego.chatroom.demo.view;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class PaddingDecoration extends RecyclerView.ItemDecoration {

    private int mDividerHeight;

    public PaddingDecoration(int dividerHeight) {
        this.mDividerHeight = dividerHeight;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.bottom = mDividerHeight;//类似加了一个bottom padding
    }
}
