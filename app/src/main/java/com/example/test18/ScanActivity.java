package com.example.test18;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;

public class ScanActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private final static String TAG = "ScannerActivity";

    public static final int REQUEST_ENABLE_BT = 1;              //BT enable code

    private Button scan_button, connect_button, third_button;                 //UI
    private TextView status_tv,address_tv, name_tv;

    private DataKeeper dataKeeper = DataKeeper.getInstance();   //Singleton class

    private ArrayList<LE_Device> devices_arraylist;             //Scanned devices
    private HashMap<String, LE_Device> devices_hashmap;
    private LE_ListAdapter le_listAdapter;                      //List Adapter
    private LE_Scan le_scan;                                    //Scanner

    private LE_Device selected_device;                          //Selected Device
    private String selected_address;
    private String selected_name;


    //INSERT BROADCAST RECEIVER
    private final BroadcastReceiver localBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            updateConnectButton();
            updateConnectionStatus();
            if ("ACTION_FS_RUNNING".equals(action)){
                updateConnectButton();
            } else if ("ACTION_FS_STOPPING".equals(action)){
                updateConnectButton();
            }

        }
    };

    //ACTIVITY LIFE CYCLE
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        //instantiate new scan variables
        le_scan = new LE_Scan(this, 7500, -75);
        devices_arraylist = new ArrayList<>();
        devices_hashmap   = new HashMap<>();
        le_listAdapter    = new LE_ListAdapter(this, R.layout.device_list, devices_arraylist);

        //set the scrollview
        ListView listView = new ListView(this);
        listView.setAdapter(le_listAdapter);
        listView.setOnItemClickListener(this);
        ((ScrollView) findViewById(R.id.scrollView)).addView(listView);

        //set the buttons
        scan_button    = (Button) findViewById(R.id.scan_button);
        findViewById(R.id.scan_button).setOnClickListener(this);
        connect_button = (Button) findViewById(R.id.connect_button);
        findViewById(R.id.connect_button).setOnClickListener(this);
        third_button   = (Button) findViewById(R.id.button3);
        findViewById(R.id.button3).setOnClickListener(this);


        //set the text views
        status_tv  = (TextView) findViewById(R.id.status_tv);
        address_tv = (TextView) findViewById(R.id.address_tv);
        name_tv    = (TextView) findViewById(R.id.name_tv);

        //check if connected or not
        updateConnectionStatus();
        updateConnectButton();
    }

    @Override
    protected void onStart(){
        super.onStart();
        registerReceiver(localBroadcastReceiver, localIntentFilter());
        updateConnectionStatus();
    }
    @Override
    protected void onStop(){
        super.onStop();
        unregisterReceiver(localBroadcastReceiver);
        stopScan();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Turn on BT", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //USER INTERFACE INTERACTION
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.scan_button:
                if (!le_scan.isScanning()){
                    Log.i(TAG, "Scanning started");
                    startScan();
                } else {
                    Log.i(TAG, "Scanning stopped");
                    stopScan();
                }
                break;
            case R.id.connect_button:
                if (dataKeeper.isFSRunning){                                                    //if foreground service is running stop it
                    Log.i(TAG, "Foreground service stopped");
                    stopFGService();
                    //connect_button.setEnabled(false);
                } else if (!dataKeeper.isFSRunning && selected_address != null) {               //else start fg service
                    Log.i(TAG, "Foreground service started");
                    startFGService(selected_address);
                }
                break;
            case R.id.button3:
                Log.i(TAG, "Data Read Button pressed");
                if (!dataKeeper.le_charateristics.isEmpty()){
                    broadcastUpdate("READ");
                } else {
                    Log.e(TAG, "Void list");
                }
                break;
            default:
                break;

        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        selected_device = le_listAdapter.getDevice(i);
        if (selected_device==null){
            Log.d(TAG, "null");
            return;
        } else {
            selected_address = selected_device.getAddress();
            selected_name = selected_device.getName();
            address_tv.setText("Device Address: "+selected_address);
            if (selected_name==null){
                name_tv.setText("Device Name: NO NAME");
            } else {
                name_tv.setText("Device Name: "+selected_name);
            }
        }
        if (selected_address!= null){
            Log.d(TAG, "New selected address "+selected_address);
        }
    }



    // SCAN FUNCTIONS
    public void startScan(){
        Log.d(TAG, "Scan Starting");
        scan_button.setText("STOP");
        devices_hashmap.clear();
        devices_arraylist.clear();

        le_listAdapter.notifyDataSetChanged();
        le_scan.start();
    }
    public void stopScan(){
        Log.d(TAG, "Scan Ending");
        scan_button.setText("SCAN");
        le_scan.stop();
    }
    public void addDevice(BluetoothDevice bluetoothDevice, int new_rssi){
        String address = bluetoothDevice.getAddress();
        if(!devices_hashmap.containsKey(address)){
            LE_Device le_device = new LE_Device(bluetoothDevice, new_rssi);
            devices_hashmap.put(address, le_device);
            devices_arraylist.add(le_device);
        } else {
            devices_hashmap.get(address).setRssi(new_rssi);
        }
        le_listAdapter.notifyDataSetChanged();
    }

    // FOREGROUND SERVICE FUNCTIONS
    public void startFGService(String input){
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        serviceIntent.putExtra("inputExtra", input);
        ContextCompat.startForegroundService(this, serviceIntent);
    }
    public void stopFGService(){
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        stopService(serviceIntent);
    }

    // LOCAL UTILITIES
    private void updateConnectionStatus(){
        if(dataKeeper.isConnected){
            status_tv.setText("Status: CONNECTED");
        } else {
            status_tv.setText("Status: NOT CONNECTED");
        }
    }
    private void updateConnectButton(){
        if(dataKeeper.isFSRunning){
            connect_button.setText("DISCONNECT");
        } else {
            connect_button.setText("CONNECT");
        }
    }

    private static IntentFilter localIntentFilter(){
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("ACTION_FS_RUNNING");
        intentFilter.addAction("ACTION_FS_STOPPING");
        intentFilter.addAction("ACTION_GATT_CONNECTED");
        intentFilter.addAction("ACTION_GATT_DISCONNECTED");

        return intentFilter;
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
        Log.d(TAG, "Brodcast GO");
    }

}
