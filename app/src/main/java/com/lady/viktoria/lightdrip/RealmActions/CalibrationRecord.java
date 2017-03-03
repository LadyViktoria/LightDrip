package com.lady.viktoria.lightdrip.RealmActions;

import android.content.Context;
import android.util.Log;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lady.viktoria.lightdrip.RealmConfig.PrimaryKeyFactory;
import com.lady.viktoria.lightdrip.RealmConfig.RealmBase;
import com.lady.viktoria.lightdrip.RealmModels.CalibrationData;
import com.lady.viktoria.lightdrip.RealmModels.GlucoseData;
import com.lady.viktoria.lightdrip.RealmModels.SensorData;
import com.lady.viktoria.lightdrip.RealmSerialize.CalibrationDataSerializer;
import com.lady.viktoria.lightdrip.RealmSerialize.GlucoseDataSerializer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;

import static io.realm.Realm.getInstance;

public class CalibrationRecord extends RealmBase {
    private final static String TAG = CalibrationRecord.class.getSimpleName();

    public static final double LOW_SLOPE_1 = 0.95;
    public static final double LOW_SLOPE_2 = 0.85;
    public static final double HIGH_SLOPE_1 = 1.3;
    public static final double HIGH_SLOPE_2 = 1.4;
    public static final double DEFAULT_LOW_SLOPE_LOW = 1.08;
    public static final double DEFAULT_LOW_SLOPE_HIGH = 1.15;
    public static final int DEFAULT_SLOPE = 1;
    public static final double DEFAULT_HIGH_SLOPE_HIGH = 1.3;
    public static final double DEFAUL_HIGH_SLOPE_LOW = 1.2;
    public static final double MMOLL_TO_MGDL = 18.0182;
    public static final double MGDL_TO_MMOLL = 1 / MMOLL_TO_MGDL;

    SensorRecord sensorRecord = new SensorRecord();
    CalibrationRecord higherCalibration = new CalibrationRecord();
    CalibrationRecord lowerCalibration = new CalibrationRecord();
    GlucoseRecord glucoseRecord = new GlucoseRecord();
    CalibrationRecord calibrationRecord = new CalibrationRecord();


    Realm mRealm;
    Context context;
    private Gson gson;

    public CalibrationRecord() {
        Realm.init(context);
        mRealm = getInstance(getRealmConfig());
        try {
            PrimaryKeyFactory.getInstance().initialize(mRealm);
        } catch (Exception e) {
            Log.v(TAG, "CalibrationData() PrimaryKeyFactory " + e.getMessage());
        }
    }

