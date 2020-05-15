package com.example.curriculum.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
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

public class SetCourseDialogFragment extends DialogFragment
    implements View.OnClickListener {

    private String TAG = "SetCourseDialogFragment";
    boolean existence;
    private EditText name;
    private String course_name = "";
    private EditText location;
    private String course_location = "";
    private EditText teacher;
    private String course_teacher = "";
    private Button cancel;
    private Button confirm;
    private Button delete;
    private int id;
    static public int CONFIRM = 1;
    static public int CANCEL = 0;
    static public int DELETE = -1;

    public SetCourseDialogFragment(int id, boolean existence) {
        super();
        this.id = id;
        this.existence = existence;
    }

    public SetCourseDialogFragment(int id, boolean existence, String name, String location, String teacher) {
        super();
        this.id = id;
        this.existence = existence;
        this.course_name = name;
        this.course_location = location;
        this.course_teacher = teacher;
        Log.d(TAG, "SetCourseDialogFragment: " + name + "," + location + "," + teacher);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.set_course_info, container, false);
        getDialog().setCancelable(false);
        getDialog().setCanceledOnTouchOutside(false);
        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view= LayoutInflater.from(getActivity()).inflate(R.layout.set_course_info, null);
        name = (EditText) view.findViewById(R.id.input_course_name);
        location = (EditText) view.findViewById(R.id.input_course_location);
        teacher = (EditText) view.findViewById(R.id.input_course_teacher);
        confirm = (Button) view.findViewById(R.id.set_info_confirm);
        cancel = (Button) view.findViewById(R.id.set_info_cancel);
        delete = (Button) view.findViewById(R.id.set_info_delete);
        name.setText(course_name);
        location.setText(course_location);
        teacher.setText(course_teacher);
        cancel.setOnClickListener(this);
        confirm.setOnClickListener(this);
        delete.setOnClickListener(this);
        if (existence) {
            delete.setVisibility(View.VISIBLE);
        }
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

    public interface SetCourseInfoListener {
        void onSetCourseInfoClickResult(int id, int state, boolean existence, String course_name, String course_location, String course_teacher);
    }

    @Override
    public void onClick(View v) {
        SetCourseInfoListener listener = (SetCourseInfoListener) getActivity();
        switch (v.getId()) {
            case R.id.set_info_confirm:
                if (listener != null) {
                    if (!name.getText().toString().equals("")) {
                        listener.onSetCourseInfoClickResult(id, CONFIRM, existence, name.getText().toString(),
                                location.getText().toString(), teacher.getText().toString());
                        dismiss();
                    }
                }
                break;
            case R.id.set_info_cancel:
                if (listener != null) {
                    listener.onSetCourseInfoClickResult(id, CANCEL, existence, name.getText().toString(),
                            location.getText().toString(), teacher.getText().toString());
                    dismiss();
                }
                break;
            case R.id.set_info_delete:
                if (existence && listener != null) {
                    listener.onSetCourseInfoClickResult(id, DELETE, existence, null, null, null);
                    dismiss();
                }
        }
    }
}
