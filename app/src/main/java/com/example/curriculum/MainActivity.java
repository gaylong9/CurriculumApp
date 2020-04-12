package com.example.curriculum;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.curriculum.Utils.DeleteDataConfirm;
import com.example.curriculum.Utils.GetNumDialogFragment;
import com.example.curriculum.Utils.InfoExistence;
import com.example.curriculum.Utils.LeftListViewAdapater;
import com.example.curriculum.Utils.MyDBHelper;
import com.example.curriculum.Utils.SetCourseDialogFragment;
import com.example.curriculum.Utils.SingleCourseLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements GetNumDialogFragment.GetNumConfirmListener,
        SetCourseDialogFragment.SetCourseInfoListener,
        DeleteDataConfirm.DeleteDataListener {

    private ListView listView;  // 左侧listView
    private int course_num = 0; // 一天中的总课程数
    private LinearLayout linearLayout;  // 右侧碎片中，课程框的父布局
    private int cur_weekday;    // 当前日 1：周一 2：周二
    private static final String TAG = "MainActivity";

    private MyDBHelper dbHelper;
    private SQLiteDatabase db;
    private SharedPreferences.Editor sp_editor;
    private SharedPreferences sp_reader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linearLayout = (LinearLayout) findViewById(R.id.right_linearlayout);
        listView = (ListView) findViewById(R.id.days);

        // 获取当前星期几
        Calendar calendar = Calendar.getInstance(); // 1：周日 2：周一
        cur_weekday = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (cur_weekday == 0) {
            cur_weekday = 7;
        }
        Log.d(TAG, "onCreate: cur_weekday: " + cur_weekday);

        // 填充左侧碎片
        inflateLeftFragment();

        // 打开数据库
        dbHelper = new MyDBHelper(this, "Course.db", null, 1);
        db = dbHelper.getWritableDatabase();
        sp_editor = MainActivity.this.getPreferences(0).edit();
        sp_reader = getSharedPreferences("MainActivity", 0);

        // 判断是否已设置过课程数
        Cursor cursor = db.rawQuery("select * from BasicInfo", null);
        course_num = cursor.getCount();
        cursor.close();
        // course_num = basicinfo_reader.getInt("course_num", 0);
        if (course_num == 0) {
            // 无课程，则获取每天课程数 & 动态填充右侧碎片
            GetNumDialogFragment getNumDialogFragment = new GetNumDialogFragment();
            getNumDialogFragment.show(getSupportFragmentManager(), null);
        } else {
            // 有课程，正常读取，填入碎片中
            addCourseLayout();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.clear_data, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_data:
                DeleteDataConfirm deleteDataConfirm = new DeleteDataConfirm();
                deleteDataConfirm.show(getSupportFragmentManager(), null);
                break;
        }
        return true;
    }

    // 填充左侧碎片
    private void inflateLeftFragment() {
        final List<String> days = new ArrayList<>();
        days.add("星期一");
        days.add("星期二");
        days.add("星期三");
        days.add("星期四");
        days.add("星期五");
        days.add("星期六");
        days.add("星期日");
        final LeftListViewAdapater adapter = new LeftListViewAdapater(
                MainActivity.this, R.layout.listview_item, days);
        adapter.setCurrentItem(cur_weekday - 1);    // 设置初始点击为当前日
        listView.setAdapter(adapter);
        // 设置点击，根据点击修改右侧显示内容
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.setCurrentItem(position);
                adapter.notifyDataSetChanged();
                cur_weekday = position + 1; // position: 0：周一 1：周二
                linearLayout.removeAllViews();  // 先清空右侧
                addCourseLayout();  // 重新布局
            }
        });
    }

    // 动态填充右侧布局
    private void addCourseLayout() {
        for (int i = 1; i <= course_num; i++) {
            // 根据一天中课程总数，动态添加右侧课程框数
            final SingleCourseLayout singleCourseLayout = new SingleCourseLayout(MainActivity.this, null);
            int id = cur_weekday*100 + i;   // id与星期几 第几节课有关
            singleCourseLayout.setId(id);
            switch (i % 3) {
                case 0:
                    singleCourseLayout.setBackgroundResource(R.drawable.shape1);
                    break;
                case 1:
                    singleCourseLayout.setBackgroundResource(R.drawable.shape2);
                    break;
                case 2:
                    singleCourseLayout.setBackgroundResource(R.drawable.shape3);
                    break;
            }

            // 判断课程信息是否已有
            InfoExistence info = new InfoExistence();
            info = getInfo(id, singleCourseLayout);

            // 获得课程已有信息后，在右侧显示
            if (info.existence == InfoExistence.SQLite) {
                ((TextView) singleCourseLayout.findViewById(R.id.course_name)).setText(info.name);
                ((TextView) singleCourseLayout.findViewById(R.id.course_location)).setText(info.location);
                ((TextView) singleCourseLayout.findViewById(R.id.teacher)).setText(info.teacher);

            }

            // 设置右侧点击
            singleCourseLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 点击课程，添加/修改/删除课程信息
                    // 从数据库判断是否已存在课程信息
                    InfoExistence click_info = new InfoExistence();
                    click_info = getInfo(v.getId(), singleCourseLayout);
                    Log.d(TAG, "onClick: right info: " + click_info.name + "," + click_info.location + "," + click_info.teacher);
                    SetCourseDialogFragment setCourseDialogFragment = new SetCourseDialogFragment(
                            singleCourseLayout.getId(),
                            click_info.existence==InfoExistence.SQLite,
                            click_info.name, click_info.location, click_info.teacher);
                    setCourseDialogFragment.show(getSupportFragmentManager(), null);
                }
            });
            linearLayout.addView(singleCourseLayout);
        }
    }

    // 判断课程信息存在性，并返回
    private InfoExistence getInfo(int id, SingleCourseLayout singleCourseLayout) {
        InfoExistence info = new InfoExistence();
        // 根据id和数据库内容，判断课程信息存在性
        Cursor cursor = db.rawQuery("select * from Curriculum where id = ?", new String[] {"" + singleCourseLayout.getId()});
        if (cursor.getCount() == 1) {
            // 已有课程信息，在SQLite中
            info.existence = InfoExistence.SQLite;
            cursor.moveToFirst();
            info.name = cursor.getString(cursor.getColumnIndex("name"));
            info.location = cursor.getString(cursor.getColumnIndex("location"));
            info.teacher = cursor.getString(cursor.getColumnIndex("teacher"));
        } else {
            try {
                // if (!sp_reader.getString("" + id + "name", "").equals("")) {
                    // 无课程信息，但SharedPreferences中有临时修改的信息
                    info.existence = InfoExistence.SharedPreferences;
                    Log.d(TAG, "addCourseLayout: enter sp existence judge.");
                    info.name = sp_reader.getString("" + id + "name", "");
                    info.location = sp_reader.getString("" + id + "location", "");
                    info.teacher = sp_reader.getString("" + id + "teacher", "");
                // }
            } catch (Exception NullPointerException) {
                // SharedPreferences中也无临时信息，则信息彻底暂无
                info.existence = InfoExistence.Empty;
                Log.d(TAG, "addCourseLayout: " + id + " sp is empty.");
                info.name = "";
                info.location = "";
                info.teacher = "";
            }
        }
        cursor.close();
        return info;
    }

    @Override
    // 获取每天课程数
    public void onClickComplete(int num) {
        // 获取课程数，并将信息存入数据库
        course_num = num;
        for (int i = 1; i <= num; i++) {
            db.execSQL("insert into BasicInfo(id, time) values(?, ?)",
                    new String[] {"" + i, ""});
        }
        // 布局中动态增加课程布局
        addCourseLayout();
    }

    @Override
    // 获取课程信息
    public void onClickResult(int id, int state, boolean existence, String course_name, String course_location, String course_teacher) {
        Log.d(TAG, "onClickResult: " + course_name + "," + course_location + "," + course_teacher);
        SingleCourseLayout singleCourseLayout = (SingleCourseLayout) findViewById(id);
        TextView name = (TextView) singleCourseLayout.findViewById(R.id.course_name);
        TextView location = (TextView) singleCourseLayout.findViewById(R.id.course_location);
        TextView teacher = (TextView) singleCourseLayout.findViewById(R.id.teacher);
        String old_name = name.getText().toString();
        String old_location = location.getText().toString();
        String old_teacher = teacher.getText().toString();

        if (state == SetCourseDialogFragment.DELETE) {
            // 点击删除按钮，从SQLite中删除
            // textView去除内容
            name.setText("");
            location.setText("");
            teacher.setText("");
            // 删除本课程
            db.execSQL("delete from Curriculum where id = ?", new String[] {"" + id});
        } else if (state == SetCourseDialogFragment.CONFIRM){
            // 点击确定按钮，存入SQLite
            // TextView添加内容
            name.setText(course_name);
            location.setText(course_location);
            teacher.setText(course_teacher);
            // 存入/更改SQLite
            if (existence && (!old_name.equals(course_name) || !old_location.equals(course_location)
            || !old_teacher.equals(course_teacher))) {
                // 数据库中有本节课，且内容有变化
                db.execSQL("update Curriculum set name = ?, location = ?, teacher = ? where id = ?",
                        new String[] {course_name, course_location, course_teacher, "" + id});
            } else if (!existence) {
                // 数据库中无本节课
                db.execSQL("insert into Curriculum(id, name, location, teacher) values(?, ?, ?, ?)",
                        new String[] {"" + id, course_name, course_location, course_teacher});
            }
        } else {
            // 点击取消按钮
            if (!existence) {
                // 新建课程信息中断，存入sp
                Log.d(TAG, "onClickResult: save into sp" + "" + id + ": " + course_name + "," + course_location + "," +course_teacher);
                sp_editor.putString("" + id + "name", course_name);
                sp_editor.putString("" + id + "location", course_location);
                sp_editor.putString("" + id + "teacher", course_teacher);
                sp_editor.apply();
                String temp = sp_reader.getString("" + id + "name", "");
                Log.d(TAG, "onClickResult: get in time: " + temp);
                // 修改课程信息中断，不存入sp
            }
        }
    }

    @Override
    // 清空数据，重新设置
    public void onDeleteConfirmClick(boolean confirm) {
        if (confirm) {
            // 确定清空
            sp_editor.clear();
            sp_editor.apply();
            db.execSQL("delete from Curriculum");
            db.execSQL("delete from BasicInfo");

            // 重启
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }
}
