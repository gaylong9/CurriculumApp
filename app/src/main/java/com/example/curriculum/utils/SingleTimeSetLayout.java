package com.example.curriculum.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.curriculum.R;

/**
 * 设置界面，时间设置布局
 */

public class SingleTimeSetLayout extends ConstraintLayout {
    public SingleTimeSetLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.single_time_set, this);
    }
}
