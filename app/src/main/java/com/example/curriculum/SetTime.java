package com.example.curriculum;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.curriculum.Utils.MyDBHelper;
import com.example.curriculum.Utils.SetTimeSwitchLayout;
import com.example.curriculum.Utils.SingleSetTimeLayout;

import java.util.Locale;

/**
 * 设置上课时间所用
 */

public class SetTime extends AppCompatActivity {

    LinearLayout linearLayout;
    Switch switcher;
    int course_num;
    int cur_id;
    MyDBHelper dbHelper;
    String TAG = "SetTime";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_time);

        // 获取课程数
        Intent intent = getIntent();
        this.course_num = intent.getIntExtra("course_num", 0);

        // 数据库
        dbHelper = new MyDBHelper(this, "Course.db", null, 1);
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        // 动态添加控件
        linearLayout = (LinearLayout) findViewById(R.id.set_time_layout);
//        // 课前提醒开关
//        final SetTimeSwitchLayout setTimeSwitchLayout = new SetTimeSwitchLayout(SetTime.this, null);
//        switcher = (Switch) setTimeSwitchLayout.findViewById(R.id.select_remind_switch);
//        Cursor selected = db.rawQuery("select selected from Settings where id = ?", new String[] {"1"});
//        selected.moveToFirst();
//        boolean flag = selected.getString(selected.getColumnIndex("selected")).equals("1");
//        switcher.setChecked(flag);
//        linearLayout.addView(setTimeSwitchLayout);
//        selected.close();

        for (int i = 1; i <= course_num; i++) {
            SingleSetTimeLayout singleSetTimeLayout = new SingleSetTimeLayout(SetTime.this, null);
            singleSetTimeLayout.setId(i);
            TextView course_hint = (TextView) singleSetTimeLayout.findViewById(R.id.set_time_id);
            final TextView course_time = (TextView) singleSetTimeLayout.findViewById(R.id.set_time_show);

            // 判断时间信息是否已有
            Cursor cursor = db.rawQuery("select time from BasicInfo where id = ?", new String[] {"" + i});
            course_hint.setText("第" + i + "节课：");
            if (cursor.getCount() == 1) {
                // 库中已有时间信息
                cursor.moveToFirst();
                String time = cursor.getString(cursor.getColumnIndex("time"));
                course_time.setText(time);
            }
            cursor.close();

            // 设置点击
            singleSetTimeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 调用TimePickerDialog，修改时间信息
                    cur_id = v.getId();
                    Log.d(TAG, "onClick: cur_id:" + cur_id);
                    TimePickerDialog timePickerDialog = new TimePickerDialog(SetTime.this,
                            new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                    String save_time = String.format(Locale.CHINA, "%02d", hourOfDay)
                                            +  ":" + String.format(Locale.CHINA, "%02d", minute);
                                    db.execSQL("update BasicInfo set time = ? where id = ?",
                                            new String[] {save_time, "" + cur_id});
                                    course_time.setText(save_time);
                                    Log.d(TAG, "setTime: " + save_time);
                                }
                            }, 0, 0, true);
                    // Log.d(TAG, "setTime: " + MainActivity.this.hour + MainActivity.this.minute);
                    timePickerDialog.setCancelable(false);
                    timePickerDialog.setCanceledOnTouchOutside(false);
                    timePickerDialog.show();
                }
            });
            linearLayout.addView(singleSetTimeLayout);
        }
    }

//    @Override
//    // 保存课前提醒的状态
//    protected void onDestroy() {
//        super.onDestroy();
//        Log.d(TAG, "onDestroy: " + switcher.isChecked());
//        String flag = switcher.isChecked()? "1": "0";
//        Log.d(TAG, "onDestroy: " + flag);
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//        db.execSQL("update Settings set selected = ? where id = ?", new String[] {flag, "1"});
//    }
}
