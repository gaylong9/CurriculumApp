package com.example.curriculum.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.curriculum.R;

/**
 * 用于获取一天中总课程数的DialogFragment
 */

public class GetNumDialogFragment extends DialogFragment
        implements View.OnClickListener {

    private int num;
    private EditText editText;
    private Button button;
    private String TAG = "GetNumConfirmListener";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.get_course_num, container, false);
        getDialog().setCancelable(false);
        getDialog().setCanceledOnTouchOutside(false);
        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view= LayoutInflater.from(getActivity()).inflate(R.layout.get_course_num, null);
        editText = (EditText) view.findViewById(R.id.input_course_num);
        button = (Button) view.findViewById(R.id.get_num_confirm);
        button.setOnClickListener(this);
        builder.setView(view);

        AlertDialog alertDialog = builder.create();
        // 设置宽度为屏宽、位置靠近屏幕底部
        Window window = alertDialog.getWindow();
        window.setBackgroundDrawableResource(android.R.color.white);
        window.getDecorView().setPadding(100, 100, 100, 100);
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(wlp);
        return alertDialog;
    }

    public interface GetNumConfirmListener {
        void onGetNumClickComplete(int num);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.get_num_confirm:
                GetNumConfirmListener listener = (GetNumConfirmListener) getActivity();
                if (listener != null) {
                    if (!editText.getText().toString().equals("")) {
                        listener.onGetNumClickComplete(Integer.parseInt(editText.getText().toString()));
                        dismiss();
                    }
                }
                break;
        }
    }
}
