package com.lady.viktoria.lightdrip;

import com.lady.viktoria.lightdrip.DatabaseModels.TransmitterData;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmResults;

public class TransmitterDataRx {
    private final static String TAG = TransmitterDataRx.class.getSimpleName();

    private long timestamp;
    private double raw_data;
    private double filtered_data;
    private int sensor_battery_level;
    public String uuid;
    Realm mRealm;

    private TransmitterDataRx() {}

    public static synchronized TransmitterDataRx create(byte[] buffer, int len, Long timestamp) {
        if (buffer[0] < 6) {
            return null;
        }
        TransmitterDataRx mTransmitterDataRx = new TransmitterDataRx();
        mTransmitterDataRx.timestamp = timestamp;
        mTransmitterDataRx.uuid = UUID.randomUUID().toString();
        ByteBuffer txData = ByteBuffer.allocate(len);
        txData.order(ByteOrder.LITTLE_ENDIAN);
        txData.put(buffer, 0, len);
        mTransmitterDataRx.raw_data = txData.getInt(2);
        mTransmitterDataRx.filtered_data = txData.getInt(6);
        mTransmitterDataRx.sensor_battery_level = txData.get(10) & 0xff;
        //Stop allowing duplicate data, its bad!
        if (mTransmitterDataRx.lastData("raw_data") == mTransmitterDataRx
                .raw_data && Math.abs(mTransmitterDataRx.lastData("timestamp") - timestamp) < (120000)) {
            return null;
        } else {
            mTransmitterDataRx.writeTransmitterDataToRealm();
        }
        return mTransmitterDataRx;
    }

    private double lastData(String identifyer) {
        mRealm = Realm.getDefaultInstance();
        RealmResults<TransmitterData> results = mRealm.where(TransmitterData.class).findAll();
        if (identifyer.equals("raw_data")) {
            return results.last().getraw_data();
        } else if (identifyer.equals("timestamp")) {
            return (double) results.last().gettimestamp();
        }
        mRealm.close();
        return 0;
    }

    private void writeTransmitterDataToRealm() {
        mRealm = Realm.getDefaultInstance();
        mRealm.beginTransaction();
        TransmitterData mTransmitterData = mRealm.createObject(TransmitterData.class);
        mTransmitterData.settimestamp(timestamp);
        mTransmitterData.setraw_data(raw_data);
        mTransmitterData.setfiltered_data(filtered_data);
        mTransmitterData.setsensor_battery_level(sensor_battery_level);
        mTransmitterData.setuuid(uuid);
        mRealm.commitTransaction();
        mRealm.close();
    }
}