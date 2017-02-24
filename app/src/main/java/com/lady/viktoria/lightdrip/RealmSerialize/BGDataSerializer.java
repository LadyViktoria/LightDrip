package com.lady.viktoria.lightdrip.RealmSerialize;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.lady.viktoria.lightdrip.RealmModels.BGData;

import java.lang.reflect.Type;

public class BGDataSerializer implements JsonSerializer<BGData> {

    @Override
    public JsonElement serialize(BGData src, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("a", src.getA());
        jsonObject.addProperty("ageAdjustedRawValue", src.getageAdjustedRawValue());
        jsonObject.addProperty("b", src.getB());
        jsonObject.addProperty("c", src.getC());
        jsonObject.addProperty("calculatedValue", src.getCalculatedValue());
        jsonObject.addProperty("calculatedValueSlope", src.getCalculatedValueSlope());
        jsonObject.addProperty("calibrationFlag", src.getCalibrationFlag());
        jsonObject.addProperty("calibrationUuid", src.getCalibrationUuid());
        jsonObject.addProperty("filteredData", src.getFilteredData());
        jsonObject.addProperty("ra", src.getRa());
        jsonObject.addProperty("rawData", src.getRawData());
        jsonObject.addProperty("rb", src.getRb());
        jsonObject.addProperty("rc", src.getRc());
        jsonObject.addProperty("sensorUuid", src.getSensorUuid());
        jsonObject.addProperty("timeSinceSensorStarted", src.getTimeSinceSensorStarted());
        jsonObject.addProperty("timestamp", src.getTimestamp());
        jsonObject.addProperty("uuid", src.getUuid());
        return jsonObject;
    }
}