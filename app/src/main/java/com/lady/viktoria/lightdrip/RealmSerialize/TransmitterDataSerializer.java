package com.lady.viktoria.lightdrip.RealmSerialize;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.lady.viktoria.lightdrip.RealmModels.TransmitterData;

import java.lang.reflect.Type;

public class TransmitterDataSerializer implements JsonSerializer<TransmitterData> {

    @Override
    public JsonElement serialize(TransmitterData src, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", src.getid());
        jsonObject.addProperty("timestamp", src.gettimestamp());
        jsonObject.addProperty("raw_data", src.getraw_data());
        jsonObject.addProperty("filtered_data", src.getfiltered_data());
        jsonObject.addProperty("transmitter_battery_level", src.gettransmitter_battery_level());
        jsonObject.addProperty("bridge_battery_level", src.getbridge_battery_level());
        return jsonObject;
    }
}