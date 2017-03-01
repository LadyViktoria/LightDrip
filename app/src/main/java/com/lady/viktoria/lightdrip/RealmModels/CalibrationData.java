package com.lady.viktoria.lightdrip.RealmModels;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


public class CalibrationData extends RealmObject {

    @PrimaryKey
    private long id;
    public long getid() {
        return id;
    }
    public void setid(final long id) {
        this.id = id;
    }

    private long timestamp;
    public long gettimestamp() {
        return timestamp;
    }
    public void settimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }

    private double sensor_age_at_time_of_estimation;
    public double getsensor_age_at_time_of_estimation() {
        return sensor_age_at_time_of_estimation;
    }
    public void setsensor_age_at_time_of_estimation(final Double sensor_age_at_time_of_estimation) {
        this.sensor_age_at_time_of_estimation = sensor_age_at_time_of_estimation;
    }

    //public Sensor sensor;

    private double bg;
    public double getbg() {
        return bg;
    }
    public void setbg(final double bg) {
        this.bg = bg;
    }

    private double raw_value;
    public double getraw_value() {
        return raw_value;
    }
    public void setraw_value(final double raw_value) {
        this.raw_value = raw_value;
    }

    private double adjusted_raw_value;
    public double getadjusted_raw_value() {
        return adjusted_raw_value;
    }
    public void setadjusted_raw_value(final double adjusted_raw_value) {
        this.adjusted_raw_value = adjusted_raw_value;
    }

    private double sensor_confidence;
    public double getsensor_confidence() {
        return sensor_confidence;
    }
    public void setsensor_confidence(final double sensor_confidence) {
        this.sensor_confidence = sensor_confidence;
    }

    private double slope_confidence;
    public double getslope_confidence() {
        return slope_confidence;
    }
    public void setslope_confidence(final double slope_confidence) {
        this.slope_confidence = slope_confidence;
    }

    private long raw_timestamp;
    public long getraw_timestamp() {
        return raw_timestamp;
    }
    public void setraw_timestamp(final long raw_timestamp) {
        this.raw_timestamp = raw_timestamp;
    }

    private double slope;
    public double getslope() {
        return slope;
    }
    public void setslope(final double slope) {
        this.slope = slope;
    }

    private double intercept;
    public double getintercept() {
        return intercept;
    }
    public void setintercept(final double intercept) {
        this.intercept = intercept;
    }

    private double distance_from_estimate;
    public double getdistance_from_estimate() {
        return distance_from_estimate;
    }
    public void setdistance_from_estimate(final double distance_from_estimate) {
        this.distance_from_estimate = distance_from_estimate;
    }

    private double estimate_raw_at_time_of_calibration;
    public double getestimate_raw_at_time_of_calibration() {
        return estimate_raw_at_time_of_calibration;
    }
    public void setestimate_raw_at_time_of_calibration(final double estimate_raw_at_time_of_calibration) {
        this.estimate_raw_at_time_of_calibration = estimate_raw_at_time_of_calibration;
    }

    private double estimate_bg_at_time_of_calibration;
    public double getestimate_bg_at_time_of_calibration() {
        return estimate_bg_at_time_of_calibration;
    }
    public void setestimate_bg_at_time_of_calibration(final double estimate_bg_at_time_of_calibration) {
        this.estimate_bg_at_time_of_calibration = estimate_bg_at_time_of_calibration;
    }

    private long sensor_id;
    public long getsensor_id() {
        return sensor_id;
    }
    public void setsensor_id(final long sensor_id) {
        this.sensor_id = sensor_id;
    }

    private boolean possible_bad;
    public boolean getpossible_bad() {
        return possible_bad;
    }
    public void setpossible_bad(final boolean possible_bad) {
        this.possible_bad = possible_bad;
    }

    private boolean check_in;
    public boolean getcheck_in() {
        return check_in;
    }
    public void setcheck_in(final boolean check_in) {
        this.check_in = check_in;
    }

    private double first_decay;
    public double getfirst_decay() {
        return first_decay;
    }
    public void setfirst_decay(final double first_decay) {
        this.first_decay = first_decay;
    }

    private double second_decay;
    public double getsecond_decay() {
        return second_decay;
    }
    public void setsecond_decay(final double second_decay) {
        this.second_decay = second_decay;
    }

    private double first_slope;
    public double getfirst_slope() {
        return first_slope;
    }
    public void setfirst_slope(final double first_slope) {
        this.first_slope = first_slope;
    }

    private double second_slope;
    public double getsecond_slope() {
        return second_slope;
    }
    public void setsecond_slope(final double second_slope) {
        this.second_slope = second_slope;
    }

    private double first_intercept;
    public double getfirst_intercept() {
        return first_intercept;
    }
    public void setfirst_intercept(final double first_intercept) {
        this.first_intercept = first_intercept;
    }

    private double second_intercept;
    public double getsecond_intercept() {
        return second_intercept;
    }
    public void setsecond_intercept(final double second_intercept) {
        this.second_intercept = second_intercept;
    }

    private double first_scale;
    public double getfirst_scale() {
        return first_scale;
    }
    public void setfirst_scale(final double first_scale) {
        this.first_scale = first_scale;
    }

    private double second_scale;
    public double getsecond_scale() {
        return second_scale;
    }
    public void setsecond_scale(final double second_scale) {
        this.second_scale = second_scale;
    }

}