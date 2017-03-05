package com.lady.viktoria.lightdrip.RealmActions;

import android.content.Context;
import android.util.Log;


import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.DateTypeAdapter;
import com.lady.viktoria.lightdrip.RealmConfig.PrimaryKeyFactory;
import com.lady.viktoria.lightdrip.RealmModels.GlucoseData;
import com.lady.viktoria.lightdrip.RealmModels.SensorData;
import com.lady.viktoria.lightdrip.RealmSerialize.GlucoseDataSerializer;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;

import static io.realm.Realm.getDefaultInstance;
import static io.realm.Realm.getInstance;

public class GlucoseRecord {
    private final static String TAG = GlucoseRecord.class.getSimpleName();

    private static final double AGE_ADJUSTMENT_TIME = 86400000 * 1.9;
    private static final double AGE_ADJUSTMENT_FACTOR = .45;
    private double time_since_sensor_started;
    private double filtered_data;
    private boolean calibration_flag;
    private double calculated_value;
    private double a;
    private double b;
    private double c;
    private double ra;
    private double rb;
    private double rc;
    private String uuid;
    private String calibration_uuid;
    private String sensor_uuid;
    private boolean synced;
    private double raw_calculated;
    private boolean hide_slope;
    private String noise;
    private long timestamp;
    private Realm mRealm;
    private Gson gson;
    Context context;

    public GlucoseRecord() {
        Realm.init(context);
        mRealm = getDefaultInstance();
    }

    public void create(double raw_data, double filtered_data, Long timestamp) {
        SensorRecord sensorRecord = new SensorRecord();
        if (!sensorRecord.isSensorActive()) {
            Log.i("BG GSON: ", toS());
            return;
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
        SensorData mSensorData = mRealm.where(SensorData.class)
                .equalTo("id", lastID)
                .findFirst();
        long started_at = mSensorData.getstarted_at();
        mGlucoseData.setTimeSinceSensorStarted(timestamp - started_at);
        mGlucoseData.setsynced(false);
        mGlucoseData.setCalibrationFlag(false);
        mRealm.commitTransaction();
        mRealm.close();

        calculateAgeAdjustedRawValue();
        //glucoseRecord.perform_calculations();

        serializeToJson();
        String json = gson.toJson(mRealm.copyFromRealm(mRealm
                .where(GlucoseData.class)
                .findAllSorted("id", Sort.DESCENDING)
                .where()
                .findFirst()));
        Log.v(TAG, "glucoseRecord json: "  + json);
    }

    private void calculateAgeAdjustedRawValue() {
        RealmResults<GlucoseData> results = mRealm.where(GlucoseData.class).findAll();
        long lastID = results.last().getid();
        GlucoseData mGlucoseData = mRealm.where(GlucoseData.class).equalTo("id", lastID).findFirst();
        double TimeSinceSensorStarted = mGlucoseData.getTimeSinceSensorStarted();
        double raw_data = mGlucoseData.getRawData();
        double adjust_for = AGE_ADJUSTMENT_TIME - TimeSinceSensorStarted;
        double age_adjusted_raw_value;
        if (adjust_for > 0) {
            age_adjusted_raw_value = ((AGE_ADJUSTMENT_FACTOR * (adjust_for / AGE_ADJUSTMENT_TIME)) * raw_data) + raw_data;
            Log.i(TAG, "calculateAgeAdjustedRawValue: RAW VALUE ADJUSTMENT FROM:" + raw_data + " TO: " + age_adjusted_raw_value);
            mRealm.beginTransaction();
            lastGluscoseEntry().setageAdjustedRawValue(age_adjusted_raw_value);
            mRealm.commitTransaction();
            mRealm.close();
        } else {
            age_adjusted_raw_value = raw_data;
            mRealm.beginTransaction();
            lastGluscoseEntry().setageAdjustedRawValue(age_adjusted_raw_value);
            mRealm.commitTransaction();
            mRealm.close();
        }
    }

    private void serializeToJson() {
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

    private String toS() {
        gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(Date.class, new DateTypeAdapter())
                .serializeSpecialFloatingPointValues()
                .create();
        return gson.toJson(this);
    }

    public int countRecordsByLastSensorID() {
        try {
            RealmResults<SensorData> results = mRealm.where(SensorData.class).equalTo("stopped_at", 0L).findAll();
            long lastID = results.last().getid();
            RealmResults<GlucoseData> glucoseRecord = mRealm.where(GlucoseData.class)
                    .equalTo("sensor_id", lastID)
                    .findAll();
            return glucoseRecord.size();
        } catch (Exception e) {
            Log.v(TAG, "countRecordsByLastSensorID " + e.getMessage());
        }
        return 0;
    }

    GlucoseData lastGluscoseEntry() {
        return mRealm.where(GlucoseData.class)
                .findAllSorted("id", Sort.DESCENDING)
                .where()
                .findFirst();
    }

    //*******INSTANCE METHODS***********//
    public void perform_calculations() {
      //  find_new_curve();
     //   find_new_raw_curve();
        find_slope();
    }

    private void find_slope() {
        RealmResults<GlucoseData> results = mRealm.where(GlucoseData.class).findAll().sort("id", Sort.DESCENDING);
        int resultsize = results.size();
        double lastGlucose = results.get(resultsize - 2).getRawData();
        double currentGlucose = results.get(resultsize - 1).getRawData();

        //assert last_2.get(0) == this : "Invariant condition not fulfilled: calculating slope and current reading wasn't saved before";

        double calculated_value_slope;
        if (lastGlucose != 0 && currentGlucose != 0) {
            calculated_value_slope = calculateSlope(currentGlucose, lastGlucose);
            //save();
        } else if (currentGlucose != 0) {
            calculated_value_slope = 0;
            //save();
        } else {
            Log.w(TAG, "NO BG? COULDNT FIND SLOPE!");
        }
    }

    private static double calculateSlope(double current, double last) {
        /*
        if (current.timestamp == last.timestamp || current.calculated_value == last.calculated_value) {
            return 0;
        } else {
            return (last.calculated_value - current.calculated_value) / (last.timestamp - current.timestamp);
        }
        */
        return current;
    }

    public static double weightedAverageRaw(double timeA, double timeB, double calibrationTime, double rawA, double rawB) {
        double relativeSlope = (rawB - rawA) / (timeB - timeA);
        double relativeIntercept = rawA - (relativeSlope * timeA);
        return ((relativeSlope * calibrationTime) + relativeIntercept);
    }


}