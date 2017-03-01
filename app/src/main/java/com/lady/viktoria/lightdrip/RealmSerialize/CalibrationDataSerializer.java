package com.lady.viktoria.lightdrip.RealmSerialize;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.lady.viktoria.lightdrip.RealmModels.CalibrationData;

import java.lang.reflect.Type;

public class CalibrationDataSerializer implements JsonSerializer<CalibrationData> {

    @Override
    public JsonElement serialize(CalibrationData src, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", src.getid());
        jsonObject.addProperty("timestamp", src.gettimestamp());
        jsonObject.addProperty("sensor_age_at_time_of_estimation", src.getsensor_age_at_time_of_estimation());
        jsonObject.addProperty("bg", src.getbg());
        jsonObject.addProperty("raw_value", src.getraw_value());
        jsonObject.addProperty("adjusted_raw_value", src.getadjusted_raw_value());
        jsonObject.addProperty("sensor_confidence", src.getsensor_confidence());
        jsonObject.addProperty("slope_confidence", src.getslope_confidence());
        jsonObject.addProperty("raw_timestamp", src.getraw_timestamp());
        jsonObject.addProperty("slope", src.getslope());
        jsonObject.addProperty("intercept", src.getintercept());
        jsonObject.addProperty("distance_from_estimate", src.getdistance_from_estimate());
        jsonObject.addProperty("estimate_raw_at_time_of_calibration", src.getestimate_raw_at_time_of_calibration());
        jsonObject.addProperty("estimate_bg_at_time_of_calibration", src.getestimate_bg_at_time_of_calibration());
        jsonObject.addProperty("sensor_id", src.getsensor_id());
        jsonObject.addProperty("possible_bad", src.getpossible_bad());
        jsonObject.addProperty("check_in", src.getcheck_in());
        jsonObject.addProperty("first_decay", src.getfirst_decay());
        jsonObject.addProperty("second_decay", src.getsecond_decay());
        jsonObject.addProperty("first_slope", src.getfirst_slope());
        jsonObject.addProperty("second_slope", src.getsecond_slope());
        jsonObject.addProperty("first_intercept", src.getfirst_intercept());
        jsonObject.addProperty("second_intercept", src.getsecond_intercept());
        jsonObject.addProperty("first_scale", src.getfirst_scale());
        jsonObject.addProperty("second_scale", src.getsecond_scale());
        return jsonObject;
    }
}