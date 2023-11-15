package com.example.test18;

import static com.example.test18.App.CHANNEL_ID;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;

public class ForegroundService extends Service {
   // public final static String ACTION_FS_RUNNING =
    private final static String TAG = "Foreground Service";
    private final DataKeeper dataKeeper = DataKeeper.getInstance();

    private LE_Service le_service;
    private Handler handler;
    private boolean isLeReady = false;
    private int pollingNumber = 0;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            le_service = ((LE_Service.LocalBinder) iBinder ).getService();
            if (!le_service.initialize()){
                Log.e(TAG, "Unable to initialize Bluetooth");
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            le_service.onDestroy();

        }
    };
    private final BroadcastReceiver localBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if ("ACTION_FS_RUNNING".equals(action)){

            } else if ("ACTION_FS_STOPPING".equals(action)){
                //dataKeeper.isChannelFree = false;
            } else if ("ACTION_GATT_DISCONNECTED".equals(action)){
                stopSelf();
            } else if ("ACTION_GATT_SERVICES_DISCOVERED".equals(action)){
                generateGattArrayList(le_service.getSupportedGattServices());
                //dataKeeper.isChannelFree = true;
            } else if ("ACTION_DATA_AVAILABLE".equals(action)){
                //Log.d(TAG, "HERE WE ARE");
                //dataKeeper.isChannelFree = true;
                dataKeeper.insert(intent.getStringExtra("CHARACTERISTIC"), intent.getStringExtra("VALUE"));
                Log.d(TAG, intent.getStringExtra("CHARACTERISTIC") + " " + intent.getStringExtra("VALUE"));
                broadcastUpdate("UPDATE_DATA");
            } else if ("READ".equals(action)){
               //dataKeeper.isChannelFree = false;
                le_service.readCharacteristic(dataKeeper.le_charateristics.get("000012a7-0000-1000-8000-00805f9b34fb"));
            } else if ("SUBSCRIBED".equals(action)){
                sequentialSubscribe();
            }

        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        dataKeeper.isFSRunning = true;
        broadcastUpdate("ACTION_FS_RUNNING");
        String selected_address = intent.getStringExtra("inputExtra");
        Intent notificationIntent = new Intent(this, ScanActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("test 18")
                .setContentText(selected_address)
                .setSmallIcon(R.drawable.baseline_sailing_24)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(2, notification);

        registerReceiver(localBroadcastReceiver, localIntentFilter());

        Intent gattServiceIntent = new Intent(this, LE_Service.class);
        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);

        handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                le_service.connect(selected_address);
            }
        };
        //handler.post(runnable);
        handler.postDelayed(runnable, 500);


        return START_NOT_STICKY;
    }
    @Override
    public void onDestroy(){
        le_service.disconnect();
        le_service.onDestroy();
        unbindService(serviceConnection);

        unregisterReceiver(localBroadcastReceiver);

        dataKeeper.isFSRunning = false;
        broadcastUpdate("ACTION_FS_STOPPING");
        super.onDestroy();
    }

    private void broadcastUpdate(final String action){
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private static IntentFilter localIntentFilter(){
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("ACTION_GATT_CONNECTED");
        intentFilter.addAction("ACTION_GATT_DISCONNECTED");
        intentFilter.addAction("ACTION_GATT_SERVICES_DISCOVERED");
        intentFilter.addAction("ACTION_DATA_AVAILABLE");
        intentFilter.addAction("READ");
        intentFilter.addAction("SUBSCRIBED");
        return intentFilter;
    }

    private void generateGattArrayList(List<BluetoothGattService> gattServices){
        String uuid = null;
        LE_GattAttributes le_gattAttributes = new LE_GattAttributes();
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            List<BluetoothGattCharacteristic> gattCharacteristics           = gattService.getCharacteristics();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {

                uuid = gattCharacteristic.getUuid().toString();

                dataKeeper.le_charateristics.put(uuid, gattCharacteristic);
                if (le_gattAttributes.isThere(uuid)) {
                    dataKeeper.le_charateristics_list.add(gattCharacteristic);
                }
                //le_service.setCharacteristicNotification(gattCharacteristic,true);
                Log.i(TAG, uuid);
                //Log.e(TAG, "Prop " + gattCharacteristic.PROPERTY_NOTIFY +" Perm " + gattCharacteristic.getPermissions()+" read" );
            }
        }
        Log.d(TAG, "Done that");
        //isLeReady = true;
        sequentialSubscribe();

    }




    private void sequentialSubscribe(){
        Log.d(TAG, "I'm called");
        if (dataKeeper.le_charateristics_list.size()>0){
            BluetoothGattCharacteristic subbing = dataKeeper.le_charateristics_list.get(0);
            le_service.setCharacteristicNotification(subbing, true);
            dataKeeper.le_charateristics_list.remove(0);
        } else {
            Log.d(TAG, "no more characteristics to sub");
            broadcastUpdate("DONE SUBSCRIBING");
        }
    }
}
