package com.example.test18;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class LE_Scan {
    private ScanActivity parent;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private boolean isScanning = false;
    private Handler handler;
    private long scanPeriod;
    private int signal;
    private String TAG = "Scanner";

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            int rssi = result.getRssi();
            Log.d(TAG, "found "+ device.getAddress());

            handler.post(new Runnable() {
                @Override
                public void run() { parent.addDevice(device, rssi); }
            });

        }
    };



    public LE_Scan(ScanActivity parent, long scanPeriod, int signal){
        this.parent = parent;
        this.scanPeriod = scanPeriod;
        this.signal = signal;

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        handler = new Handler(Looper.getMainLooper());
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

    }

    // SCAN FUNCTION

    //@SuppressLint("MissingPermission")
    @SuppressLint("MissingPermission")
    private void scanLeDevice(final boolean enable){

        if(enable && !isScanning){

            Log.d(TAG,"starting");

            bluetoothLeScanner.startScan(scanCallback);
            isScanning = true;
            handler.postDelayed(() -> {
                stop();
                Log.d("handler", "time out");
            }, 7500);
            //bluetoothAdapter.startLeScan(leScanCallback);


        }
    }

    // IS SCANNING

    public boolean isScanning() { return isScanning;}


    //START AND STOP FUNCTIONS

    public void start() {
        if(!LE_Utilities.check_BLE(bluetoothAdapter)){
            LE_Utilities.request_permission(parent);
            parent.stopScan();
        } else {
            Log.d(TAG, "scan = true");
            scanLeDevice(true);
        }
    }

    @SuppressLint("MissingPermission")
    public void stop() {
        if(isScanning){
            bluetoothLeScanner.stopScan(scanCallback);
            isScanning =false;
            parent.stopScan();
        }
    }

}