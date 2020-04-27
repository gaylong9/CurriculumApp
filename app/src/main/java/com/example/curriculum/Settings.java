package com.example.curriculum;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.curriculum.Utils.MyDBHelper;
import com.example.curriculum.Utils.SingleSetTimeLayout;
import com.example.curriculum.Utils.SingleTextSetLayout;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 设置上课时间所用
 */

public class Settings extends AppCompatActivity {

    LinearLayout linearLayout;
    Switch switcher;
    int course_num;
    int cur_id;
    int cur_week;
    int sum_week;
    MyDBHelper dbHelper;
    Intent return_intent;
    String TAG = "Settings";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        return_intent = new Intent();

        // 获取课程数
        Intent intent = getIntent();
        this.course_num = intent.getIntExtra("course_num", 0);
        this.cur_week = intent.getIntExtra("cur_week", 0);

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

        // 总周数设置
        SingleTextSetLayout singleTextSetLayout = new SingleTextSetLayout(Settings.this, null);
        TextView week_sum_hint = (TextView) singleTextSetLayout.findViewById(R.id.text_set_hint);
        final EditText week_sum_value = (EditText) singleTextSetLayout.findViewById(R.id.text_set_value);
        week_sum_hint.setText(R.string.week_sum);
        Cursor cursor = db.rawQuery("select * from Settings where keys = ?", new String[] {"week_sum"});
        cursor.moveToFirst();
        String week_sum = cursor.getString(cursor.getColumnIndex("value"));
        if (week_sum.equals("")) {
            this.sum_week = 0;
        }else {
            this.sum_week = Integer.parseInt(week_sum);
        }
        if (sum_week == 0) {
            week_sum_value.setText("");
        } else {
            week_sum_value.setText(week_sum);
        }
        week_sum_value.addTextChangedListener(new TextWatcher() {
            // 总周数变化
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "afterTextChanged: 总周数变化 " + s.toString());
                int new_sum_week;
                if (s.toString().equals("")) {
                    new_sum_week = 0;
                } else {
                    String temp = s.toString().trim();
                    temp = temp.replaceAll("\n", "");
                    temp = temp.replaceAll("-", "");
                    new_sum_week = Integer.parseInt(temp);
                }
                db.execSQL("update Settings set value = ? where keys = ?",
                        new String[] {""+new_sum_week, "week_sum"});
            }
        });
        linearLayout.addView(singleTextSetLayout);
        cursor.close();

        // 当前周数
        singleTextSetLayout = new SingleTextSetLayout(Settings.this, null);
        int id = 12345;
        singleTextSetLayout.setId(id);
        final TextView cur_week_hint = (TextView) singleTextSetLayout.findViewById(R.id.text_set_hint);
        final EditText cur_week_value = (EditText) singleTextSetLayout.findViewById(R.id.text_set_value);
        cur_week_hint.setText(R.string.cur_week);
        if (cur_week == 0) {
            cur_week_value.setText("");
        } else {
            cur_week_value.setText("" + cur_week);
        }
        cur_week_value.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "afterTextChanged: 当前周变化 " + s.toString());
                if (!s.toString().equals("" + cur_week)) {
                    // 当前周变化，修改数据库
                    int new_cur_week;
                    if (s.toString().equals("")) {
                        new_cur_week = 0;
                    }else {
                        String temp = s.toString().trim();
                        temp = temp.replaceAll("\n", "");
                        temp = temp.replaceAll("-", "");
                        new_cur_week = Integer.parseInt(temp);
                    }
                    return_intent.putExtra("cur_week", new_cur_week);
                    setResult(RESULT_OK, return_intent);
                    Calendar cal = Calendar.getInstance();
                    cal.setFirstDayOfWeek(Calendar.MONDAY); // 设置每周的第一天为星期一
                    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); // 每周从周一开始
                    cal.setMinimalDaysInFirstWeek(7); // 设置最少为7天
                    cal.setTime(new Date());
                    int cur_week_of_year = cal.get(Calendar.WEEK_OF_YEAR);
                    if (cur_week_of_year < new_cur_week) {
                        // 去年开始
                        db.execSQL("update Settings set value = ? where keys = ?",
                                new String[] {""+(cal.get(Calendar.YEAR)-1), "start_year"});
                        Calendar cal1 = Calendar.getInstance();
                        cal.setFirstDayOfWeek(Calendar.MONDAY); // 设置每周的第一天为星期一
                        cal1.set(Calendar.YEAR, cal.get(Calendar.YEAR)-1); // Set only year
                        cal1.set(Calendar.MONTH, Calendar.DECEMBER); // Don't change
                        cal1.set(Calendar.DAY_OF_MONTH, 31); // Don't change
                        int totalWeeks = cal1.get(Calendar.WEEK_OF_YEAR);
                        int new_start_week_of_year = totalWeeks - new_cur_week + cur_week_of_year + 1;
                        db.execSQL("update Settings set value = ? where keys = ?",
                                new String[] {""+new_start_week_of_year, "start_week_of_year"});
                        Log.d(TAG, "afterTextChanged: new start week of year: " + new_start_week_of_year);
                    } else {
                        // 今年开始
                        db.execSQL("update Settings set value = ? where keys = ?",
                                new String[] {""+cal.get(Calendar.YEAR), "start_year"});
                        int new_start_week_of_year = cur_week_of_year-new_cur_week+1;
                        db.execSQL("update Settings set value = ? where keys = ?",
                                new String[] {""+new_start_week_of_year, "start_week_of_year"});
                        Log.d(TAG, "afterTextChanged: new start week of year: " + new_start_week_of_year);
                    }

                }
            }
        });
        linearLayout.addView(singleTextSetLayout);

        // 课程时间设置
        for (int i = 1; i <= course_num; i++) {
            SingleSetTimeLayout singleSetTimeLayout = new SingleSetTimeLayout(Settings.this, null);
            singleSetTimeLayout.setId(i);
            TextView course_hint = (TextView) singleSetTimeLayout.findViewById(R.id.time_set_hint);
            final TextView course_time = (TextView) singleSetTimeLayout.findViewById(R.id.time_set_value);

            // 判断时间信息是否已有
            cursor = db.rawQuery("select time from TimeInfo where id = ?", new String[] {"" + i});
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
                    TimePickerDialog timePickerDialog = new TimePickerDialog(Settings.this,
                            new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                    String save_time = String.format(Locale.CHINA, "%02d", hourOfDay)
                                            +  ":" + String.format(Locale.CHINA, "%02d", minute);
                                    db.execSQL("update TimeInfo set time = ? where id = ?",
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: destoryed");
//        // 保存课前提醒的状态
//        Log.d(TAG, "onDestroy: " + switcher.isChecked());
//        String flag = switcher.isChecked()? "1": "0";
//        Log.d(TAG, "onDestroy: " + flag);
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//        db.execSQL("update Settings set selected = ? where id = ?", new String[] {flag, "1"});
    }
}
