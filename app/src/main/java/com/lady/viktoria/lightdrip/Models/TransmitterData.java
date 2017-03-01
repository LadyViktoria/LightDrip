package com.lady.viktoria.lightdrip.Models;

import android.provider.BaseColumns;
import android.util.Log;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.UUID;

/**
 * Created by stephenblack on 11/6/14.
 */

public class TransmitterData {
    private final static String TAG = TransmitterData.class.getSimpleName();

    public long timestamp;

    public double raw_data;

    public double filtered_data;

    public int sensor_battery_level;

    public String uuid;

    public static synchronized TransmitterData create(byte[] buffer, int len, Long timestamp) {
        if (len < 6) {
            return null;
        }
        TransmitterData transmitterData = new TransmitterData();
        if (buffer[0] == 0x11 && buffer[1] == 0x00) {
            //this is a dexbridge packet.  Process accordingly.
            Log.i(TAG, "create Processing a Dexbridge packet");
            ByteBuffer txData = ByteBuffer.allocate(len);
            txData.order(ByteOrder.LITTLE_ENDIAN);
            txData.put(buffer, 0, len);
            transmitterData.raw_data = txData.getInt(2);
            transmitterData.filtered_data = txData.getInt(6);
            //  bitwise and with 0xff (1111....1) to avoid that the byte is treated as signed.
            transmitterData.sensor_battery_level = txData.get(10) & 0xff;
            Log.i(TAG, "Created transmitterData record with Raw value of " + transmitterData.raw_data + " and Filtered value of " + transmitterData.filtered_data + " at " + transmitterData.timestamp);
        } else { //this is NOT a dexbridge packet.  Process accordingly.
            Log.i(TAG, "create Processing a BTWixel or IPWixel packet");
            StringBuilder data_string = new StringBuilder();
            for (int i = 0; i < len; ++i) {
                data_string.append((char) buffer[i]);
            }
            String[] data = data_string.toString().split("\\s+");

            if (data.length > 1) {
                transmitterData.sensor_battery_level = Integer.parseInt(data[1]);
            }
            transmitterData.raw_data = Integer.parseInt(data[0]);
            transmitterData.filtered_data = Integer.parseInt(data[0]);
        }
        //Stop allowing duplicate data, its bad!
        TransmitterData lastTransmitterData = TransmitterData.last();
        if (lastTransmitterData != null && lastTransmitterData.raw_data == transmitterData.raw_data && Math.abs(lastTransmitterData.timestamp - timestamp) < (120000)) {
            return null;
        }

        transmitterData.timestamp = timestamp;
        transmitterData.uuid = UUID.randomUUID().toString();
        //transmitterData.save();
        return transmitterData;
    }

    public static TransmitterData last() {
        //return new Select()
          //      .from(TransmitterData.class)
            //    .orderBy("_ID desc")
              //  .executeSingle();
        return null;
    }

}
