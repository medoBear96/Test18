package com.example.test18;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;

public class LE_Device {
    private BluetoothDevice bluetoothDevice;
    private int rssi;

    public LE_Device(BluetoothDevice bluetoothDevice) { this.bluetoothDevice=bluetoothDevice;}
    public LE_Device(BluetoothDevice bluetoothDevice, int rssi) { this.bluetoothDevice=bluetoothDevice; this.rssi = rssi;}

    public String getAddress() { return bluetoothDevice.getAddress();}
    @SuppressLint("MissingPermission")
    public String getName() { return bluetoothDevice.getName();}
    public void setRssi(int rssi) { this.rssi =rssi;    }
    public int getRssi() {return rssi;}
}
