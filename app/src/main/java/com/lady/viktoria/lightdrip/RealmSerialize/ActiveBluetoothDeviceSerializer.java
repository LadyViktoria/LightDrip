package com.lady.viktoria.lightdrip.RealmSerialize;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.lady.viktoria.lightdrip.RealmModels.ActiveBluetoothDevice;

import java.lang.reflect.Type;

public class ActiveBluetoothDeviceSerializer implements JsonSerializer<ActiveBluetoothDevice> {

    @Override
    public JsonElement serialize(ActiveBluetoothDevice src, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", src.getname());
        jsonObject.addProperty("address", src.getaddress());
        jsonObject.addProperty("connected", src.getconnected());
        return jsonObject;
    }
}