package com.example.test18;

import android.bluetooth.BluetoothGattCharacteristic;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.function.BinaryOperator;

import kotlin.collections.AbstractList;

public class DataKeeper {

    private static DataKeeper instance = null;

    public volatile boolean isChannelFree = false;
    public volatile boolean isConnected = false;
    public volatile boolean isFSRunning = false;
    public volatile String deviceAddress;
    public volatile String deviceName;
    //various values

    public volatile String Latitude;
    public volatile String Longitude;
    public volatile String Speed;
    public volatile String Course;
    public volatile String HDOP;
    public volatile String hour;
    public volatile String minute;
    public volatile String second;
    public volatile String data;
    public volatile String acceleration;
    public volatile String gyroscope;
    public volatile String orientation;
    public volatile String magnetic;
    public volatile String temperature;

    public HashMap<String, BluetoothGattCharacteristic> le_charateristics= new HashMap<String,BluetoothGattCharacteristic>();
    public List<BluetoothGattCharacteristic> le_charateristics_list = new ArrayList<>();
    public Queue<String> le_charateristics_queue;
    public HashMap<BluetoothGattCharacteristic,byte[]> le_charateristics_values = new HashMap<BluetoothGattCharacteristic, byte[]>();
    public static DataKeeper getInstance(){
        if (instance==null){
            synchronized (DataKeeper.class){
                if (instance==null){
                    instance = new DataKeeper();
                }
            }
        }
        return instance;
    }

    private DataKeeper() {}


    public void insert(String charat, String value) {
        switch (charat){
            case "Latitude":
                Latitude = value;
                break;
            case "Longitude":
                Longitude = value;
                break;
            case "Speed":
                Speed = value;
                break;
            case "Course":
                Course = value;
                break;
            case "HDOP":
                 HDOP = value;
                break;
            case "time":
                second = value;
                break;
            case "data":
                data = value;
                break;
            case "Acceleration":
                acceleration = value;
                break;
            case "Gyroscope":
                gyroscope = value;
                break;
            case "Magnetic field":
                magnetic = value;
                break;
            case "Orientation":
                orientation = value;
                break;
            case "Temperature":
                temperature = value;
                break;
            default:
                break;
        }

    }
}
