package com.lady.viktoria.lightdrip.Models;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.DateTypeAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public class Calibration {
    public static final double LOW_SLOPE_1 = 0.95;
    public static final double LOW_SLOPE_2 = 0.85;
    public static final double HIGH_SLOPE_1 = 1.3;
    public static final double HIGH_SLOPE_2 = 1.4;
    public static final double DEFAULT_LOW_SLOPE_LOW = 1.08;
    public static final double DEFAULT_LOW_SLOPE_HIGH = 1.15;
    public static final int DEFAULT_SLOPE = 1;
    public static final double DEFAULT_HIGH_SLOPE_HIGH = 1.3;
    public static final double DEFAUL_HIGH_SLOPE_LOW = 1.2;
    private final static String TAG = Calibration.class.getSimpleName();
    public long timestamp;
    public double sensor_age_at_time_of_estimation;
    public Sensor sensor;
    public double bg;
    public double raw_value;
//
//    @Expose
//    @Column(name = "filtered_value")
//    public double filtered_value;

    public double adjusted_raw_value;
    public double sensor_confidence;
    public double slope_confidence;
    public long raw_timestamp;
    public double slope;
    public double intercept;
    public double distance_from_estimate;
    public double estimate_raw_at_time_of_calibration;
    public double estimate_bg_at_time_of_calibration;
    public String uuid;
    public String sensor_uuid;
    public Boolean possible_bad;
    public boolean check_in;
    public double first_decay;
    public double second_decay;
    public double first_slope;
    public double second_slope;
    public double first_intercept;
    public double second_intercept;
    public double first_scale;
    public double second_scale;

    public static void initialCalibration(double bg1, double bg2, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String unit = prefs.getString("units", "mgdl");

        if (unit.compareTo("mgdl") != 0) {
            bg1 = bg1 * 18.0182;
            bg2 = bg2 * 18.0182;
        }
        clear_all_existing_calibrations();

        Calibration higherCalibration = new Calibration();
        Calibration lowerCalibration = new Calibration();
        Sensor sensor = Sensor.currentSensor();
        List<BgReading> bgReadings = BgReading.latest_by_size(2);
        BgReading bgReading1 = bgReadings.get(0);
        BgReading bgReading2 = bgReadings.get(1);
        BgReading highBgReading;
        BgReading lowBgReading;
        double higher_bg = Math.max(bg1, bg2);
        double lower_bg = Math.min(bg1, bg2);

        if (bgReading1.raw_data > bgReading2.raw_data) {
            highBgReading = bgReading1;
            lowBgReading = bgReading2;
        } else {
            highBgReading = bgReading2;
            lowBgReading = bgReading1;
        }

        higherCalibration.bg = higher_bg;
        higherCalibration.slope = 1;
        higherCalibration.intercept = higher_bg;
        higherCalibration.sensor = sensor;
        higherCalibration.estimate_raw_at_time_of_calibration = highBgReading.age_adjusted_raw_value;
        higherCalibration.adjusted_raw_value = highBgReading.age_adjusted_raw_value;
        higherCalibration.raw_value = highBgReading.raw_data;
        higherCalibration.raw_timestamp = highBgReading.timestamp;
//        higherCalibration.save();

        highBgReading.calculated_value = higher_bg;
        highBgReading.calibration_flag = true;
        highBgReading.calibration = higherCalibration;
    //    highBgReading.save();
  //      higherCalibration.save();

        lowerCalibration.bg = lower_bg;
        lowerCalibration.slope = 1;
        lowerCalibration.intercept = lower_bg;
        lowerCalibration.sensor = sensor;
        lowerCalibration.estimate_raw_at_time_of_calibration = lowBgReading.age_adjusted_raw_value;
        lowerCalibration.adjusted_raw_value = lowBgReading.age_adjusted_raw_value;
        lowerCalibration.raw_value = lowBgReading.raw_data;
        lowerCalibration.raw_timestamp = lowBgReading.timestamp;
      //  lowerCalibration.save();

        lowBgReading.calculated_value = lower_bg;
        lowBgReading.calibration_flag = true;
        lowBgReading.calibration = lowerCalibration;
        //lowBgReading.save();
        //lowerCalibration.save();

        highBgReading.find_new_curve();
        highBgReading.find_new_raw_curve();
        lowBgReading.find_new_curve();
        lowBgReading.find_new_raw_curve();

        List<Calibration> calibrations = new ArrayList<Calibration>();
        calibrations.add(lowerCalibration);
        calibrations.add(higherCalibration);

        for (Calibration calibration : calibrations) {
            calibration.timestamp = new Date().getTime();
            calibration.sensor_uuid = sensor.uuid;
            calibration.slope_confidence = .5;
            calibration.distance_from_estimate = 0;
            calibration.check_in = false;
            calibration.sensor_confidence = ((-0.0018 * calibration.bg * calibration.bg) + (0.6657 * calibration.bg) + 36.7505) / 100;

            calibration.sensor_age_at_time_of_estimation = calibration.timestamp - sensor.started_at;
            calibration.uuid = UUID.randomUUID().toString();
          //  calibration.save();

            calculate_w_l_s();
            //CalibrationSendQueue.addToQueue(calibration, context);
        }
        adjustRecentBgReadings(5);
        //CalibrationRequest.createOffset(lowerCalibration.bg, 35);
        //context.startService(new Intent(context, Notifications.class));
    }

    public static Calibration create(double bg, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String unit = prefs.getString("units", "mgdl");

        if (unit.compareTo("mgdl") != 0) {
            bg = bg * 18.0182;
        }

        Calibration calibration = new Calibration();
        Sensor sensor = Sensor.currentSensor();

        if (sensor != null) {
            BgReading bgReading = BgReading.last();
            if (bgReading != null) {
                calibration.sensor = sensor;
                calibration.bg = bg;
                calibration.check_in = false;
                calibration.timestamp = new Date().getTime();
                calibration.raw_value = bgReading.raw_data;
                calibration.adjusted_raw_value = bgReading.age_adjusted_raw_value;
                calibration.sensor_uuid = sensor.uuid;
                calibration.slope_confidence = Math.min(Math.max(((4 - Math.abs((bgReading.calculated_value_slope) * 60000)) / 4), 0), 1);

                double estimated_raw_bg = BgReading.estimated_raw_bg(new Date().getTime());
                calibration.raw_timestamp = bgReading.timestamp;
                if (Math.abs(estimated_raw_bg - bgReading.age_adjusted_raw_value) > 20) {
                    calibration.estimate_raw_at_time_of_calibration = bgReading.age_adjusted_raw_value;
                } else {
                    calibration.estimate_raw_at_time_of_calibration = estimated_raw_bg;
                }
                calibration.distance_from_estimate = Math.abs(calibration.bg - bgReading.calculated_value);
                calibration.sensor_confidence = Math.max(((-0.0018 * bg * bg) + (0.6657 * bg) + 36.7505) / 100, 0);
                calibration.sensor_age_at_time_of_estimation = calibration.timestamp - sensor.started_at;
                calibration.uuid = UUID.randomUUID().toString();
               // calibration.save();

                bgReading.calibration = calibration;
                bgReading.calibration_flag = true;
               // bgReading.save();
                // BgSendQueue.handleNewBgReading(bgReading, "update", context);

                calculate_w_l_s();
                adjustRecentBgReadings();
                // CalibrationSendQueue.addToQueue(calibration, context);
                //context.startService(new Intent(context, Notifications.class));
                Calibration.requestCalibrationIfRangeTooNarrow();
            }
        } else {
            Log.d("CALIBRATION", "No sensor, cant save!");
        }
        return Calibration.last();
    }


    private static void calculate_w_l_s() {
        if (Sensor.isActive()) {
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



    public static void adjustRecentBgReadings() {// This just adjust the last 30 bg readings transition from one calibration point to the next
        adjustRecentBgReadings(30);
    }

    public static void adjustRecentBgReadings(int adjustCount) {
        //TODO: add some handling around calibration overrides as they come out looking a bit funky
        List<Calibration> calibrations = Calibration.latest(3);
        List<BgReading> bgReadings = BgReading.latestUnCalculated(adjustCount);
        if (calibrations.size() == 3) {
            int denom = bgReadings.size();
            Calibration latestCalibration = calibrations.get(0);
            int i = 0;
            for (BgReading bgReading : bgReadings) {
                double oldYValue = bgReading.calculated_value;
                double newYvalue = (bgReading.age_adjusted_raw_value * latestCalibration.slope) + latestCalibration.intercept;
                bgReading.calculated_value = ((newYvalue * (denom - i)) + (oldYValue * (i))) / denom;
                //bgReading.save();
                i += 1;
            }
        } else if (calibrations.size() == 2) {
            Calibration latestCalibration = calibrations.get(0);
            for (BgReading bgReading : bgReadings) {
                double newYvalue = (bgReading.age_adjusted_raw_value * latestCalibration.slope) + latestCalibration.intercept;
                bgReading.calculated_value = newYvalue;
                //bgReading.save();

            }
        }
        bgReadings.get(0).find_new_raw_curve();
        bgReadings.get(0).find_new_curve();
    }

    public static void requestCalibrationIfRangeTooNarrow() {
        double max = Calibration.max_recent();
        double min = Calibration.min_recent();
        if ((max - min) < 55) {
            double avg = ((min + max) / 2);
            double dist = max - avg;
            //CalibrationRequest.createOffset(avg, dist + 20);
        }
    }

    public static void clear_all_existing_calibrations() {
        //CalibrationRequest.clearAll();
        List<Calibration> pastCalibrations = Calibration.allForSensor();
        if (pastCalibrations != null) {
            for (Calibration calibration : pastCalibrations) {
                calibration.slope_confidence = 0;
                calibration.sensor_confidence = 0;
               // calibration.save();
            }
        }

    }



    //COMMON SCOPES!
    public static Calibration last() {
        Sensor sensor = Sensor.currentSensor();
        if (sensor == null) {
            return null;
        }
        /*
        return new Select()
                .from(Calibration.class)
                .where("Sensor = ? ", sensor.getId())
                .orderBy("timestamp desc")
                .executeSingle();
                */
        return null;
    }

    public static Calibration first() {
        Sensor sensor = Sensor.currentSensor();
        /*
        return new Select()
                .from(Calibration.class)
                .where("Sensor = ? ", sensor.getId())
                .where("slope_confidence != 0")
                .where("sensor_confidence != 0")
                .orderBy("timestamp asc")
                .executeSingle();
                */
        return null;
    }

    public static double max_recent() {
        Sensor sensor = Sensor.currentSensor();
        /*
        Calibration calibration = new Select()
                .from(Calibration.class)
                .where("Sensor = ? ", sensor.getId())
                .where("slope_confidence != 0")
                .where("sensor_confidence != 0")
                .where("timestamp > ?", (new Date().getTime() - (60000 * 60 * 24 * 4)))
                .orderBy("bg desc")
                .executeSingle();

        if (calibration != null) {
            return calibration.bg;
        } else {
            return 120;
        }
        */
        return 0;
    }

    public static double min_recent() {
        Sensor sensor = Sensor.currentSensor();
        /*
        Calibration calibration = new Select()
                .from(Calibration.class)
                .where("Sensor = ? ", sensor.getId())
                .where("slope_confidence != 0")
                .where("sensor_confidence != 0")
                .where("timestamp > ?", (new Date().getTime() - (60000 * 60 * 24 * 4)))
                .orderBy("bg asc")
                .executeSingle();
        if (calibration != null) {
            return calibration.bg;
        } else {
            return 100;
        }
        */
        return 0;
    }

    public static List<Calibration> latest(int number) {
        Sensor sensor = Sensor.currentSensor();
        if (sensor == null) {
            return null;
        }
        /*
        return new Select()
                .from(Calibration.class)
                .where("Sensor = ? ", sensor.getId())
                .orderBy("timestamp desc")
                .limit(number)
                .execute();
                */
        return null;
    }



    public static List<Calibration> allForSensor() {
        Sensor sensor = Sensor.currentSensor();
        if (sensor == null) {
            return null;
        }
        /*
        return new Select()
                .from(Calibration.class)
                .where("Sensor = ? ", sensor.getId())
                .where("slope_confidence != 0")
                .where("sensor_confidence != 0")
                .orderBy("timestamp desc")
                .execute();
                */
        return null;
    }

    public static List<Calibration> allForSensorInLastFourDays() {
        Sensor sensor = Sensor.currentSensor();
        if (sensor == null) {
            return null;
        }
        /*
        return new Select()
                .from(Calibration.class)
                .where("Sensor = ? ", sensor.getId())
                .where("slope_confidence != 0")
                .where("sensor_confidence != 0")
                .where("timestamp > ?", (new Date().getTime() - (60000 * 60 * 24 * 4)))
                .orderBy("timestamp desc")
                .execute();
                */
        return null;
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

    private double calculateWeight() {
        double firstTimeStarted = Calibration.first().sensor_age_at_time_of_estimation;
        double lastTimeStarted = Calibration.last().sensor_age_at_time_of_estimation;
        double time_percentage = Math.min(((sensor_age_at_time_of_estimation - firstTimeStarted) / (lastTimeStarted - firstTimeStarted)) / (.85), 1);
        time_percentage = (time_percentage + .01);
        Log.i(TAG, "CALIBRATIONS TIME PERCENTAGE WEIGHT: " + time_percentage);
        return Math.max((((((slope_confidence + sensor_confidence) * (time_percentage))) / 2) * 100), 1);
    }

    public void rawValueOverride(double rawValue, Context context) {
        estimate_raw_at_time_of_calibration = rawValue;
        //save();
        calculate_w_l_s();
        //CalibrationSendQueue.addToQueue(this, context);
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
