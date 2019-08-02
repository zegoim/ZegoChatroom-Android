package com.zego.chatroom.demo.adapter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;


import com.zego.chatroom.demo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 音效PagerAdapter，创建的View不能被回收，因此需在ViewPager中进行 setOffscreenPageLimit()设置
 */
public class SoundEffectViewAdapter extends PagerAdapter implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    // 音效设置View的数量
    private final static int VIEW_COUNT = 3;

    private final static int TEXT_COLOR_SELECTED = Color.parseColor("#0d70ff");
    private final static int TEXT_COLOR_UNSELECTED = Color.parseColor("#333333");

    // 变声相关类型
    public final static int SOUND_EFFECT_TYPE_VOICE_CHANGE_NO = 0x10; // 无
    public final static int SOUND_EFFECT_TYPE_VOICE_CHANGE_LOLI = 0x11;  // 萝莉
    public final static int SOUND_EFFECT_TYPE_VOICE_CHANGE_UNCLE = 0x12;  // 大叔

    // 立体声相关类型
    public final static int SOUND_EFFECT_TYPE_STEREO_NO = 0x20; // 无
    public final static int SOUND_EFFECT_TYPE_STEREO_LEFT_SIDE = 0x21; // 左侧声
    public final static int SOUND_EFFECT_TYPE_STEREO_RIGHT_SIDE = 0x22; // 右侧声

    // 混响相关类型
    public final static int SOUND_EFFECT_TYPE_MIXED_VOICE_NO = 0x30; // 无
    public final static int SOUND_EFFECT_TYPE_MIXED_VOICE_LOBBY = 0x31;  // 大堂场景
    public final static int SOUND_EFFECT_TYPE_MIXED_VOICE_VALLEY = 0x32;  // 山谷场景

    // View的分组
    private final static int VIEW_GROUP_VOICE_CHANGE = 0x10;  // 变声
    private final static int VIEW_GROUP_STEREO = 0x20;   // 立体声
    private final static int VIEW_GROUP_MIXED_VOICE = 0x30;  // 混响

    // checkBox 列表
    private List<CheckBox> checkBoxList;
    // 当前checkBox的状态
    private boolean currentCheckBoxState;

    private List<TextView> voiceChangeTextViewList;
    private List<TextView> stereoTextViewList;
    private List<TextView> mixedTextViewList;

    private OnSoundEffectChangedListener onSoundEffectChangedListener;
    private OnSoundEffectAuditionCheckedListener onSoundEffectAuditionCheckedListener;

    private Context context;
    private AudioManager audioManager;
    private BroadcastReceiver headSetBroadcastReceiver;

    // 是否支持立体声音
    private boolean isStereo;

    public SoundEffectViewAdapter(Context context, boolean isStereo) {
        this.isStereo = isStereo;
        checkBoxList = new ArrayList<>(3);
        currentCheckBoxState = false;

        voiceChangeTextViewList = new ArrayList<>(3);
        stereoTextViewList = new ArrayList<>(3);
        mixedTextViewList = new ArrayList<>(3);

        // 初始化耳机相关监听
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        headSetBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null && action.equals(Intent.ACTION_HEADSET_PLUG)) {
                    if (intent.hasExtra("state")) {
                        int state = intent.getIntExtra("state", -1);
                        //  耳机 拔出
                        if (state == 0) {
                            // 当勾选的情况
                            if (currentCheckBoxState && checkBoxList != null && !checkBoxList.isEmpty()) {
                                // 切换状态
                                checkBoxList.get(0).toggle();
                            }
                        } else if (state == 1) {
                            // DO NOTHING
                        }
                    }
                }
            }
        };
        this.context = context;
        context.registerReceiver(headSetBroadcastReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public int getCount() {
        return VIEW_COUNT;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.sound_effect_item_layout, container, false);
        TextView textView;
        CheckBox checkBox;
        // 变声
        if (position == 0) {
            textView = view.findViewById(R.id.item_1);
            textView.setCompoundDrawablesWithIntrinsicBounds(null, container.getContext().getResources().getDrawable(R.drawable.sound_effect_no_level_list), null, null);
            textView.setTextColor(TEXT_COLOR_SELECTED);
            setDrawableTopLevel(textView, 2);
            textView.setText(R.string.sound_effect_no_set);
            textView.setTag(R.id.sound_effect_selected_state, true);
            textView.setTag(R.id.sound_effect_type, SOUND_EFFECT_TYPE_VOICE_CHANGE_NO);
            textView.setTag(R.id.sound_effect_view_group_type, VIEW_GROUP_VOICE_CHANGE);
            textView.setOnClickListener(this);
            voiceChangeTextViewList.add(textView);


            textView = view.findViewById(R.id.item_2);
            textView.setCompoundDrawablesWithIntrinsicBounds(null, container.getContext().getResources().getDrawable(R.drawable.sound_effect_loli_level_list), null, null);
            textView.setTextColor(TEXT_COLOR_UNSELECTED);
            setDrawableTopLevel(textView, 1);
            textView.setText(R.string.loli);
            textView.setTag(R.id.sound_effect_selected_state, false);
            textView.setTag(R.id.sound_effect_type, SOUND_EFFECT_TYPE_VOICE_CHANGE_LOLI);
            textView.setTag(R.id.sound_effect_view_group_type, VIEW_GROUP_VOICE_CHANGE);
            textView.setOnClickListener(this);
            voiceChangeTextViewList.add(textView);

            textView = view.findViewById(R.id.item_3);
            textView.setCompoundDrawablesWithIntrinsicBounds(null, container.getContext().getResources().getDrawable(R.drawable.sound_effect_uncle_level_list), null, null);
            textView.setTextColor(TEXT_COLOR_UNSELECTED);
            setDrawableTopLevel(textView, 1);
            textView.setText(R.string.uncle);
            textView.setTag(R.id.sound_effect_selected_state, false);
            textView.setTag(R.id.sound_effect_type, SOUND_EFFECT_TYPE_VOICE_CHANGE_UNCLE);
            textView.setTag(R.id.sound_effect_view_group_type, VIEW_GROUP_VOICE_CHANGE);
            textView.setOnClickListener(this);
            voiceChangeTextViewList.add(textView);
        } else if (position == 1) {  // 立体声
            textView = view.findViewById(R.id.item_1);
            textView.setCompoundDrawablesWithIntrinsicBounds(null, container.getContext().getResources().getDrawable(R.drawable.sound_effect_no_level_list), null, null);
            textView.setTextColor(TEXT_COLOR_SELECTED);
            setDrawableTopLevel(textView, 2);
            textView.setText(R.string.sound_effect_no_set);
            textView.setTag(R.id.sound_effect_selected_state, true);
            textView.setTag(R.id.sound_effect_type, SOUND_EFFECT_TYPE_STEREO_NO);
            textView.setTag(R.id.sound_effect_view_group_type, VIEW_GROUP_STEREO);
            textView.setOnClickListener(this);
            stereoTextViewList.add(textView);

            textView = view.findViewById(R.id.item_2);
            textView.setCompoundDrawablesWithIntrinsicBounds(null, container.getContext().getResources().getDrawable(R.drawable.sound_effect_left_side_level_list), null, null);
            textView.setTextColor(TEXT_COLOR_UNSELECTED);
            setDrawableTopLevel(textView, 1);
            textView.setText(R.string.sound_effect_stereo_left_side);
            textView.setTag(R.id.sound_effect_selected_state, false);
            textView.setTag(R.id.sound_effect_type, SOUND_EFFECT_TYPE_STEREO_LEFT_SIDE);
            textView.setTag(R.id.sound_effect_view_group_type, VIEW_GROUP_STEREO);
            textView.setOnClickListener(this);
            stereoTextViewList.add(textView);

            textView = view.findViewById(R.id.item_3);
            textView.setCompoundDrawablesWithIntrinsicBounds(null, container.getContext().getResources().getDrawable(R.drawable.sound_effect_right_side_level_list), null, null);
            textView.setTextColor(TEXT_COLOR_UNSELECTED);
            setDrawableTopLevel(textView, 1);
            textView.setText(R.string.sound_effect_stereo_right_side);
            textView.setTag(R.id.sound_effect_selected_state, false);
            textView.setTag(R.id.sound_effect_type, SOUND_EFFECT_TYPE_STEREO_RIGHT_SIDE);
            textView.setTag(R.id.sound_effect_view_group_type, VIEW_GROUP_STEREO);
            textView.setOnClickListener(this);
            stereoTextViewList.add(textView);
        } else if (position == 2) { // 混响
            textView = view.findViewById(R.id.item_1);
            textView.setCompoundDrawablesWithIntrinsicBounds(null, container.getContext().getResources().getDrawable(R.drawable.sound_effect_no_level_list), null, null);
            textView.setTextColor(TEXT_COLOR_SELECTED);
            setDrawableTopLevel(textView, 2);
            textView.setText(R.string.sound_effect_no_set);
            textView.setTag(R.id.sound_effect_selected_state, true);
            textView.setTag(R.id.sound_effect_type, SOUND_EFFECT_TYPE_MIXED_VOICE_NO);
            textView.setTag(R.id.sound_effect_view_group_type, VIEW_GROUP_MIXED_VOICE);
            textView.setOnClickListener(this);
            mixedTextViewList.add(textView);

            textView = view.findViewById(R.id.item_2);
            textView.setCompoundDrawablesWithIntrinsicBounds(null, container.getContext().getResources().getDrawable(R.drawable.sound_effect_lobby_level_list), null, null);
            textView.setTextColor(TEXT_COLOR_UNSELECTED);
            setDrawableTopLevel(textView, 1);
            textView.setText(R.string.sound_effect_mixed_voice_lobby);
            textView.setTag(R.id.sound_effect_selected_state, false);
            textView.setTag(R.id.sound_effect_type, SOUND_EFFECT_TYPE_MIXED_VOICE_LOBBY);
            textView.setTag(R.id.sound_effect_view_group_type, VIEW_GROUP_MIXED_VOICE);
            textView.setOnClickListener(this);
            mixedTextViewList.add(textView);

            textView = view.findViewById(R.id.item_3);
            textView.setCompoundDrawablesWithIntrinsicBounds(null, container.getContext().getResources().getDrawable(R.drawable.sound_effect_valley_level_list), null, null);
            textView.setTextColor(TEXT_COLOR_UNSELECTED);
            setDrawableTopLevel(textView, 1);
            textView.setText(R.string.sound_effect_mixed_voice_valley);
            textView.setTag(R.id.sound_effect_selected_state, false);
            textView.setTag(R.id.sound_effect_type, SOUND_EFFECT_TYPE_MIXED_VOICE_VALLEY);
            textView.setTag(R.id.sound_effect_view_group_type, VIEW_GROUP_MIXED_VOICE);
            textView.setOnClickListener(this);
            mixedTextViewList.add(textView);
        }
        // 初始化checkbox
        checkBox = view.findViewById(R.id.checkbox);
        checkBox.setChecked(currentCheckBoxState);
        checkBox.setOnCheckedChangeListener(this);
        checkBoxList.add(checkBox);

        // 试听文案添加点击事件监听
        view.findViewById(R.id.audition_layout).setOnClickListener(this);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // do nothing
    }

    /**
     * 释放相关资源
     */
    public void release() {
        audioManager = null;
        context.registerReceiver(headSetBroadcastReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.audition_layout) {
            CheckBox checkBox = ((ViewGroup) v).findViewById(R.id.checkbox);
            if (checkBox != null) {
                checkAndToggle(checkBox);
            }
            return;
        }
        if (!isStereo && (int) v.getTag(R.id.sound_effect_view_group_type) == VIEW_GROUP_STEREO) {
            Toast.makeText(context, "当前房间不支持立体声功能", Toast.LENGTH_SHORT).show();
            return;
        }
        // 如果是非选中，才进行处理
        if (!(boolean) v.getTag(R.id.sound_effect_selected_state)) {
            // 设置选中的字体颜色
            ((TextView) v).setTextColor(TEXT_COLOR_SELECTED);
            // 设置选中的图片背景
            setDrawableTopLevel((TextView) v, 2);
            // 设置选中状态
            v.setTag(R.id.sound_effect_selected_state, true);
            // 变声组
            if ((int) v.getTag(R.id.sound_effect_view_group_type) == VIEW_GROUP_VOICE_CHANGE) {
                for (TextView textView : voiceChangeTextViewList) {
                    // 重置其他没有选中的textView的状态
                    if (textView != v) {
                        // 设置字体颜色
                        textView.setTextColor(TEXT_COLOR_UNSELECTED);
                        setDrawableTopLevel(textView, 1);
                        textView.setTag(R.id.sound_effect_selected_state, false);
                    }
                }
            } else if ((int) v.getTag(R.id.sound_effect_view_group_type) == VIEW_GROUP_STEREO) {
                for (TextView textView : stereoTextViewList) {
                    // 重置其他没有选中的textView的状态
                    if (textView != v) {
                        // 设置字体颜色
                        textView.setTextColor(TEXT_COLOR_UNSELECTED);
                        setDrawableTopLevel(textView, 1);
                        textView.setTag(R.id.sound_effect_selected_state, false);
                    }
                }
            } else if ((int) v.getTag(R.id.sound_effect_view_group_type) == VIEW_GROUP_MIXED_VOICE) {
                for (TextView textView : mixedTextViewList) {
                    // 重置其他没有选中的textView的状态
                    if (textView != v) {
                        // 设置字体颜色
                        textView.setTextColor(TEXT_COLOR_UNSELECTED);
                        setDrawableTopLevel(textView, 1);
                        textView.setTag(R.id.sound_effect_selected_state, false);
                    }
                }
            }

            // 进行音效状态改变回调
            if (onSoundEffectChangedListener != null) {
                onSoundEffectChangedListener.onSoundEffectChanged((int) v.getTag(R.id.sound_effect_type));
            }
        }
    }

    /**
     * 检查后 checkbox 切换状态
     * @param checkBox 检查和切换状态的 checkbox
     */
    private void checkAndToggle(CheckBox checkBox) {
        if (!checkBox.isChecked()) {
            // 如果没有勾选，并且没有插耳机，提示
            if (!audioManager.isWiredHeadsetOn()) {
                Toast.makeText(context, "音效试听需要带上耳机才可使用", Toast.LENGTH_LONG).show();
            } else {
                // 否则，切换状态
                checkBox.toggle();
            }
        } else {
            checkBox.toggle();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        currentCheckBoxState = isChecked;

        for (CheckBox checkBox : checkBoxList) {
            // 对其他对checkBox进行处理
            if (checkBox != buttonView) {
                // 先置null，避免重复调用
                checkBox.setOnCheckedChangeListener(null);
                checkBox.setChecked(isChecked);
                checkBox.setOnCheckedChangeListener(this);
            }
        }

        if (onSoundEffectAuditionCheckedListener != null) {
            onSoundEffectAuditionCheckedListener.onSoundEffectAuditionChecked(isChecked);
        }
    }

    /**
     * 设置check点击回调事件
     */
    public void setOnSoundEffectAuditionCheckedListener(OnSoundEffectAuditionCheckedListener onSoundEffectAuditionCheckedListener) {
        this.onSoundEffectAuditionCheckedListener = onSoundEffectAuditionCheckedListener;
    }

    /**
     * 设置音效状态改变事件
     */
    public void setOnSoundEffectChangedListener(OnSoundEffectChangedListener onSoundEffectChangedListener) {
        this.onSoundEffectChangedListener = onSoundEffectChangedListener;
    }

    // 设置 textView drawableTop level 值
    private void setDrawableTopLevel(TextView textView, int level) {
        if (textView != null && textView.getCompoundDrawables()[1] != null) {
            textView.getCompoundDrawables()[1].setLevel(level);
        }
    }

    /**
     * 音效改变监听器
     */
    public interface OnSoundEffectChangedListener {

        /**
         * 音效类型改变回调
         *
         * @param soundEffectType 音效类型
         */
        void onSoundEffectChanged(int soundEffectType);
    }

    /**
     * 音效试听点击回调
     */
    public interface OnSoundEffectAuditionCheckedListener {
        void onSoundEffectAuditionChecked(boolean isChecked);
    }
}
