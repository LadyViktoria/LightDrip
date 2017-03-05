package com.lady.viktoria.lightdrip.Models;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.DateTypeAdapter;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class BgReading {
    public static final double AGE_ADJUSTMENT_TIME = 86400000 * 1.9;
    public static final double AGE_ADJUSTMENT_FACTOR = .45;
    //TODO: Have these as adjustable settings!!
    public final static double BESTOFFSET = (60000 * 0); // Assume readings are about x minutes off from actual!
    // The extra 120,000 is to allow the packet to be delayed for some time and still be counted in that group
    // Please don't use for MAX_INFLUANCE a number that is complete multiply of 5 minutes (300,000)
    static final int MAX_INFLUANCE = 30 * 60000 - 120000; // A bad point means data is untrusted for 30 minutes.
    private final static String TAG = BgReading.class.getSimpleName();
    private final static String TAG_ALERT = TAG + " AlertBg";
    private static boolean predictBG;
    public Sensor sensor;
    public Calibration calibration;
    public long timestamp;

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


    public static double calculateSlope(BgReading current, BgReading last) {
        if (current.timestamp == last.timestamp || current.calculated_value == last.calculated_value) {
            return 0;
        } else {
            return (last.calculated_value - current.calculated_value) / (last.timestamp - current.timestamp);
        }
    }


    //*******CLASS METHODS***********//


    public static BgReading create(double raw_data, double filtered_data, Context context, Long timestamp) {
        BgReading bgReading = new BgReading();
        Sensor sensor = Sensor.currentSensor();
        if (sensor == null) {
            Log.i("BG GSON: ", bgReading.toS());

            return bgReading;
        }

        Calibration calibration = Calibration.last();
        if (calibration == null) {
            Log.d(TAG, "create: No calibration yet");
            bgReading.sensor = sensor;
            bgReading.sensor_uuid = sensor.uuid;
            bgReading.raw_data = (raw_data / 1000);
            bgReading.filtered_data = (filtered_data / 1000);
            bgReading.timestamp = timestamp;
            bgReading.uuid = UUID.randomUUID().toString();
            bgReading.time_since_sensor_started = bgReading.timestamp - sensor.started_at;
            bgReading.synced = false;
            bgReading.calibration_flag = false;

            bgReading.calculateAgeAdjustedRawValue();

            //bgReading.save();
            bgReading.perform_calculations();
        } else {
            Log.d(TAG, "Calibrations, so doing everything");
            bgReading.sensor = sensor;
            bgReading.sensor_uuid = sensor.uuid;
            bgReading.calibration = calibration;
            bgReading.calibration_uuid = calibration.uuid;
            bgReading.raw_data = (raw_data / 1000);
            bgReading.filtered_data = (filtered_data / 1000);
            bgReading.timestamp = timestamp;
            bgReading.uuid = UUID.randomUUID().toString();
            bgReading.time_since_sensor_started = bgReading.timestamp - sensor.started_at;
            bgReading.synced = false;

            bgReading.calculateAgeAdjustedRawValue();

            if (calibration.check_in) {
                double firstAdjSlope = calibration.first_slope + (calibration.first_decay * (Math.ceil(new Date().getTime() - calibration.timestamp) / (1000 * 60 * 10)));
                double calSlope = (calibration.first_scale / firstAdjSlope) * 1000;
                double calIntercept = ((calibration.first_scale * calibration.first_intercept) / firstAdjSlope) * -1;
                bgReading.calculated_value = (((calSlope * bgReading.raw_data) + calIntercept) - 5);

            } else {
                BgReading lastBgReading = BgReading.last();
                if (lastBgReading != null && lastBgReading.calibration != null) {
                    if (lastBgReading.calibration_flag == true && ((lastBgReading.timestamp + (60000 * 20)) > bgReading.timestamp) && ((lastBgReading.calibration.timestamp + (60000 * 20)) > bgReading.timestamp)) {
                        lastBgReading.calibration.rawValueOverride(BgReading.weightedAverageRaw(lastBgReading.timestamp, bgReading.timestamp, lastBgReading.calibration.timestamp, lastBgReading.age_adjusted_raw_value, bgReading.age_adjusted_raw_value), context);
                    }
                }
                bgReading.calculated_value = ((calibration.slope * bgReading.age_adjusted_raw_value) + calibration.intercept);
            }
            if (bgReading.calculated_value < 10) {
                bgReading.calculated_value = 9;
                bgReading.hide_slope = true;
            } else {
                bgReading.calculated_value = Math.min(400, Math.max(39, bgReading.calculated_value));
            }
            Log.i(TAG, "NEW VALUE CALCULATED AT: " + bgReading.calculated_value);

            //bgReading.save();
            bgReading.perform_calculations();
            // context.startService(new Intent(context, wearDripWatchFace.class));
            // BgSendQueue.handleNewBgReading(bgReading, "create", context);
        }

        Log.i("BG GSON: ", bgReading.toS());

        return bgReading;
    }


    public static BgReading last() {
        Sensor sensor = Sensor.currentSensor();
        if (sensor != null) {
            /*
            return new Select()
                    .from(BgReading.class)
                    .where("Sensor = ? ", sensor.getId())
                    .where("calculated_value != 0")
                    .where("raw_data != 0")
                    .orderBy("timestamp desc")
                    .executeSingle();
                    */
        }
        return null;
    }

    public static List<BgReading> latest_by_size(int number) {
        Sensor sensor = Sensor.currentSensor();
        /*
        return new Select()
                .from(BgReading.class)
                .where("Sensor = ? ", sensor.getId())
                .where("raw_data != 0")
                .orderBy("timestamp desc")
                .limit(number)
                .execute();
                */
        return null;
    }

    public static BgReading lastNoSenssor() {
        /*
        return new Select()
                .from(BgReading.class)
                .where("calculated_value != 0")
                .where("raw_data != 0")
                .orderBy("timestamp desc")
                .executeSingle();
                */
        return null;
    }

    public static List<BgReading> latest(int number) {
        Sensor sensor = Sensor.currentSensor();
        if (sensor == null) {
            return null;
        }
        /*
        return new Select()
                .from(BgReading.class)
                .where("Sensor = ? ", sensor.getId())
                .where("calculated_value != 0")
                .where("raw_data != 0")
                .orderBy("timestamp desc")
                .limit(number)
                .execute();
                */
        return null;
    }

    public static List<BgReading> latestUnCalculated(int number) {
        Sensor sensor = Sensor.currentSensor();
        if (sensor == null) {
            return null;
        }
        /*
        return new Select()
                .from(BgReading.class)
                .where("Sensor = ? ", sensor.getId())
                .where("raw_data != 0")
                .orderBy("timestamp desc")
                .limit(number)
                .execute();
                */
        return null;
    }


    public static double estimated_raw_bg(double timestamp) {
        timestamp = timestamp + BESTOFFSET;
        double estimate;
        BgReading latest = BgReading.last();
        if (latest == null) {
            Log.i(TAG, "No data yet, assume perfect!");
            estimate = 160;
        } else {
            estimate = (latest.ra * timestamp * timestamp) + (latest.rb * timestamp) + latest.rc;
        }
        Log.i(TAG, "ESTIMATE RAW BG" + estimate);
        return estimate;
    }

    public static double weightedAverageRaw(double timeA, double timeB, double calibrationTime, double rawA, double rawB) {
        double relativeSlope = (rawB - rawA) / (timeB - timeA);
        double relativeIntercept = rawA - (relativeSlope * timeA);
        return ((relativeSlope * calibrationTime) + relativeIntercept);
    }

    //*******INSTANCE METHODS***********//
    public void perform_calculations() {
        find_new_curve();
        find_new_raw_curve();
        find_slope();
    }

    /*
     * returns the time (in ms) that the state is not clear and no alerts should work
     * The base of the algorithm is that any period can be bad or not. bgReading.Unclear() tells that.
     * a non clear bgReading means MAX_INFLUANCE time after it we are in a bad position
     * Since this code is based on heuristics, and since times are not accurate, boundary issues can be ignored.
     *
     * interstingTime is the period to check. That is if the last period is bad, we want to know how long does it go bad...
     * */

    public void find_slope() {
        List<BgReading> last_2 = BgReading.latest(2);

        assert last_2.get(0) == this : "Invariant condition not fulfilled: calculating slope and current reading wasn't saved before";

        if (last_2.size() == 2) {
            calculated_value_slope = calculateSlope(this, last_2.get(1));
            //save();
        } else if (last_2.size() == 1) {
            calculated_value_slope = 0;
            //save();
        } else {
            Log.w(TAG, "NO BG? COULDNT FIND SLOPE!");
        }
    }

    public void find_new_curve() {
        List<BgReading> last_3 = BgReading.latest(3);
        if (last_3.size() == 3) {
            BgReading second_latest = last_3.get(1);
            BgReading third_latest = last_3.get(2);

            double y3 = calculated_value;
            double x3 = timestamp;
            double y2 = second_latest.calculated_value;
            double x2 = second_latest.timestamp;
            double y1 = third_latest.calculated_value;
            double x1 = third_latest.timestamp;

            a = y1 / ((x1 - x2) * (x1 - x3)) + y2 / ((x2 - x1) * (x2 - x3)) + y3 / ((x3 - x1) * (x3 - x2));
            b = (-y1 * (x2 + x3) / ((x1 - x2) * (x1 - x3)) - y2 * (x1 + x3) / ((x2 - x1) * (x2 - x3)) - y3 * (x1 + x2) / ((x3 - x1) * (x3 - x2)));
            c = (y1 * x2 * x3 / ((x1 - x2) * (x1 - x3)) + y2 * x1 * x3 / ((x2 - x1) * (x2 - x3)) + y3 * x1 * x2 / ((x3 - x1) * (x3 - x2)));

            Log.i(TAG, "find_new_curve: BG PARABOLIC RATES: " + a + "x^2 + " + b + "x + " + c);

            //save();
        } else if (last_3.size() == 2) {

            Log.i(TAG, "find_new_curve: Not enough data to calculate parabolic rates - assume Linear");
            BgReading latest = last_3.get(0);
            BgReading second_latest = last_3.get(1);

            double y2 = latest.calculated_value;
            double x2 = timestamp;
            double y1 = second_latest.calculated_value;
            double x1 = second_latest.timestamp;

            if (y1 == y2) {
                b = 0;
            } else {
                b = (y2 - y1) / (x2 - x1);
            }
            a = 0;
            c = -1 * ((latest.b * x1) - y1);

            Log.i(TAG, "" + latest.a + "x^2 + " + latest.b + "x + " + latest.c);
            //save();
        } else {
            Log.i(TAG, "find_new_curve: Not enough data to calculate parabolic rates - assume static data");
            a = 0;
            b = 0;
            c = calculated_value;

            Log.i(TAG, "" + a + "x^2 + " + b + "x + " + c);
            //save();
        }
    }

    public void calculateAgeAdjustedRawValue() {
        double adjust_for = AGE_ADJUSTMENT_TIME - time_since_sensor_started;
        if (adjust_for > 0) {
            age_adjusted_raw_value = ((AGE_ADJUSTMENT_FACTOR * (adjust_for / AGE_ADJUSTMENT_TIME)) * raw_data) + raw_data;
            Log.i(TAG, "calculateAgeAdjustedRawValue: RAW VALUE ADJUSTMENT FROM:" + raw_data + " TO: " + age_adjusted_raw_value);
        } else {
            age_adjusted_raw_value = raw_data;
        }
    }

    public void find_new_raw_curve() {
        List<BgReading> last_3 = BgReading.latest(3);
        if (last_3.size() == 3) {
            BgReading second_latest = last_3.get(1);
            BgReading third_latest = last_3.get(2);

            double y3 = age_adjusted_raw_value;
            double x3 = timestamp;
            double y2 = second_latest.age_adjusted_raw_value;
            double x2 = second_latest.timestamp;
            double y1 = third_latest.age_adjusted_raw_value;
            double x1 = third_latest.timestamp;

            ra = y1 / ((x1 - x2) * (x1 - x3)) + y2 / ((x2 - x1) * (x2 - x3)) + y3 / ((x3 - x1) * (x3 - x2));
            rb = (-y1 * (x2 + x3) / ((x1 - x2) * (x1 - x3)) - y2 * (x1 + x3) / ((x2 - x1) * (x2 - x3)) - y3 * (x1 + x2) / ((x3 - x1) * (x3 - x2)));
            rc = (y1 * x2 * x3 / ((x1 - x2) * (x1 - x3)) + y2 * x1 * x3 / ((x2 - x1) * (x2 - x3)) + y3 * x1 * x2 / ((x3 - x1) * (x3 - x2)));

            Log.i(TAG, "find_new_raw_curve: RAW PARABOLIC RATES: " + ra + "x^2 + " + rb + "x + " + rc);
            //  save();
        } else if (last_3.size() == 2) {
            BgReading latest = last_3.get(0);
            BgReading second_latest = last_3.get(1);

            double y2 = latest.age_adjusted_raw_value;
            double x2 = timestamp;
            double y1 = second_latest.age_adjusted_raw_value;
            double x1 = second_latest.timestamp;
            if (y1 == y2) {
                rb = 0;
            } else {
                rb = (y2 - y1) / (x2 - x1);
            }
            ra = 0;
            rc = -1 * ((latest.rb * x1) - y1);

            Log.i(TAG, "find_new_raw_curve: Not enough data to calculate parabolic rates - assume Linear data");

            Log.i(TAG, "RAW PARABOLIC RATES: " + ra + "x^2 + " + rb + "x + " + rc);
            //      save();
        } else {
            Log.i(TAG, "find_new_raw_curve: Not enough data to calculate parabolic rates - assume static data");
            BgReading latest_entry = BgReading.lastNoSenssor();
            ra = 0;
            rb = 0;
            if (latest_entry != null) {
                rc = latest_entry.age_adjusted_raw_value;
            } else {
                rc = 105;
            }

            //    save();
        }
    }

    public String toS() {
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(Date.class, new DateTypeAdapter())
                .serializeSpecialFloatingPointValues()
                .create();
        return gson.toJson(this);
    }

}