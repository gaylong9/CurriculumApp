package com.example.curriculum.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.curriculum.R;

/**
 * 用于加入右侧碎片的一个课程的布局
 */

public class SingleCourseLayout extends ConstraintLayout {
    public SingleCourseLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.single_course, this);
    }
}
