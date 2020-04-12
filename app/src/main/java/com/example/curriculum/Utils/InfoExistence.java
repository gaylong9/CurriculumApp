package com.example.curriculum.Utils;

/**
 * 用于判断课程存在性 与 记录获取到的课程信息的类
 */

public class InfoExistence {
    static public int SQLite = 1;
    static public int SharedPreferences = 0;
    static public int Empty = -1;
    public int existence = -2;
    public String name = "" ;
    public String location = "";
    public String teacher = "";
}
