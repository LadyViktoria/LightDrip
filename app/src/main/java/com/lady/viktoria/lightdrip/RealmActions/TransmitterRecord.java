package com.lady.viktoria.lightdrip.RealmActions;

import android.content.Context;
import android.util.Log;

import com.lady.viktoria.lightdrip.RealmConfig.PrimaryKeyFactory;
import com.lady.viktoria.lightdrip.RealmModels.TransmitterData;
import com.lady.viktoria.lightdrip.RealmConfig.RealmBase;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import io.realm.Realm;
import io.realm.RealmResults;

import static io.realm.Realm.getInstance;

public class TransmitterRecord extends RealmBase {
    private final static String TAG = TransmitterRecord.class.getSimpleName();

    private long timestamp;
    private double raw_data;
    private double filtered_data;
    private int transmitter_battery_level;
    private int bridge_battery_level;
    private Realm mRealm;
    Context context;

    private TransmitterRecord() {
        Realm.init(context);
        mRealm = getInstance(getRealmConfig());
        try {
            PrimaryKeyFactory.getInstance().initialize(mRealm);
        } catch (Exception e) {
            Log.v(TAG, "TransmitterDataRx() PrimaryKeyFactory " + e.getMessage());
        }
    }

    public static synchronized TransmitterRecord create(byte[] buffer, int len, Long timestamp) {
        if (buffer[0] < 6) {
            return null;
        }
        TransmitterRecord mTransmitterRecord = new TransmitterRecord();
        mTransmitterRecord.timestamp = timestamp;
        ByteBuffer txData = ByteBuffer.allocate(len);
        txData.order(ByteOrder.LITTLE_ENDIAN);
        txData.put(buffer, 0, len);
        mTransmitterRecord.raw_data = txData.getInt(2);
        mTransmitterRecord.filtered_data = txData.getInt(6);
        mTransmitterRecord.transmitter_battery_level = txData.get(10) & 0xff;
        mTransmitterRecord.bridge_battery_level = txData.get(11) & 0xff;
        //Stop allowing duplicate data, its bad!
        if (mTransmitterRecord.lastData("raw_data") == mTransmitterRecord
                .raw_data && Math.abs(mTransmitterRecord.lastData("timestamp") - timestamp) < (120000)) {
            return null;
        }
        mTransmitterRecord.writeTransmitterDataToRealm();
        return mTransmitterRecord;
    }

    private double lastData(String identifyer) {
        try {
            RealmResults<TransmitterData> results = mRealm.where(TransmitterData.class).findAll();
            if (identifyer.equals("raw_data")) {
                return results.last().getraw_data();
            } else if (identifyer.equals("timestamp")) {
                return (double) results.last().gettimestamp();
            }

        } catch (Exception e) {
            Log.v(TAG, "Error try_get_realm_obj " + e.getMessage());
        }
        return 0;
    }

    private void writeTransmitterDataToRealm() {
        long newprimekey = PrimaryKeyFactory.getInstance().nextKey(TransmitterData.class);
        mRealm.beginTransaction();
        TransmitterData mTransmitterData = mRealm.createObject(TransmitterData.class, newprimekey);
        mTransmitterData.settimestamp(timestamp);
        mTransmitterData.setraw_data(raw_data);
        mTransmitterData.setfiltered_data(filtered_data);
        mTransmitterData.settransmitter_battery_level(transmitter_battery_level);
        mTransmitterData.setbridge_battery_level(bridge_battery_level);
        mRealm.commitTransaction();
        GlucoseRecord gluciserecord = new GlucoseRecord();
        gluciserecord.create(raw_data,filtered_data,timestamp);
    }
}