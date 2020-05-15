package com.example.curriculum.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.curriculum.R;

public class SingleImageLayout extends ConstraintLayout {
    public String path;
    private Context context;
    public SingleImageLayout(Context context, AttributeSet attrs, String path) {
        super(context, attrs);
        this.path = path;
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.single_image, this);
    }
}