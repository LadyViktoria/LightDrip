package com.lady.viktoria.lightdrip.RealmActions;

import android.content.Context;
import android.util.Log;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lady.viktoria.lightdrip.RealmConfig.PrimaryKeyFactory;
import com.lady.viktoria.lightdrip.RealmConfig.RealmBase;
import com.lady.viktoria.lightdrip.RealmModels.SensorData;
import com.lady.viktoria.lightdrip.RealmSerialize.SensorDataSerializer;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

import static io.realm.Realm.getInstance;

public class SensorRecord extends RealmBase {
    private final static String TAG = SensorRecord.class.getSimpleName();

    private Realm mRealm;
    private Gson gson;
    Context context;
    public Context getcontext() {
        return context;
    }

    public SensorRecord() {
        Realm.init(context);
        mRealm = getInstance(getRealmConfig());
        try {
            PrimaryKeyFactory.getInstance().initialize(mRealm);
        } catch (Exception e) {
            Log.v(TAG, "onCreateView PrimaryKeyFactory " + e.getMessage());
        }
    }


    public void StartSensor(long startTime) {
        long newprimekey = PrimaryKeyFactory.getInstance().nextKey(SensorData.class);
        try {
            mRealm.beginTransaction();
            SensorData mSensorData = mRealm.createObject(SensorData.class, newprimekey);
            mSensorData.setstarted_at(startTime);
            mRealm.commitTransaction();
            mRealm.close();
        } catch (Exception e) {
            Log.v(TAG, "onTimeSet try_set_realm_obj " + e.getMessage());
        }
    }


    public void StopSensor() {
        try {
            long stopped_at = new Date().getTime();
            RealmResults<SensorData> results = mRealm.where(SensorData.class).findAll();
            long lastID = results.last().getid();
            SensorData mSensorData = mRealm.where(SensorData.class).equalTo("id", lastID).findFirst();
            mRealm.beginTransaction();
            mSensorData.setstopped_at(stopped_at);
            mRealm.commitTransaction();
            mRealm.close();
        } catch (Exception e) {
            Log.v(TAG, "stopSensor try_set_realm_obj " + e.getMessage());
        }
    }

    private void currentSensor() {
        if (isSensorActive()) {
            try {
                RealmResults<SensorData> results = mRealm.where(SensorData.class).findAll();
                long lastID = results.last().getid();
                SensorData mSensorData = mRealm.where(SensorData.class).equalTo("id", lastID).findFirst();
                //get realm object
                Log.v(TAG, "currentSensor realm object" + String.valueOf(mSensorData));
                // transform into json
                String Json = gson.toJson(mRealm.copyFromRealm(mSensorData));
                Log.v(TAG, "currentSensor json: "  + Json);
            } catch (Exception e) {
                Log.v(TAG, "currentSensor try_get_realm_obj " + e.getMessage());
            }
        } else {
            //Snackbar.make(getView(), "Please stop current Sensor fist!", Snackbar.LENGTH_LONG).show();
        }
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
            Log.v(TAG, "isSensorActive try_get_realm_obj " + e.getMessage());
        }
        return false;
    }

    private void serializeToJson() throws ClassNotFoundException {
        gson = new GsonBuilder()
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getDeclaringClass().equals(RealmObject.class);
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .registerTypeAdapter(Class.forName("com.lady.viktoria.lightdrip.RealmModels.SensorData"), new SensorDataSerializer())
                .create();
    }
}
