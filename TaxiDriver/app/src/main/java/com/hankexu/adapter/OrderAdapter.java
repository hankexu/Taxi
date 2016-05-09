package com.hankexu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hankexu.bean.Passenger;
import com.hankexu.taxidriver.R;

import java.util.ArrayList;

/**
 * Created by hanke on 2016-01-10.
 */
public class OrderAdapter extends BaseAdapter{

    private ArrayList<Passenger> passengers;
    private Context context;

    public OrderAdapter(ArrayList<Passenger> passengers,Context context){
        this.passengers = passengers;
        this.context = context;
    }

    @Override
    public int getCount() {
        return passengers.size();
    }

    @Override
    public Object getItem(int position) {
        return getItem(position);
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
            convertView = LayoutInflater.from(context).inflate(R.layout.order_cell,null);
            viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
            viewHolder.tvInception = (TextView) convertView.findViewById(R.id.tv_inception);
            viewHolder.tvDestination = (TextView) convertView.findViewById(R.id.tv_destination);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvInception.setText(passengers.get(position).getInception());
        viewHolder.tvDestination.setText(passengers.get(position).getDestination());
        viewHolder.tvName.setText(passengers.get(position).getName());

        return convertView;
    }

    private static class ViewHolder{
        TextView tvName,tvInception,tvDestination;
    }
}
