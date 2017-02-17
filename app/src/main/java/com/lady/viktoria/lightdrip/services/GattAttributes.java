package com.lady.viktoria.lightdrip.services;

import java.util.HashMap;

public class GattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    // Client Characteristic
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    // Glucose Service
    public static String HM_10_SERVICE = "0000ffe0-0000-1000-8000-00805f9b34fb";
    // Glucose Measurement Characteristic
    public static String HM_RX_TX = "0000ffe1-0000-1000-8000-00805f9b34fb";

    static {
        // Services.
        attributes.put(HM_10_SERVICE, "HM10 Service");
        // Characteristics.
        attributes.put(HM_RX_TX, "BG Measurement");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}