    public void initialCalibration(double bg1, double bg2) {
        long currentsensor_id = sensorRecord.currentSensorID();
        double bgReading1 = 0, bgReading2 = 0
                , ageAdjustedRawData1 = 0 ,ageAdjustedRawData2 = 0
                ,timestamp1 = 0, timestamp2 = 0;
        GlucoseData obj1 = null, obj2 = null;
        long started_at = 0;
        try {
            RealmResults<GlucoseData> results = mRealm.where(GlucoseData.class).findAll().sort("id", Sort.ASCENDING);
            int resultsize = results.size();
            bgReading1 = results.get(resultsize - 2).getRawData();
            bgReading2 = results.get(resultsize - 1).getRawData();
            ageAdjustedRawData1 = results.get(resultsize - 2).getageAdjustedRawValue();
            ageAdjustedRawData2 = results.get(resultsize - 1).getageAdjustedRawValue();
            timestamp1 = results.get(resultsize - 2).getTimestamp();
            timestamp2 = results.get(resultsize - 1).getTimestamp();
            obj1 = results.get(resultsize - 2);
            obj2 = results.get(resultsize - 1);

            RealmResults<SensorData> results_timestamp = mRealm.where(SensorData.class).findAll();
            long lastID = results_timestamp.last().getid();
            SensorData mSensorData = mRealm.where(SensorData.class)
                    .equalTo("id", lastID)
                    .findFirst();
            started_at = mSensorData.getstarted_at();

        } catch (Exception e) {
            Log.v(TAG, "Error try_get_realm_obj " + e.getMessage());
        }
        double highBgReading;
        double lowBgReading;
        double highAgeAdjusted;
        double lowAgeAdjusted;
        double highTimestamp;
        double lowTimestamp;
        GlucoseData highRealmObj;
        GlucoseData lowRealmObj;
        double higher_bg = Math.max(bg1, bg2);
        double lower_bg = Math.min(bg1, bg2);

        if (bgReading1 > bgReading2) {
            highBgReading = bgReading1;
            highAgeAdjusted = ageAdjustedRawData1;
            highTimestamp = timestamp1;
            highRealmObj = obj1;
            lowBgReading = bgReading2;
            lowAgeAdjusted = ageAdjustedRawData2;
            lowTimestamp = timestamp2;
            lowRealmObj = obj2;

        } else {
            highBgReading = bgReading2;
            highAgeAdjusted = ageAdjustedRawData2;
            highTimestamp = timestamp2;
            highRealmObj = obj2;
            lowBgReading = bgReading1;
            lowAgeAdjusted = ageAdjustedRawData1;
            lowTimestamp = timestamp1;
            lowRealmObj = obj2;
        }

        long newprimekeyHighCal = PrimaryKeyFactory.getInstance().nextKey(CalibrationData.class);
        mRealm.beginTransaction();
        CalibrationData mCalibrationDataHighCal = mRealm.createObject(CalibrationData.class, newprimekeyHighCal);
        mCalibrationDataHighCal.setbg(higher_bg);
        mCalibrationDataHighCal.setslope(1);
        mCalibrationDataHighCal.setintercept(higher_bg);
        mCalibrationDataHighCal.setsensor_id(currentsensor_id);
        mCalibrationDataHighCal.setestimate_raw_at_time_of_calibration(highAgeAdjusted);
        mCalibrationDataHighCal.setadjusted_raw_value(highAgeAdjusted);
        mCalibrationDataHighCal.setraw_value(highBgReading);
        mCalibrationDataHighCal.setraw_timestamp((long) highTimestamp);
        highRealmObj.setCalculatedValue(higher_bg);
        highRealmObj.setCalibrationFlag(true);
        //highRealmObj.setcalibration_type(higherCalibration);
        mCalibrationDataHighCal.settimestamp(new Date().getTime());
        mCalibrationDataHighCal.setslope_confidence(0.5);
        mCalibrationDataHighCal.setdistance_from_estimate(0);
        mCalibrationDataHighCal.setcheck_in(false);
        mCalibrationDataHighCal.setsensor_confidence(((-0.0018 * higher_bg * higher_bg) + (0.6657 * higher_bg) + 36.7505) / 100);
        mCalibrationDataHighCal.setsensor_age_at_time_of_estimation(Double.valueOf(mCalibrationDataHighCal.gettimestamp() - started_at));
        //calculate_w_l_s();
        mRealm.commitTransaction();
        mRealm.close();

        long newprimekeyLowCal = PrimaryKeyFactory.getInstance().nextKey(CalibrationData.class);
        mRealm.beginTransaction();
        CalibrationData mCalibrationDataLowCal = mRealm.createObject(CalibrationData.class, newprimekeyLowCal);
        mCalibrationDataLowCal.setbg(lower_bg);
        mCalibrationDataLowCal.setslope(1);
        mCalibrationDataLowCal.setintercept(lower_bg);
        mCalibrationDataLowCal.setsensor_id(currentsensor_id);
        mCalibrationDataLowCal.setestimate_raw_at_time_of_calibration(lowAgeAdjusted);
        mCalibrationDataLowCal.setadjusted_raw_value(lowAgeAdjusted);
        mCalibrationDataLowCal.setraw_value(lowBgReading);
        mCalibrationDataLowCal.setraw_timestamp((long) lowTimestamp);
        lowRealmObj.setCalculatedValue(lower_bg);
        lowRealmObj.setCalibrationFlag(true);
        //lowRealmObj.setcalibration_type(lowerCalibration);
        mCalibrationDataLowCal.settimestamp(new Date().getTime());
        mCalibrationDataLowCal.setslope_confidence(0.5);
        mCalibrationDataLowCal.setdistance_from_estimate(0);
        mCalibrationDataLowCal.setcheck_in(false);
        mCalibrationDataLowCal.setsensor_confidence(((-0.0018 * lower_bg * lower_bg) + (0.6657 * lower_bg) + 36.7505) / 100);
        mCalibrationDataLowCal.setsensor_age_at_time_of_estimation(Double.valueOf(mCalibrationDataLowCal.gettimestamp() - started_at));
        //calculate_w_l_s();
        mRealm.commitTransaction();
        mRealm.close();


        serializeToJson();
        RealmResults<CalibrationData> results = mRealm.where(CalibrationData.class).findAll().sort("id", Sort.ASCENDING);
        int resultsize = results.size();
        CalibrationData result1 = results.get(resultsize - 2);
        CalibrationData result2 = results.get(resultsize - 1);

        String json1 = gson.toJson(mRealm.copyFromRealm(result1));
        String json2 = gson.toJson(mRealm.copyFromRealm(result2));

        Log.v(TAG, "glucoseRecord json1: "  + json1);
        Log.v(TAG, "glucoseRecord json2: "  + json2);


    }

