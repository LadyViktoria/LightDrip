package com.lady.viktoria.lightdrip;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.lady.viktoria.lightdrip.RealmConfig.PrimaryKeyFactory;
import com.lady.viktoria.lightdrip.RealmConfig.RealmBase;

import io.realm.Realm;

import static io.realm.Realm.getInstance;

public class CalibrationData extends RealmBase {
    private final static String TAG = CalibrationData.class.getSimpleName();

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

    private Realm mRealm;
    Context context;
    public Context getcontext() {
        return context;
    }

    private CalibrationData() {
        Realm.init(context);
        mRealm = getInstance(getRealmConfig());
        try {
            PrimaryKeyFactory.getInstance().initialize(mRealm);
        } catch (Exception e) {
            Log.v(TAG, "CalibrationData() PrimaryKeyFactory " + e.getMessage());
        }
    }

    public static void initialCalibration(double bg1, double bg2, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String unit = prefs.getString("units", "mgdl");




    }
}
