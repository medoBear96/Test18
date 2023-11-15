package com.example.test18;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class LE_ListAdapter extends ArrayAdapter<LE_Device> {

    Activity activity;
    int layoutResourceID;
    ArrayList<LE_Device> devices;


    public LE_ListAdapter(Activity activity, int resource, ArrayList<LE_Device> devices) {
        super(activity.getApplicationContext(), resource, devices);

        this.activity= activity;
        this.layoutResourceID = resource;
        this.devices = devices;
    }
    public LE_Device getDevice(int position) {return devices.get(position);}

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater) activity.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layoutResourceID, parent, false);
        }
        LE_Device device = devices.get(position);
        String name = device.getName();
        String address = device.getAddress();
        int rssi = device.getRssi();

        TextView tv_name = (TextView) convertView.findViewById(R.id.tv_name);
        if(name!= null && name.length()>0){
            tv_name.setText(device.getName());
        } else {
            tv_name.setText("NO NAME");
        }
        TextView tv_rssi = (TextView) convertView.findViewById(R.id.tv_rssi);
        tv_rssi.setText("RSSI: " + Integer.toString(rssi));

        TextView tv_macaddr = (TextView) convertView.findViewById(R.id.tv_address);
        if ( address != null && address.length() >0) {
            tv_macaddr.setText(device.getAddress());
        } else {
            tv_macaddr.setText("NO ADDRESS");
        }
        return convertView;
    }

}