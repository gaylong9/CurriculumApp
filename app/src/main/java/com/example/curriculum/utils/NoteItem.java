package com.example.curriculum.utils;

import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * note部分转为listview使用，但目前仍使用scrollview
 */

public class NoteItem {
    public int type = -1;
    public SingleNoteLayout singleNoteLayout = null;
    public SingleRecordLayout singleRecordLayout = null;
    String TAG = "NoteItem";

    public NoteItem(Object object) {
        if (object instanceof SingleNoteLayout) {
            singleNoteLayout = (SingleNoteLayout) object;
            type = 0;
        } else if (object instanceof SingleRecordLayout) {
            singleRecordLayout = (SingleRecordLayout) object;
            type = 1;
        }

        ArrayList<SingleNoteLayout> list = new ArrayList<SingleNoteLayout>();
        list.add(singleNoteLayout);
        Class<?> clz = list.getClass();
        try {
            Method method = clz.getMethod("add", Object.class);
            method.invoke(list, singleRecordLayout);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Log.d(TAG, "NoteItem: " + list.get(type).getClass());
    }
}
