package com.example.curriculum.Utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.curriculum.R;

public class SingleSetTimeLayout extends ConstraintLayout {
    public SingleSetTimeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.single_set_time, this);
    }
}
