package com.example.test18;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 2;
    private TextView status_tv,address_tv, name_tv;
    private DataKeeper dataKeeper = DataKeeper.getInstance();

    private TextView lat_tv, lon_tv, ori_tv;

    private final BroadcastReceiver localBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if ("UPDATE_DATA".equals(action)){
                updateData();
            } else {
            }

        }
    };
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.BLUETOOTH_ADMIN,
                    android.Manifest.permission.BLUETOOTH_CONNECT,
                    android.Manifest.permission.BLUETOOTH_SCAN,
                    android.Manifest.permission.BLUETOOTH,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, PERMISSION_REQUEST_CODE);
        }
        status_tv = (TextView) findViewById(R.id.status_tv_m);
        address_tv = (TextView) findViewById(R.id.address_tv_m);
        name_tv = (TextView) findViewById(R.id.name_tv_m);

        lat_tv = (TextView) findViewById(R.id.lat_tv);
        lon_tv = (TextView) findViewById(R.id.lon_tv);
        ori_tv = (TextView) findViewById(R.id.ori_tv);
        updateConnectionStatus();

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.test18menu, menu);
        return true;

    }
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bluetooth_menu:
                openScanActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void openScanActivity(){
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }

    private void updateConnectionStatus(){
        if(dataKeeper.isConnected){
            status_tv.setText("Status: CONNECTED");
            address_tv.setText("Device address: " + dataKeeper.deviceAddress);
            name_tv.setText("Device Name: " + dataKeeper.deviceName);
        } else {
            status_tv.setText("Status: NOT CONNECTED");
            address_tv.setText("Device address: none" );
            name_tv.setText("Device Name: none");
        }
    }

    private static IntentFilter localIntentFilter(){
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("UPDATE_DATA");
        return intentFilter;
    }
    private void updateData(){
        lat_tv.setText(dataKeeper.Latitude);
        lon_tv.setText(dataKeeper.Longitude);
        ori_tv.setText(dataKeeper.orientation);
    }
}