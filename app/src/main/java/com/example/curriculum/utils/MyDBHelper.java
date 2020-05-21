package com.example.curriculum.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

/**
 * SQLite所用
 */

public class MyDBHelper extends SQLiteOpenHelper {

    public static final String CREATE_CURRICULUM = "create table Curriculum (" +
            "id integer primary key autoincrement," +
            "teacher text," +
            "location text," +
            "name text)";

//    public static final String CREATE_TIMEINFO = "create table TimeInfo (" +
//            "id integer primary key autoincrement," +
//            "time text)";

    public static final String CREATE_SETTINGS = "create table Settings (" +
            "id integer primary key autoincrement," +
            "type text," +
            "keys text," +
            "value text)";

    public static final String CREATE_NOTES = "create table Notes (" +
            "id integer primary key autoincrement," +
            "course_id integer," +
            "type integer," +
            "completed intger," +
            "content text)";

    private static final String TAG = "MyDBHelper";

    private Context context;

    public MyDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 建表
        db.execSQL(CREATE_CURRICULUM);
        // db.execSQL(CREATE_TIMEINFO);
        db.execSQL(CREATE_SETTINGS);
        db.execSQL(CREATE_NOTES);
//        db.execSQL("insert into Settings(id, selected) values(?, ?)",
//                new String[] {"1", "0"});   // 课前通知
        db.execSQL("insert into Settings(keys, value, type) values(?, ?, ?)",
                new String[] {"week_sum", "0", "week_num"});
        db.execSQL("insert into Settings(keys, value, type) values(?, ?, ?)",
                new String[] {"start_year", "0", "week_num"});
        db.execSQL("insert into Settings(keys, value, type) values(?, ?, ?)",
                new String[] {"start_week_of_year", "0", "week_num"});
        Log.d(TAG, "onCreate: " + "Created DB.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if ((oldVersion == 1 || oldVersion == 2) && newVersion == 3) {
            // 删除TimeInfo，转移数据
            try {
                Cursor cursor = db.rawQuery("select * from TimeInfe", null);
                if(cursor.moveToFirst()) {
                    do{
                        int id = cursor.getInt(cursor.getColumnIndex("id"));
                        String time = cursor.getString(cursor.getColumnIndex("time"));
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("key", "time" + id);
                        contentValues.put("time", time);
                        contentValues.put("type", "start_time");
                        db.insert("Settings", null, contentValues);
                    } while(cursor.moveToNext());
                }
                cursor.close();
                db.execSQL("drop table if exists TimeInfo");
                db.execSQL(CREATE_NOTES);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "数据库更新失败，可尝试卸载后重装", Toast.LENGTH_LONG);
            }
        } else {
            db.execSQL("drop table if exists Curriculum");
            // db.execSQL("drop table if exists TimeInfo");
            db.execSQL("drop table if exists Settings");
            db.execSQL("drop table if exists Notes");
            onCreate(db);
        }

    }
}
