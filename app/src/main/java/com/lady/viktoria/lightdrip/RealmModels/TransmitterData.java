package com.lady.viktoria.lightdrip.RealmModels;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class TransmitterData extends RealmObject {

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

    private double raw_data;
    public double getraw_data() {
        return raw_data;
    }
    public void setraw_data(final double raw_data) {
        this.raw_data = raw_data;
    }

    private double filtered_data;
    public double getfiltered_data() {
        return filtered_data;
    }
    public void setfiltered_data(final double filtered_data) {
        this.filtered_data = filtered_data;
    }

    private int sensor_battery_level;
    public int getsensor_battery_level() {
        return sensor_battery_level;
    }
    public void setsensor_battery_level(final int sensor_battery_level) {
        this.sensor_battery_level = sensor_battery_level;
    }

}
