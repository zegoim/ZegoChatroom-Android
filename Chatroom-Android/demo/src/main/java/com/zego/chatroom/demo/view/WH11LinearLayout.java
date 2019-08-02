package com.zego.chatroom.demo.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * 长宽1：1 LinearLayout，长宽都会默认修改为固定值，因此wrap_content可能会失效。
 */
public class WH11LinearLayout extends LinearLayout {

    public WH11LinearLayout(Context context) {
        super(context);
    }

    public WH11LinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public WH11LinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 强制设置成父布局提供的或者自身定义的宽度，并且计算高度，强制设置为EXACTLY
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        super.onMeasure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY));
    }
}
