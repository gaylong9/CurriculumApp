package com.example.curriculum.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.curriculum.R;

import java.util.jar.Attributes;

/**
 * 用于添加note的布局
 */

public class SingleNoteLayout extends ConstraintLayout {
    public boolean isCompleted = false;
    public SingleNoteLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.single_note, this);
    }

    public SingleNoteLayout(Context context, AttributeSet attrs, String content) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.single_note, this);
        EditText editText = findViewById(R.id.note_edittext);
        editText.setText(content);
    }
}
