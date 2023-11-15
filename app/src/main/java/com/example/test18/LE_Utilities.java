package com.example.test18;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;

public class LE_Utilities {
    public static boolean check_BLE(BluetoothAdapter bluetoothAdapter){
        if ( bluetoothAdapter == null || !bluetoothAdapter.isEnabled()){
            return false;
        } else {
            return true;
        }
    }



    @SuppressLint("MissingPermission")
    public static void request_permission(Activity activity){
        Intent enableBTintent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(enableBTintent, ScanActivity.REQUEST_ENABLE_BT);
    }




}