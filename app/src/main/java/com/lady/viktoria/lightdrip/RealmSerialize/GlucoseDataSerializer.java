package com.lady.viktoria.lightdrip.RealmSerialize;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.lady.viktoria.lightdrip.RealmModels.GlucoseData;

import java.lang.reflect.Type;

public class GlucoseDataSerializer implements JsonSerializer<GlucoseData> {

    @Override
    public JsonElement serialize(GlucoseData src, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", src.getid());
        jsonObject.addProperty("a", src.getA());
        jsonObject.addProperty("ageAdjustedRawValue", src.getageAdjustedRawValue());
        jsonObject.addProperty("b", src.getB());
        jsonObject.addProperty("c", src.getC());
        jsonObject.addProperty("calculatedValue", src.getCalculatedValue());
        jsonObject.addProperty("calculatedValueSlope", src.getCalculatedValueSlope());
        jsonObject.addProperty("calibrationFlag", src.getCalibrationFlag());
        jsonObject.addProperty("calibration_id", src.getCalibration_id());
        jsonObject.addProperty("filteredData", src.getFilteredData());
        jsonObject.addProperty("ra", src.getRa());
        jsonObject.addProperty("rawData", src.getRawData());
        jsonObject.addProperty("rb", src.getRb());
        jsonObject.addProperty("rc", src.getRc());
        jsonObject.addProperty("sensor_id", src.getSensor_id());
        jsonObject.addProperty("synced", src.getsynced());
        jsonObject.addProperty("timeSinceSensorStarted", src.getTimeSinceSensorStarted());
        jsonObject.addProperty("timestamp", src.getTimestamp());
        return jsonObject;
    }
}