package com.example.curriculum;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.curriculum.utils.MyDBHelper;
import com.example.curriculum.utils.SingleNoteLayout;
import com.example.curriculum.utils.SingleTimeSetLayout;
import com.example.curriculum.utils.SingleTextSetLayout;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 设置
 */

public class Settings extends AppCompatActivity {

    LinearLayout linearLayout;
    Switch switcher;
    int course_num;
    int cur_id;
    int cur_week;
    int sum_week;
    MyDBHelper dbHelper;
    SQLiteDatabase db;
    String TAG = "Settings";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_with_fragment);


        // 获取基本信息
        Intent intent = getIntent();
        this.course_num = intent.getIntExtra("course_num", 0);
        this.cur_week = intent.getIntExtra("cur_week", 0);

        // 数据库
        dbHelper = new MyDBHelper(this, "Course.db", null, 3);
        db = dbHelper.getWritableDatabase();

        // 动态添加控件
        linearLayout = (LinearLayout) findViewById(R.id.settings_layout);
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
        // 先判断库中有无数据
        Cursor cursor = db.rawQuery("select value from Settings where keys = ?", new String[] {"week_sum"});
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

        week_sum_value.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    ((EditText) v).clearFocus();
                    return true;
                }
                return false;
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

        cur_week_value.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    ((EditText) v).clearFocus();
                    return true;
                }
                return false;
            }
        });
        linearLayout.addView(singleTextSetLayout);

        // 课程时间设置
        for (int i = 1; i <= course_num; i++) {
            SingleTimeSetLayout singleTimeSetLayout = new SingleTimeSetLayout(Settings.this, null);
            singleTimeSetLayout.setId(i);
            TextView course_hint = (TextView) singleTimeSetLayout.findViewById(R.id.time_set_hint);
            final TextView course_time = (TextView) singleTimeSetLayout.findViewById(R.id.time_set_value);

            // 判断时间信息是否已有
            // cursor = db.rawQuery("select time from TimeInfo where id = ?", new String[] {"" + i});
            cursor = db.rawQuery("select value from Settings where keys = ?", new String[] {"time" + i});
            course_hint.setText("第" + i + "节课：");
            if (cursor.getCount() == 1) {
                // 库中已有时间信息
                cursor.moveToFirst();
                String time = cursor.getString(cursor.getColumnIndex("value"));
                course_time.setText(time);
            }
            cursor.close();

            // 设置点击
            singleTimeSetLayout.setOnClickListener(new View.OnClickListener() {
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
                                    course_time.setText(save_time);
                                    Log.d(TAG, "setTime: " + save_time);
                                }
                            }, 0, 0, true);
                    timePickerDialog.setCancelable(false);
                    timePickerDialog.setCanceledOnTouchOutside(false);
                    timePickerDialog.show();
                }
            });
            linearLayout.addView(singleTimeSetLayout);
        }
    }

    private void store_settings() {
        // db.delete("Settings", "course_id = ?", new String[]{""+course_id});
        int count = linearLayout.getChildCount();
        for (int index = 0; index < count; index++) {
            if (linearLayout.getChildAt(index) instanceof SingleTextSetLayout) {
                // 文本设置
                SingleTextSetLayout singleTextSetLayout = (SingleTextSetLayout) linearLayout.getChildAt(index);
                EditText editText = (EditText) singleTextSetLayout.findViewById(R.id.text_set_value);
                if (((TextView)singleTextSetLayout.findViewById(R.id.text_set_hint))
                        .getText().toString().equals("本学期周数：")) {
                    // 本学期周数设置
                    int new_sum_week;
                    if (editText.getText().toString().equals("")) {
                        new_sum_week = 0;
                    } else {
                        String temp = editText.getText().toString().trim();
                        temp = temp.replaceAll("\n", "");
                        temp = temp.replaceAll("-", "");
                        new_sum_week = Integer.parseInt(temp);
                    }
                    db.execSQL("update Settings set value = ? where keys = ?",
                            new String[] {""+new_sum_week, "week_sum"});
                } else if (((TextView)singleTextSetLayout.findViewById(R.id.text_set_hint))
                        .getText().toString().equals("当前周数：")) {
                    // 当前周设置
                    int new_cur_week;
                    if (editText.getText().toString().equals("")) {
                        new_cur_week = 0;
                    }else {
                        String temp = editText.getText().toString().trim();
                        temp = temp.replaceAll("\n", "");
                        temp = temp.replaceAll("-", "");
                        new_cur_week = Integer.parseInt(temp);
                    }
                    Log.d(TAG, "onDestroy: before store, cur_week: "  + new_cur_week);
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
            } else if (linearLayout.getChildAt(index) instanceof SingleTimeSetLayout) {
                // 时间设置
                SingleTimeSetLayout singleTimeSetLayout = (SingleTimeSetLayout) linearLayout.getChildAt(index);
                TextView textView = singleTimeSetLayout.findViewById(R.id.time_set_value);
                int id = singleTimeSetLayout.getId();
                String time = textView.getText().toString();
                int cur_id = linearLayout.getChildAt(index).getId();
                db.execSQL("update Settings set value = ? where keys = ?",
                        new String[] {time, "time"+id});
                Log.d(TAG, "onDestroy: store time: " + time);
            }
        }

//        // 保存课前提醒的状态
//        Log.d(TAG, "onDestroy: " + switcher.isChecked());
//        String flag = switcher.isChecked()? "1": "0";
//        Log.d(TAG, "onDestroy: " + flag);
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//        db.execSQL("update Settings set selected = ? where id = ?", new String[] {flag, "1"});
    }

    @Override
    public void onBackPressed() {
        // 先保存，再返回。 onActivityResult 先于 OnDestroy
        store_settings();
        cur_week = Integer.parseInt(((EditText)linearLayout.getChildAt(1)
                .findViewById(R.id.text_set_value)).getText().toString());
        Intent intent = new Intent();
        intent.putExtra("cur_week", cur_week);
        setResult(RESULT_OK, intent);
        Log.d(TAG, "onBackPressed: set back data before finish. cur_week: " + cur_week);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");

    }
}
