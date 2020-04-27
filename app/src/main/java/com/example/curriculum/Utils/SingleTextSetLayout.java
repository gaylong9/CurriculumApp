package com.example.curriculum.Utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.curriculum.R;

public class SingleTextSetLayout extends ConstraintLayout {
    public SingleTextSetLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.single_text_set, this);
    }
}
