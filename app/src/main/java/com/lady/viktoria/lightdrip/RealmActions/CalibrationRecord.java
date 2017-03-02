package com.lady.viktoria.lightdrip.RealmActions;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.lady.viktoria.lightdrip.Models.Sensor;
import com.lady.viktoria.lightdrip.RealmConfig.PrimaryKeyFactory;
import com.lady.viktoria.lightdrip.RealmConfig.RealmBase;
import com.lady.viktoria.lightdrip.RealmModels.CalibrationData;
import com.lady.viktoria.lightdrip.RealmModels.SensorData;
import com.lady.viktoria.lightdrip.RealmModels.TransmitterData;

import java.util.Date;
import java.util.List;

import io.realm.Realm;
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

    Realm mRealm;
    Context context;
    SensorRecord sensorRecord = new SensorRecord();

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
        CalibrationRecord higherCalibration = new CalibrationRecord();
        CalibrationRecord lowerCalibration = new CalibrationRecord();
        double bgReading1 = 0;
        double bgReading2 = 0;
        try {
            RealmResults<TransmitterData> results = mRealm.where(TransmitterData.class).findAll().sort("id", Sort.DESCENDING);
            int resultsize = results.size();
            bgReading1 = results.get(resultsize - 2).getraw_data();
            bgReading2 = results.get(resultsize - 1).getraw_data();
            Log.v(TAG, "bgReading1 " + bgReading1);
            Log.v(TAG, "bgReading2 " + bgReading2);
        } catch (Exception e) {
            Log.v(TAG, "Error try_get_realm_obj " + e.getMessage());
        }
        double highBgReading;
        double lowBgReading;
        double higher_bg = Math.max(bg1, bg2)*1000;
        double lower_bg = Math.min(bg1, bg2)*1000;

        if (bgReading1 > bgReading2) {
            highBgReading = bgReading1;
            lowBgReading = bgReading2;
        } else {
            highBgReading = bgReading2;
            lowBgReading = bgReading1;
        }


        long newprimekeyLowCal = PrimaryKeyFactory.getInstance().nextKey(CalibrationData.class);
        mRealm.beginTransaction();
        CalibrationData mCalibrationDataLowCal = mRealm.createObject(CalibrationData.class, newprimekeyLowCal);
        mCalibrationDataLowCal.setbg(lower_bg);
        mCalibrationDataLowCal.setslope(1);
        mCalibrationDataLowCal.setintercept(lower_bg);
        mCalibrationDataLowCal.setsensor_id(currentsensor_id);
        //mCalibrationDataLowCal.setestimate_raw_at_time_of_calibration();
        //mCalibrationDataLowCal.setadjusted_raw_value();
        //mCalibrationDataLowCal.setraw_value();
        //mCalibrationDataLowCal.setraw_timestamp();
        mRealm.commitTransaction();
        mRealm.close();

        long newprimekeyHighCal = PrimaryKeyFactory.getInstance().nextKey(CalibrationData.class);
        mRealm.beginTransaction();
        CalibrationData mCalibrationDataHighCal = mRealm.createObject(CalibrationData.class, newprimekeyHighCal);
        mCalibrationDataHighCal.setbg(higher_bg);
        mCalibrationDataHighCal.setslope(1);
        mCalibrationDataHighCal.setintercept(higher_bg);
        mCalibrationDataHighCal.setsensor_id(currentsensor_id);
        //mCalibrationDataHighCal.setestimate_raw_at_time_of_calibration(highBgReading.age_adjusted_raw_value);
        //mCalibrationDataHighCal.setadjusted_raw_value();
        //mCalibrationDataHighCal.setraw_value();
        //mCalibrationDataHighCal.setraw_timestamp();
        mRealm.commitTransaction();
        mRealm.close();


        //higherCalibration.estimate_raw_at_time_of_calibration = highBgReading.age_adjusted_raw_value;
        //higherCalibration.adjusted_raw_value = highBgReading.age_adjusted_raw_value;
        //higherCalibration.raw_value = highBgReading.raw_data;
        //higherCalibration.raw_timestamp = highBgReading.timestamp;
        //higherCalibration.save();
        //highBgReading.calculated_value = higher_bg;
        //highBgReading.calibration_flag = true;
        //highBgReading.calibration = higherCalibration;
        //highBgReading.save();
        //higherCalibration.save();

    }
}
