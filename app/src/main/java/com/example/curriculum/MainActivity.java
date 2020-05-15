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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.curriculum.utils.DeleteDataConfirm;
import com.example.curriculum.utils.GetNumDialogFragment;
import com.example.curriculum.utils.InfoExistence;
import com.example.curriculum.utils.LeftListViewAdapater;
import com.example.curriculum.utils.MyDBHelper;
import com.example.curriculum.utils.SetCourseDialogFragment;
import com.example.curriculum.utils.SingleCourseLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements GetNumDialogFragment.GetNumConfirmListener,
        SetCourseDialogFragment.SetCourseInfoListener,
        DeleteDataConfirm.DeleteDataListener {

    private ListView listView;          // 左侧listView
    private int course_num = 0;         // 一天中的总课程数
    private LinearLayout linearLayout;  // 右侧碎片中，课程框的父布局
    private int cur_weekday;            // 当前日 1：周一 2：周二
    private int cur_week;               // 当前周
    private int hour;                   // 设置时间所用
    private int minute;
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

        // 打开数据库
        dbHelper = new MyDBHelper(this, "Course.db", null, 3);
        db = dbHelper.getWritableDatabase();
        sp_editor = MainActivity.this.getPreferences(0).edit();
        sp_reader = getSharedPreferences("MainActivity", 0);

        // 获取当前星期几
        Calendar calendar = Calendar.getInstance(); // 1：周日 2：周一
        cur_weekday = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (cur_weekday == 0) {
            cur_weekday = 7;
        }

        // 设置当前周
        setCurWeek();

        // 填充左侧碎片
        inflateLeftFragment();

        // 判断是否已设置过每日课程数
        Cursor cursor = db.rawQuery("select value from Settings where type = ?",
                new String[] {"start_time"});
        course_num = cursor.getCount();
        cursor.close();
        if (course_num == 0) {
            // 无课程，则获取每天课程数与时间 & 动态填充右侧碎片
            GetNumDialogFragment getNumDialogFragment = new GetNumDialogFragment();
            getNumDialogFragment.show(getSupportFragmentManager(), null);
        } else {
            // 有课程，正常读取，填入碎片中
            addCourseLayout();
        }
    }

    // 获取当前周，设置title
    private void setCurWeek() {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY); // 设置每周的第一天为星期一
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); // 每周从周一开始
        cal.setMinimalDaysInFirstWeek(7); // 设置每周最少为7天
        cal.setTime(new Date());
        int cur_week_of_year = cal.get(Calendar.WEEK_OF_YEAR);
        Log.d(TAG, "onCreate: cur_week_of_year:" + cur_week_of_year);

        Cursor cursor = db.rawQuery("select * from Settings where keys = ?",
                new String[] {"start_year"});
        cursor.moveToFirst();
        int start_year = Integer.parseInt(cursor.getString(cursor.getColumnIndex("value")));
        cursor.close();
        cursor = db.rawQuery("select * from Settings where keys = ?",
                new String[] {"start_week_of_year"});
        cursor.moveToFirst();
        int start_week_of_year = Integer.parseInt(cursor.getString(cursor.getColumnIndex("value")));
        cursor.close();
        cursor = db.rawQuery("select value from Settings where keys = ?",
                new String[] {"week_sum"});
        cursor.moveToFirst();
        int week_sum = Integer.parseInt(cursor.getString(cursor.getColumnIndex("value")));
        cursor.close();

        Log.d(TAG, "setCurWeek: " + start_year + start_week_of_year);

        if (start_year == 0 || start_week_of_year == 0) {
            this.cur_week = 0;
            setTitle("课程表");
        } else if (start_year == cal.get(Calendar.YEAR)) {
            // 今年开始，当前学期周 = 当前自然周 - 学期开始周 + 1
            this.cur_week = cur_week_of_year - start_week_of_year + 1;
            if (cur_week <= week_sum && cur_week != 0) {
                setTitle("第" + this.cur_week + "周");
            } else if (cur_week > week_sum && cur_week != 0) {
                setTitle("第" + week_sum + "周");
            } else {
                setTitle("课程表");
            }
        } else {
            // 去年开始，当前学期周 = 去年总周数 - 学期开始周 + 1 + 当前自然周
            Calendar cal1 = Calendar.getInstance();
            cal.setFirstDayOfWeek(Calendar.MONDAY); // 设置每周的第一天为星期一
            cal1.set(Calendar.YEAR, cal.get(Calendar.YEAR) - 1); // Set only year
            cal1.set(Calendar.MONTH, Calendar.DECEMBER); // Don't change
            cal1.set(Calendar.DAY_OF_MONTH, 31); // Don't change
            int totalWeeks = cal1.get(Calendar.WEEK_OF_YEAR);
            this.cur_week = totalWeeks - start_week_of_year + 1 + cur_week_of_year;
            if (cur_week <= week_sum && cur_week != 0) {
                setTitle("第" + this.cur_week + "周");
            } else if (cur_week > week_sum && cur_week != 0) {
                setTitle("第" + week_sum + "周");
            } else {
                setTitle("课程表");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_data:
                DeleteDataConfirm deleteDataConfirm = new DeleteDataConfirm();
                deleteDataConfirm.show(getSupportFragmentManager(), null);
                break;
            case R.id.set_time_item:
                Intent intent_to_settings = new Intent(MainActivity.this, Settings.class);
                intent_to_settings.putExtra("course_num", course_num);
                intent_to_settings.putExtra("cur_week", cur_week);
                startActivityForResult(intent_to_settings, 1);
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
                cur_weekday = position + 1;     // position: 0：周一 1：周二
                linearLayout.removeAllViews();  // 先清空右侧
                addCourseLayout();              // 重新布局
            }
        });
    }

    // 动态填充右侧布局
    private void addCourseLayout() {
        Log.d(TAG, "addCourseLayout: set right layout.");
        // 获取时间信息
        //Cursor time_cursor = db.rawQuery("select * from TimeInfo", null);
        Cursor time_cursor = db.rawQuery("select * from Settings where type = ?",
                new String[] {"start_time"});

        time_cursor.moveToFirst();

        // 逐个填充右侧
        for (int i = 1; i <= course_num; i++) {
            // 根据一天中课程总数，动态添加右侧课程框数
            final SingleCourseLayout singleCourseLayout = new SingleCourseLayout(MainActivity.this, null);
            final int id = cur_weekday*100 + i;   // id：星期几1位，第几节课2位
            singleCourseLayout.setId(id);
            switch (i % 5) {
                case 0:
                    singleCourseLayout.setBackgroundResource(R.drawable.shape1);
                    break;
                case 1:
                    singleCourseLayout.setBackgroundResource(R.drawable.shape2);
                    break;
                case 2:
                    singleCourseLayout.setBackgroundResource(R.drawable.shape3);
                    break;
                case 3:
                    singleCourseLayout.setBackgroundResource(R.drawable.shape4);
                    break;
                case 4:
                    singleCourseLayout.setBackgroundResource(R.drawable.shape5);
                    break;
            }

            // 判断课程信息是否已有
            InfoExistence info = new InfoExistence();
            info = getInfo(id, singleCourseLayout);
            Log.d(TAG, "addCourseLayout: turn to id: " + id);

            // 获得课程已有信息后，在右侧显示
            if (info.existence == InfoExistence.SQLite) {
                Log.d(TAG, "addCourseLayout: will set course " + info.name);
                ((TextView) singleCourseLayout.findViewById(R.id.course_name)).setText(info.name);
                TextView location = ((TextView) singleCourseLayout.findViewById(R.id.course_location));
                if (info.location.equals("")) {
                    location.setVisibility(View.GONE);
                } else {
                    location.setVisibility(View.VISIBLE);
                    location.setText(info.location);
                }
                TextView teacher = ((TextView) singleCourseLayout.findViewById(R.id.teacher));
                if (info.teacher.equals("")) {
                    teacher.setVisibility(View.GONE);
                } else {
                    teacher.setVisibility(View.VISIBLE);
                    teacher.setText(info.teacher);
                }
                ((TextView) singleCourseLayout.findViewById(R.id.course_time)).setText(
                        time_cursor.getString(time_cursor.getColumnIndex("value")));
                Log.d(TAG, "addCourseLayout: get time : " +
                        time_cursor.getString(time_cursor.getColumnIndex("value")));
            }
            time_cursor.moveToNext();

            // 设置右侧点击
            // 点击课程便签
            ImageView course_note = (ImageView) singleCourseLayout.findViewById(R.id.single_course_note);
            course_note.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String course_name = ((TextView) singleCourseLayout.findViewById(R.id.course_name))
                            .getText().toString();
                    Intent intent_to_note = new Intent(MainActivity.this, Note.class);
                    intent_to_note.putExtra("course_name", course_name);
                    intent_to_note.putExtra("course_id", id);
                    startActivity(intent_to_note);
                }
            });
            // 点击课程信息设置
            ImageView course_setting = (ImageView) singleCourseLayout.findViewById(R.id.single_course_setting);
            course_setting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: sindleCLID " + singleCourseLayout.getId());
                    Log.d(TAG, "onClick: now id " + ((SingleCourseLayout)v.getParent().getParent().getParent()).getId());
                    // 点击课程，添加/修改/删除课程信息
                    // 从数据库判断是否已存在课程信息
                    InfoExistence click_info;
                    click_info = getInfo(((SingleCourseLayout)v.getParent().getParent().getParent()).getId(), singleCourseLayout);
                    Log.d(TAG, "onClick: right info: " + click_info.name + "," + click_info.location + "," + click_info.teacher);
                    SetCourseDialogFragment setCourseDialogFragment = new SetCourseDialogFragment(
                            singleCourseLayout.getId(),
                            click_info.existence==InfoExistence.SQLite,
                            click_info.name, click_info.location, click_info.teacher);
                    setCourseDialogFragment.show(getSupportFragmentManager(), null);
                }
            });
            linearLayout.addView(singleCourseLayout, linearLayout.getChildCount());
        }
        time_cursor.close();
    }

    // 判断课程信息存在性，并返回
    private InfoExistence getInfo(int id, SingleCourseLayout singleCourseLayout) {
        Log.d(TAG, "getInfo: get id: " + id);
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
                if (!sp_reader.getString("" + id + "name", "").equals("")) {
                    // 无课程信息，但SharedPreferences中有临时修改的信息
                    info.existence = InfoExistence.SharedPreferences;
                    info.name = sp_reader.getString("" + id + "name", "null");
                    info.location = sp_reader.getString("" + id + "location", "null");
                    info.teacher = sp_reader.getString("" + id + "teacher", "null");
                Log.d(TAG, "addCourseLayout: get info from sp: " + info.name + " " + info.location + "" + info.teacher);
                } else {
                    Log.d(TAG, "addCourseLayout: " + id + " sp is empty.");
                }
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
    public void onGetNumClickComplete(int num) {
        // 获取课程数，并将信息存入数据库
        course_num = num;
        for (int i = 1; i <= num; i++) {
            db.execSQL("insert into Settings(keys, value, type) values(?, ?, ?)",
                    new String[] {"time"+i, "", "start_time"});
        }
        // 布局中动态增加课程布局
        addCourseLayout();
    }

    @Override
    // 获取课程信息
    public void onSetCourseInfoClickResult(int id, int state, boolean existence, String course_name, String course_location, String course_teacher) {
        Log.d(TAG, "onClickResult: " + course_name + "," + course_location + "," + course_teacher);
        SingleCourseLayout singleCourseLayout = (SingleCourseLayout) findViewById(id);
        TextView name = (TextView) singleCourseLayout.findViewById(R.id.course_name);
        TextView location = (TextView) singleCourseLayout.findViewById(R.id.course_location);
        TextView teacher = (TextView) singleCourseLayout.findViewById(R.id.teacher);
        TextView time = (TextView) singleCourseLayout.findViewById(R.id.course_time);
        String old_name = name.getText().toString();
        String old_location = location.getText().toString();
        String old_teacher = teacher.getText().toString();

        if (state == SetCourseDialogFragment.DELETE) {
            // 点击删除按钮，从SQLite中删除
            // textView去除内容
            name.setText("");
            location.setText("");
            teacher.setText("");
            time.setText("");
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
                linearLayout.removeAllViews();
                addCourseLayout();
            } else if (!existence) {
                // 数据库中无本节课
                db.execSQL("insert into Curriculum(id, name, location, teacher) values(?, ?, ?, ?)",
                        new String[] {"" + id, course_name, course_location, course_teacher});
                linearLayout.removeAllViews();
                addCourseLayout();
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
                Log.d(TAG, "onClickResult: get sp in time: " + temp);
                // 修改课程信息中断，不存入sp
            }
        }
    }

    @Override
    // 清空数据，重新设置，重设数据
    public void onDeleteConfirmClick(boolean confirm) {
        if (confirm) {
            // 确定清空
            sp_editor.clear();
            sp_editor.apply();
            db.execSQL("delete from Curriculum");
            // db.execSQL("delete from TimeInfo");
            db.execSQL("update Settings set value = ? where type = ?",
                    new String[] {"0", "week_num"});
            db.execSQL("delete from Settings where type = ?",
                    new String[] {"start_time"});
            db.execSQL("delete from Notes");

            // 重启
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case 1:
                // 设置的返回, 重置当前信息
                if (data != null) {
                    Log.d(TAG, "onActivityResult: Settings result. data exists.");
                    linearLayout.removeAllViews();
                    addCourseLayout();
                    Log.d(TAG, "onActivityResult: get cur_week: " +  data.getIntExtra("cur_week", 0));
                    this.cur_week = data.getIntExtra("cur_week", 0);
                    Log.d(TAG, "onActivityResult: after set, cur_week: " + this.cur_week);
                    if (cur_week != 0) {
                        // setTitle("第" + this.cur_week + "周");
                        setTitle("ha !");
                        setTitle("ha !");
                        Log.d(TAG, "onActivityResult: setTitle" + this.cur_week);
                    }
                    setCurWeek();
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
