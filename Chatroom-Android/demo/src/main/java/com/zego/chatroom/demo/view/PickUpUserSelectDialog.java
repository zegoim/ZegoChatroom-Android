package com.zego.chatroom.demo.view;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.zego.chatroom.demo.R;
import com.zego.chatroom.demo.adapter.UserAdapter;
import com.zego.chatroom.entity.ZegoChatroomUser;

import java.util.List;

public class PickUpUserSelectDialog extends Dialog implements View.OnClickListener, UserAdapter.OnUserItemClickListener {

    private TextView mTvTitle;

    private UserAdapter mUserAdapter;

    private OnPickUserUpListener mOnPickUserUpListener;

    private int mPickUpTargetIndex;


    public PickUpUserSelectDialog(@NonNull Context context) {
        super(context, R.style.CommonDialog);

        initData();
        initView(context);
    }

    private void initData() {
        mUserAdapter = new UserAdapter();
        mUserAdapter.setOnUserItemClickListener(this);
    }


    private void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_pick_up_user_select_layout, null);
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


        mTvTitle = findViewById(R.id.tv_title);
        RecyclerView rvUsers = findViewById(R.id.rv_users);

        rvUsers.setAdapter(mUserAdapter);
        rvUsers.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

        findViewById(R.id.tv_cancel).setOnClickListener(this);
    }

    public void setOnPickUpUserListener(OnPickUserUpListener onPickUserUpListener) {
        this.mOnPickUserUpListener = onPickUserUpListener;
    }

    public void setPickUpTargetIndex(int targetIndex) {
        this.mPickUpTargetIndex = targetIndex;
        mTvTitle.setText("选择用户抱上" + targetIndex + "号麦");
    }

    public void setUserList(List<ZegoChatroomUser> userSet) {
        mUserAdapter.setUserList(userSet);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_cancel) {
            dismiss();
        }
    }

    @Override
    public void OnUserItemClick(ZegoChatroomUser user) {
        dismiss();
        if (mOnPickUserUpListener != null) {
            mOnPickUserUpListener.onPickUpUser(user, mPickUpTargetIndex);
        }
    }

    public interface OnPickUserUpListener {
        void onPickUpUser(ZegoChatroomUser user, int index);
    }
}