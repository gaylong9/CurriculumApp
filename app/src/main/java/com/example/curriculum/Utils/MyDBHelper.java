package com.example.curriculum.Utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * SQLite所用
 */

public class MyDBHelper extends SQLiteOpenHelper {

    public static final String CREATE_CURRICULUM = "create table Curriculum (" +
            "id integer primary key autoincrement," +
            "teacher text," +
            "location text," +
            "name text)";

    public static final String CREATE_BASICINFO = "create table BasicInfo (" +
            "id integer primary key autoincrement," +
            "time text)";

    private static final String TAG = "MyDBHelper";

    private Context mcontext;

    public MyDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mcontext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 建表
        db.execSQL(CREATE_CURRICULUM);
        db.execSQL(CREATE_BASICINFO);
        Log.d(TAG, "onCreate: " + "Created DB.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists Curriculum");
        db.execSQL("drop table if exists BasicInfo");
        onCreate(db);
    }
}
