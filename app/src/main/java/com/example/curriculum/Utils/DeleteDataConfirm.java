package com.example.curriculum.Utils;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.curriculum.R;

/**
 * 确认清空数据，重新设置的DialogFragment
 */

public class DeleteDataConfirm extends DialogFragment
        implements View.OnClickListener  {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.delete_data_confirm, container, false);
        getDialog().setCancelable(false);
        getDialog().setCanceledOnTouchOutside(false);
        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view= LayoutInflater.from(getActivity()).inflate(R.layout.delete_data_confirm, null);
        Button yes_button = (Button) view.findViewById(R.id.delete_confirm_yes);
        yes_button.setOnClickListener(this);
        Button no_button = (Button) view.findViewById(R.id.delete_confirm_no);
        no_button.setOnClickListener(this);
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

    public interface DeleteDataListener {
        void onDeleteConfirmClick(boolean confirm);
    }

    @Override
    public void onClick(View v) {
        DeleteDataListener listener = (DeleteDataListener) getActivity();
        switch (v.getId()) {
            case R.id.delete_confirm_yes:
                if (listener != null) {
                    listener.onDeleteConfirmClick(true);
                    dismiss();
                }
                break;
            case R.id.delete_confirm_no:
                if (listener != null) {
                    listener.onDeleteConfirmClick(false);
                    dismiss();
                }
                break;
            default:
                dismiss();
                break;
        }
    }
}
