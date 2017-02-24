package com.lady.viktoria.lightdrip.RealmSerialize;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.lady.viktoria.lightdrip.RealmModels.SensorData;

import java.lang.reflect.Type;

public class SensorDataSerializer implements JsonSerializer<SensorData> {

    @Override
    public JsonElement serialize(SensorData src, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("started_at", src.getstarted_at());
        jsonObject.addProperty("stopped_at", src.getstopped_at());
        jsonObject.addProperty("id", src.getid());
        return jsonObject;
    }
}