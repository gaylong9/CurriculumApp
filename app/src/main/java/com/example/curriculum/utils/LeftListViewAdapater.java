package com.example.curriculum.utils;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.curriculum.R;

import java.util.List;

public class LeftListViewAdapater extends ArrayAdapter {

    private int currentItem = -1;
    private int resId;
    private String TAG = "LeftListViewAdapter";

    public LeftListViewAdapater(@NonNull Context context, int resource, @NonNull List objects) {
        super(context, resource, objects);
        resId = resource;	// 将子项布局id保留
    }

    class ViewHolder {
        TextView textView;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String weekday = (String) getItem(position);
        View view;
        ViewHolder viewHolder;
        if(convertView != null) {
            view = convertView;
            viewHolder = (ViewHolder)view.getTag(); // 重新获取viewholder
        }else {
            view = LayoutInflater.from(getContext()).inflate(resId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.textView = (TextView) view.findViewById(R.id.list_item);
            view.setTag(viewHolder);
        }
        viewHolder.textView.setText(weekday);
        if (currentItem == position) {
            //如果被点击，设置当前TextView颜色
            viewHolder.textView.setTextColor(Color.parseColor("#2c4fce"));
        } else {
            //如果没有被点击，设置当前TextView颜色
            viewHolder.textView.setTextColor(Color.BLACK);
        }
        return view;
    }

    public void setCurrentItem(int currentItem) {
        this.currentItem = currentItem;
    }
}
