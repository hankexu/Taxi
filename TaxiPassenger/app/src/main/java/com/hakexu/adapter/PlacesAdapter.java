package com.hakexu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hakexu.taxipassenger.R;

import java.util.ArrayList;

/**
 * Created by hanke on 2016-01-06.
 * 地址选择列表适配器
 */
public class PlacesAdapter extends BaseAdapter{

    private ArrayList<String> places;
    private Context context;

    public PlacesAdapter(ArrayList<String> places, Context context) {
        this.places = places;
        this.context = context;
    }

    @Override
    public int getCount() {
        return places.size();
    }

    @Override
    public Object getItem(int position) {
        return places.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView==null){
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.place_cell_view,null);
            viewHolder.tv = (TextView) convertView.findViewById(R.id.tv_place);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tv.setText(places.get(position));
        return convertView;
    }

    private static class ViewHolder{
        TextView tv;
    }
}
