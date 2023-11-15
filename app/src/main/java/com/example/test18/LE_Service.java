package com.example.test18;

import static android.nfc.NfcAdapter.EXTRA_DATA;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.UUID;

@SuppressLint("MissingPermission")
public class LE_Service extends Service {
    private final static String TAG = "LE_Service";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public DataKeeper dataKeeper = DataKeeper.getInstance();

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private String bluetoothDeviceAddress;
    private BluetoothGatt bluetoothGatt;
    private int connectionState = STATE_DISCONNECTED;
    private LE_GattAttributes le_gattAttributes = new LE_GattAttributes();
    private final IBinder localBinder = new LE_Service.LocalBinder();

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED){
                dataKeeper.isConnected = true;
                connectionState = STATE_CONNECTED;

                broadcastUpdate("ACTION_GATT_CONNECTED");
                Log.i(TAG, "Attempting to start service discovery:" + bluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                dataKeeper.isConnected = false;
                connectionState = STATE_DISCONNECTED;

                broadcastUpdate("ACTION_GATT_DISCONNECTED");

            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate("ACTION_GATT_SERVICES_DISCOVERED");
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            broadcastUpdate("ACTION_DATA_AVAILABLE", characteristic);
            //broadcastUpdate("ACTION_DATA_AVAILABLE");
            //dataKeeper.le_charateristics_values.put(characteristic, value);

            //Log.d(TAG, String.valueOf(value));

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate("ACTION_DATA_AVAILABLE", characteristic);
            //Log.d(TAG, "something "+ le_gattAttributes.lookup(String.valueOf(characteristic.getUuid())));
        }
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status){
            broadcastUpdate("SUBSCRIBED");
            //Log.d(TAG, "GO ON");
        }

    };


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }
    public class LocalBinder extends Binder {
        LE_Service getService() {
            return LE_Service.this;
        }
    }
    public void close() {
        if (bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.close();
        bluetoothGatt = null;
    }

    public boolean initialize() {
        if (bluetoothManager == null) {
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

    public boolean connect(final String address) {
        Log.d(TAG, "Connect called");
        if (bluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        bluetoothDeviceAddress = address;
        connectionState = STATE_CONNECTING;
        return true;
    }

    public void disconnect() {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        bluetoothGatt.disconnect();
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic){
        //Log.d(TAG, "oh yeah");
        final Intent intent = new Intent(action);
        intent.putExtra("CHARACTERISTIC", le_gattAttributes.lookup(characteristic.getUuid().toString()));
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0){
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for(byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));
            intent.putExtra("VALUE", new String(data) );
            //Log.d(TAG, String.valueOf((stringBuilder)));
        }
        sendBroadcast(intent);
    }



    public List<BluetoothGattService> getSupportedGattServices() {
        if (bluetoothGatt == null) return null;

        return bluetoothGatt.getServices();
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {

        //setCharacteristicNotification(characteristic, false);

        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        bluetoothGatt.readCharacteristic(characteristic);
        //setCharacteristicNotification(characteristic, true);
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled){
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        String uuid = String.valueOf(characteristic.getUuid());
        Log.w(TAG, "doing " + uuid);
        if (uuid != null && le_gattAttributes.isThere(uuid)){
            boolean set = bluetoothGatt.setCharacteristicNotification(characteristic, enabled);
            Log.d(TAG, String.valueOf(set));
            String name = le_gattAttributes.lookup(String.valueOf(characteristic.getUuid()));

            if (name != null){
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
                Log.e(TAG, "UUID " + characteristic.getUuid() +" DESCR " + descriptor +" added" );
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                bluetoothGatt.writeDescriptor(descriptor);
                Log.e(TAG, "UUID " + characteristic.getUuid() +" DESCR " + descriptor +" added" );
            }
        } else{
            //skip
        }
        Log.w(TAG, "DONE");
    }
}
