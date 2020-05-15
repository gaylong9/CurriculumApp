package com.example.curriculum.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.curriculum.R;

/**
 * 语音布局
 */

public class SingleRecordLayout extends ConstraintLayout {
    public String path;
    public SingleRecordLayout(Context context, AttributeSet attrs, String path) {
        super(context, attrs);
        this.path = path;
        LayoutInflater.from(context).inflate(R.layout.single_record, this);
    }
}