    public void singleCalibration(double bg) {
        boolean currentsensor = sensorRecord.isSensorActive();
        long currentsensor_id = sensorRecord.currentSensorID();
        if (currentsensor && glucoseRecord.lastGluscoseEntry() != null) {
            long newprimekeyLowCal = PrimaryKeyFactory.getInstance().nextKey(CalibrationData.class);
            mRealm.beginTransaction();
            CalibrationData mCalibrationData = mRealm.createObject(CalibrationData.class, newprimekeyLowCal);
            mCalibrationData.setbg(bg);
            mCalibrationData.setcheck_in(false);
            mCalibrationData.settimestamp(new Date().getTime());
            //mCalibrationDataLowCal.setestimate_raw_at_time_of_calibration();
            //mCalibrationDataLowCal.setadjusted_raw_value();
            mCalibrationData.setsensor_id(currentsensor_id);


            //mCalibrationDataLowCal.setraw_value();
            //mCalibrationDataLowCal.setraw_timestamp();
            mRealm.commitTransaction();
            mRealm.close();

        } else {
        Log.d("CALIBRATION", "No sensor, cant save!");
        }
        //return lastCalibration();
    }


    public CalibrationData lastCalibration() {
        CalibrationData calibrationData = mRealm.where(CalibrationData.class)
                .findAllSorted("id", Sort.DESCENDING)
                .where()
                .findFirst();
        return calibrationData;
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
                .registerTypeAdapter(CalibrationData.class, new CalibrationDataSerializer())
                .create();
    }

