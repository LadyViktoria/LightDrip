package com.lady.viktoria.lightdrip.RealmActions;

import android.content.Context;
import android.util.Log;

import com.lady.viktoria.lightdrip.RealmConfig.PrimaryKeyFactory;
import com.lady.viktoria.lightdrip.RealmModels.CalibrationData;
import com.lady.viktoria.lightdrip.RealmModels.SensorData;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import static io.realm.Realm.getDefaultInstance;
import static io.realm.Realm.getInstance;

public class SensorRecord {
    private final static String TAG = SensorRecord.class.getSimpleName();

    private Realm mRealm;
    Context context;

    public SensorRecord() {
        Realm.init(context);
        mRealm = getDefaultInstance();
    }

    public void StartSensor(long startTime) {
        long newprimekey = PrimaryKeyFactory.getInstance().nextKey(SensorData.class);
        try {
            RealmResults<SensorData> results = mRealm.where(SensorData.class).findAll();
            int sensorTableSize = results.size();
            mRealm.beginTransaction();
            if (sensorTableSize >= 10) {
                mRealm.where(SensorData.class).findAllSorted("id", Sort.ASCENDING)
                        .where()
                        .findFirst()
                        .deleteFromRealm();
            }
            SensorData mSensorData = mRealm.createObject(SensorData.class, newprimekey);
            mSensorData.setstarted_at(startTime);
            mRealm.commitTransaction();
            mRealm.close();
        } catch (Exception e) {
            Log.v(TAG, "StartSensor " + e.getMessage());
        }
    }

    public void StopSensor() {
        try {
            long stopped_at = new Date().getTime();
            RealmResults<SensorData> results = mRealm.where(SensorData.class).findAll();
            long lastID = results.last().getid();
            SensorData mSensorData = mRealm.where(SensorData.class).equalTo("id", lastID).findFirst();
            RealmResults<CalibrationData> CalibrationDataToDrop = mRealm.where(CalibrationData.class).findAll();
            mRealm.beginTransaction();
            mSensorData.setstopped_at(stopped_at);
            CalibrationDataToDrop.deleteAllFromRealm();
            mRealm.commitTransaction();
            mRealm.close();
        } catch (Exception e) {
            Log.v(TAG, "stopSensor " + e.getMessage());
        }
    }

    public long currentSensorID() {
        if (isSensorActive()) {
            try {
                RealmResults<SensorData> results = mRealm.where(SensorData.class).findAll();
                long lastID = results.last().getid();
                long mSensorData = mRealm.where(SensorData.class).equalTo("id", lastID).findFirst().getid();
                return mSensorData;
            } catch (Exception e) {
                Log.v(TAG, "currentSensor " + e.getMessage());
            }
        } else {
            return 0L;
        }
        return 0L;
    }

    public boolean isSensorActive() {
        try {
            RealmResults<SensorData> results = mRealm.where(SensorData.class).findAll();
            long lastID = results.last().getid();
            SensorData mSensorData = mRealm.where(SensorData.class).equalTo("id", lastID).findFirst();
            if (mSensorData.getstopped_at() == 0L) {
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.v(TAG, "isSensorActive " + e.getMessage());
        }
        return false;
    }
}