package com.example.curriculum.Utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.curriculum.R;

public class SetTimeSwitchLayout extends ConstraintLayout {

    public SetTimeSwitchLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.select_time_remind, this);
    }
}