    /*
    private static void calculate_w_l_s() {
        SensorRecord sensorRecord = new SensorRecord();
        if (sensorRecord.isSensorActive()) {
            double l = 0;
            double m = 0;
            double n = 0;
            double p = 0;
            double q = 0;
            double w;
            List<Calibration> calibrations = allForSensorInLastFourDays(); //5 days was a bit much, dropped this to 4
            if (calibrations.size() <= 1) {
                Calibration calibration = Calibration.last();
                calibration.slope = 1;
                calibration.intercept = calibration.bg - (calibration.raw_value * calibration.slope);
                //calibration.save();
                //CalibrationRequest.createOffset(calibration.bg, 25);
            } else {
                for (Calibration calibration : calibrations) {
                    w = calibration.calculateWeight();
                    l += (w);
                    m += (w * calibration.estimate_raw_at_time_of_calibration);
                    n += (w * calibration.estimate_raw_at_time_of_calibration * calibration.estimate_raw_at_time_of_calibration);
                    p += (w * calibration.bg);
                    q += (w * calibration.estimate_raw_at_time_of_calibration * calibration.bg);
                }

                Calibration last_calibration = Calibration.last();
                w = (last_calibration.calculateWeight() * (calibrations.size() * 0.14));
                l += (w);
                m += (w * last_calibration.estimate_raw_at_time_of_calibration);
                n += (w * last_calibration.estimate_raw_at_time_of_calibration * last_calibration.estimate_raw_at_time_of_calibration);
                p += (w * last_calibration.bg);
                q += (w * last_calibration.estimate_raw_at_time_of_calibration * last_calibration.bg);

                double d = (l * n) - (m * m);
                Calibration calibration = Calibration.last();
                calibration.intercept = ((n * p) - (m * q)) / d;
                calibration.slope = ((l * q) - (m * p)) / d;
                if ((calibrations.size() == 2 && calibration.slope < LOW_SLOPE_1) || (calibration.slope < LOW_SLOPE_2)) { // I have not seen a case where a value below 7.5 proved to be accurate but we should keep an eye on this
                    calibration.slope = calibration.slopeOOBHandler(0);
                    if (calibrations.size() > 2) {
                        calibration.possible_bad = true;
                    }
                    calibration.intercept = calibration.bg - (calibration.estimate_raw_at_time_of_calibration * calibration.slope);
                    //  CalibrationRequest.createOffset(calibration.bg, 25);
                }
                if ((calibrations.size() == 2 && calibration.slope > HIGH_SLOPE_1) || (calibration.slope > HIGH_SLOPE_2)) {
                    calibration.slope = calibration.slopeOOBHandler(1);
                    if (calibrations.size() > 2) {
                        calibration.possible_bad = true;
                    }
                    calibration.intercept = calibration.bg - (calibration.estimate_raw_at_time_of_calibration * calibration.slope);
                    //CalibrationRequest.createOffset(calibration.bg, 25);
                }
                Log.d(TAG, "Calculated Calibration Slope: " + calibration.slope);
                Log.d(TAG, "Calculated Calibration intercept: " + calibration.intercept);
                //calibration.save();
            }
        } else {
            Log.d(TAG, "NO Current active sensor found!!");
        }
    }

    private double slopeOOBHandler(int status) {
        // If the last slope was reasonable and reasonably close, use that, otherwise use a slope that may be a little steep, but its best to play it safe when uncertain
        List<Calibration> calibrations = Calibration.latest(3);
        Calibration thisCalibration = calibrations.get(0);
        if (status == 0) {
            if (calibrations.size() == 3) {
                if ((Math.abs(thisCalibration.bg - thisCalibration.estimate_bg_at_time_of_calibration) < 30) && (calibrations.get(1).possible_bad != null && calibrations.get(1).possible_bad == true)) {
                    return calibrations.get(1).slope;
                } else {
                    return Math.max(((-0.048) * (thisCalibration.sensor_age_at_time_of_estimation / (60000 * 60 * 24))) + 1.1, DEFAULT_LOW_SLOPE_LOW);
                }
            } else if (calibrations.size() == 2) {
                return Math.max(((-0.048) * (thisCalibration.sensor_age_at_time_of_estimation / (60000 * 60 * 24))) + 1.1, DEFAULT_LOW_SLOPE_HIGH);
            }
            return DEFAULT_SLOPE;
        } else {
            if (calibrations.size() == 3) {
                if ((Math.abs(thisCalibration.bg - thisCalibration.estimate_bg_at_time_of_calibration) < 30) && (calibrations.get(1).possible_bad != null && calibrations.get(1).possible_bad == true)) {
                    return calibrations.get(1).slope;
                } else {
                    return DEFAULT_HIGH_SLOPE_HIGH;
                }
            } else if (calibrations.size() == 2) {
                return DEFAUL_HIGH_SLOPE_LOW;
            }
        }
        return DEFAULT_SLOPE;
    }
    */
}
