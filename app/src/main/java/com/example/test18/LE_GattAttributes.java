package com.example.test18;

import java.util.HashMap;

public class LE_GattAttributes {
    private static HashMap<String, String> attributes = new HashMap<>();

    static {
        attributes.put("000012a1-0000-1000-8000-00805f9b34fb", "GPS Data");
        attributes.put("000012a2-0000-1000-8000-00805f9b34fb", "Latitude");
        attributes.put("000012a3-0000-1000-8000-00805f9b34fb", "Longitude");
        attributes.put("000012a4-0000-1000-8000-00805f9b34fb", "Speed");
        attributes.put("000012a5-0000-1000-8000-00805f9b34fb", "Course");
        attributes.put("000012a6-0000-1000-8000-00805f9b34fb", "HDOP");
        attributes.put("000012a7-0000-1000-8000-00805f9b34fb", "time");
        attributes.put("000012a8-0000-1000-8000-00805f9b34fb", "data");

        attributes.put("000012b1-0000-1000-8000-00805f9b34fb", "IMU Data");
        attributes.put("000012b2-0000-1000-8000-00805f9b34fb", "Acceleration");
        attributes.put("000012b3-0000-1000-8000-00805f9b34fb", "Orientation");
        attributes.put("000012b4-0000-1000-8000-00805f9b34fb", "Magnetic field");
        attributes.put("000012b5-0000-1000-8000-00805f9b34fb", "Gyroscope");
        attributes.put("000012b6-0000-1000-8000-00805f9b34fb", "Temperature");

        attributes.put("000012c1-0000-1000-8000-00805f9b34fb", "WIND Data");
        attributes.put("000012c2-0000-1000-8000-00805f9b34fb", "direction/speed");
    }
    public String lookup(String uuid) {
        if (attributes.containsKey(uuid)){
            String name = attributes.get(uuid);
            return name;
        }
        else {
            return null;
        }
    }
    public boolean isThere(String uuid){
        if (attributes.containsKey(uuid)){
            return true;
        }
        else {
            return false;
        }
    }
}
