package com.lady.viktoria.lightdrip;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class GlucoseReadingRx {
    private final static String TAG = GlucoseReadingRx.class.getSimpleName();

    public int length;
    public int packagetype;
    public int filteredvalue;
    public int rawvalue;
    public int dexbattery;
    public int bridgebattery;
    public int bridgetxid;
    public String packetout;

    public GlucoseReadingRx(byte[] packet, int len) {

        length = packet[0];
        packagetype =  packet[1];
        if (length == 21 && packagetype == 0) {
            rawvalue =  packet[2];
            filteredvalue = packet[3];
            dexbattery = packet[4];
            bridgebattery = packet[11];
            bridgetxid = packet[12];

            packetout = Arrays.toString(packet);
        }
    }

    public String toString() {
        return "package data: " + packetout;
    }

}