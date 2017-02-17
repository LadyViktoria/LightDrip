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
        int DexSrc;
        int TransmitterID;
        String TxId;
        ByteBuffer tmpBuffer = ByteBuffer.allocate(len);
        tmpBuffer.order(ByteOrder.LITTLE_ENDIAN);
        tmpBuffer.put(packet, 0, len);
        length = packet[0];
        packagetype =  packet[1];
        if (length == 21 && packagetype == 0) {
            rawvalue =  packet[2];
            filteredvalue = packet[3];
            dexbattery = packet[4];
            bridgebattery = packet[11];
            bridgetxid = packet[12];
            DexSrc = tmpBuffer.getInt(12);
            TxId = "6GAX1";
            TransmitterID = convertSrc(TxId);
            if (Integer.compare(DexSrc, TransmitterID) != 0) {
                Log.v(TAG, "TXID wrong.  Expected " + TransmitterID + " but got " + DexSrc);
            } else {
                Log.v(TAG, "TXID from settings " + TransmitterID + " matches with " + DexSrc);
            }
            packetout = Arrays.toString(packet);
        }
    }

    public String toString() {
        return "package data: " + packetout;
    }

    private Integer convertSrc(final String Src) {
        Integer res = 0;
        String tmpSrc = Src.toUpperCase();
        res |= getSrcValue(tmpSrc.charAt(0)) << 20;
        res |= getSrcValue(tmpSrc.charAt(1)) << 15;
        res |= getSrcValue(tmpSrc.charAt(2)) << 10;
        res |= getSrcValue(tmpSrc.charAt(3)) << 5;
        res |= getSrcValue(tmpSrc.charAt(4));
        return res;
    }

    private int getSrcValue(char ch) {
        int i;
        char[] cTable = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'W', 'X', 'Y'};
        for (i = 0; i < cTable.length; i++) {
            if (cTable[i] == ch) break;
        }
        return i;
    }
}