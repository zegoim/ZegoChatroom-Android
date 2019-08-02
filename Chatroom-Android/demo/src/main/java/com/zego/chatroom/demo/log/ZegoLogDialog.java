package com.zego.chatroom.demo.log;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.zego.chatroom.demo.R;
import com.zego.chatroom.manager.log.ZegoLogInfo;

import java.util.List;

class ZegoLogDialog extends Dialog {

    private RecyclerView mRvLog;
    private ZegoLogAdapter mLogAdapter;

    ZegoLogDialog(@NonNull Context context) {
        super(context, R.style.CommonDialog);

        initData();
        initView(context);
    }

    private void initData() {
        mLogAdapter = new ZegoLogAdapter();
    }

    private void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_log_layout, null);

        setContentView(view);
        // 设置可以取消
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        // 设置Dialog高度位置
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();

        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = (int) (displayMetrics.heightPixels * 0.8);   // 屏幕高度的0.8
        layoutParams.gravity = Gravity.BOTTOM;

        // 设置没有边框
        getWindow().getDecorView().setPadding(0, 0, 0, 0);
        getWindow().setAttributes(layoutParams);

        initRecyclerView(context);
    }

    private void initRecyclerView(Context context) {
        mRvLog = findViewById(R.id.rv_log);
        mRvLog.setAdapter(mLogAdapter);
        mRvLog.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
    }

    public void setLogInfoList(List<ZegoLogInfo> logInfoList) {
        mLogAdapter.setLogInfoList(logInfoList);
    }

    @Override
    public void show() {
        super.show();
        mLogAdapter.notifyDataSetChanged();
        mRvLog.scrollToPosition(mLogAdapter.getItemCount() - 1);
    }
}
