package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.List;

public class MyAdapter extends BaseAdapter {
    List<DataModel> allItems;
    Context mContext;
    public MyAdapter(Context ct, List<DataModel> allItems) {
        this.allItems = allItems;
        mContext=ct;
    }

    @Override
    public int getCount() {
        return allItems.size();
    }

    @Override
    public Object getItem(int position) {
        return allItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = new ViewHolder();
        LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = mInflater.inflate(R.layout.list, parent, false);
        viewHolder.text = convertView.findViewById(R.id.textView);
        viewHolder.icon = convertView.findViewById(R.id.imageView);
        DataModel mModel=(DataModel) getItem(position);
        viewHolder.text.setText(mModel.getTitle());
        viewHolder.icon.setImageBitmap(mModel.getBitmap());

        return convertView;
    }
}

class ViewHolder {
    TextView text;
    ImageView icon;
}