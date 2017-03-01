package com.lady.viktoria.lightdrip.RealmActions;

import android.content.Context;
import android.util.Log;


import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.DateTypeAdapter;
import com.lady.viktoria.lightdrip.Models.BgReading;
import com.lady.viktoria.lightdrip.RealmConfig.PrimaryKeyFactory;
import com.lady.viktoria.lightdrip.RealmConfig.RealmBase;
import com.lady.viktoria.lightdrip.RealmModels.CalibrationData;
import com.lady.viktoria.lightdrip.RealmModels.GlucoseData;
import com.lady.viktoria.lightdrip.RealmModels.SensorData;
import com.lady.viktoria.lightdrip.RealmSerialize.GlucoseDataSerializer;
import com.lady.viktoria.lightdrip.RealmSerialize.SensorDataSerializer;

import java.util.Date;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;

import static io.realm.Realm.getInstance;

public class GlucoseRecord extends RealmBase {
    private final static String TAG = GlucoseRecord.class.getSimpleName();

    public static final double AGE_ADJUSTMENT_TIME = 86400000 * 1.9;
    public static final double AGE_ADJUSTMENT_FACTOR = .45;
    public double time_since_sensor_started;
    public double raw_data;
    public double filtered_data;
    public double age_adjusted_raw_value;
    public boolean calibration_flag;
    public double calculated_value;
    public double calculated_value_slope;
    public double a;
    public double b;
    public double c;
    public double ra;
    public double rb;
    public double rc;
    public String uuid;
    public String calibration_uuid;
    public String sensor_uuid;
    public boolean synced;
    public double raw_calculated;
    public boolean hide_slope;
    public String noise;

    Realm mRealm;
    Context context;
    private Gson gson;

    public GlucoseRecord() {
        Realm.init(context);
        mRealm = getInstance(getRealmConfig());
        try {
            PrimaryKeyFactory.getInstance().initialize(mRealm);
        } catch (Exception e) {
            Log.v(TAG, "CalibrationData() PrimaryKeyFactory " + e.getMessage());
        }
    }

    public GlucoseRecord create(double raw_data, double filtered_data, Long timestamp) {
        GlucoseRecord glucoseRecord = new GlucoseRecord();
        SensorRecord sensorRecord = new SensorRecord();
        if (!sensorRecord.isSensorActive()) {
            //Log.i("BG GSON: ", glucoseRecord.toS());
            return glucoseRecord;
        }

            Log.d(TAG, "create: No calibration yet");
        long newprimekey = PrimaryKeyFactory.getInstance().nextKey(GlucoseData.class);

        mRealm.beginTransaction();
        GlucoseData mGlucoseData = mRealm.createObject(GlucoseData.class, newprimekey);
        mGlucoseData.setSensor_id(sensorRecord.currentSensorID());
        mGlucoseData.setRawData(raw_data / 1000);
        mGlucoseData.setFilteredData(filtered_data / 1000);
        mGlucoseData.setTimestamp(timestamp);
        RealmResults<SensorData> results = mRealm.where(SensorData.class).findAll();
        long lastID = results.last().getid();
        SensorData mSensorData = mRealm.where(SensorData.class).equalTo("id", lastID).findFirst();
        long started_at = mSensorData.getstarted_at();
        mGlucoseData.setTimeSinceSensorStarted(timestamp - started_at);
        mGlucoseData.setsynced(false);
        mGlucoseData.setCalibrationFlag(false);
        mRealm.commitTransaction();
        mRealm.close();
        calculateAgeAdjustedRawValue();
        //bgReading.save();
        //bgReading.perform_calculations();

        try {
            serializeToJson();
            String json = gson.toJson(mRealm.where(GlucoseData.class).findAllSorted("id", Sort.DESCENDING).where().findFirst());
            Log.v(TAG, "glucoseRecord json: "  + json);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return glucoseRecord;
    }

    public void calculateAgeAdjustedRawValue() {
        RealmResults<GlucoseData> results = mRealm.where(GlucoseData.class).findAll();
        long lastID = results.last().getid();
        GlucoseData mGlucoseData = mRealm.where(GlucoseData.class).equalTo("id", lastID).findFirst();
        double TimeSinceSensorStarted = mGlucoseData.getTimeSinceSensorStarted();
        raw_data = mGlucoseData.getRawData();
        double adjust_for = AGE_ADJUSTMENT_TIME - TimeSinceSensorStarted;
        if (adjust_for > 0) {
            age_adjusted_raw_value = ((AGE_ADJUSTMENT_FACTOR * (adjust_for / AGE_ADJUSTMENT_TIME)) * raw_data) + raw_data;
            Log.i(TAG, "calculateAgeAdjustedRawValue: RAW VALUE ADJUSTMENT FROM:" + raw_data + " TO: " + age_adjusted_raw_value);
        } else {
            age_adjusted_raw_value = raw_data;
        }
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

                .registerTypeAdapter(GlucoseData.class, new GlucoseDataSerializer())
                .create();
    }
}