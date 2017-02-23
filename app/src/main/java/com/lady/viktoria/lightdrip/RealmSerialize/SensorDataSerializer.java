package com.lady.viktoria.lightdrip.RealmSerialize;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.lady.viktoria.lightdrip.DatabaseModels.SensorData;

import java.lang.reflect.Type;

public class SensorDataSerializer implements JsonSerializer<SensorData> {

    @Override
    public JsonElement serialize(SensorData src, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("started_at", src.getstarted_at());
        jsonObject.addProperty("stopped_at", src.getstopped_at());
        jsonObject.addProperty("latest_battery_level", src.getlatest_battery_level());
        jsonObject.addProperty("uuid", src.getuuid());
        jsonObject.addProperty("sensor_location", src.getsensor_location());
        return jsonObject;
    }
}