package com.zego.chatroom.demo.view;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.zego.chatroom.demo.R;


/**
 * 房间 相关提示Dialog
 */
public class TipDialog extends Dialog implements View.OnClickListener {

    // 右上角关闭按钮
    public ImageView mCloseIv;

    public TextView mTitleTv;
    public TextView mDescTv;

    // 蓝线背景的按钮
    public Button mButton1;
    public Button mButton2;
    // 蓝面背景的按钮
    public Button mButtonOk;

    public TipDialog(@NonNull Context context) {
        super(context, R.style.CommonDialog);
        initView(context);
    }

    private void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_tip_layout, null);
        setContentView(view);

        // 设置dialog 出现消失动效
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.width = (int) (displayMetrics.widthPixels * 0.9);  // 屏幕宽度的0.9
        // 设置没有边框
        getWindow().getDecorView().setPadding(0, 0, 0, 0);
        getWindow().setAttributes(layoutParams);

        mCloseIv = findViewById(R.id.close);

        mTitleTv = findViewById(R.id.tip_title);
        mDescTv = findViewById(R.id.tip_desc);

        mButton1 = findViewById(R.id.button1);
        mButton2 = findViewById(R.id.button2);
        mButtonOk = findViewById(R.id.button_ok);
    }

    /**
     * 重置dialog内容，建议在处理相关赋值之前执行
     */
    public void reset() {
        // 默认不可以取消
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        // 默认关闭按钮事件为隐藏dialog
        mCloseIv.setOnClickListener(this);
        mCloseIv.setVisibility(View.GONE);

        mButton1.setVisibility(View.GONE);
        mButton2.setVisibility(View.GONE);
        mButtonOk.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        if (v == mCloseIv) {
            dismiss();
        }
    }
}
