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

    private int transmitter_battery_level;
    public int gettransmitter_battery_level() {
        return transmitter_battery_level;
    }
    public void settransmitter_battery_level(final int transmitter_battery_level) {
        this.transmitter_battery_level = transmitter_battery_level;
    }

    private int bridge_battery_level;
    public int getbridge_battery_level() {
        return bridge_battery_level;
    }
    public void setbridge_battery_level(final int bridge_battery_level) {
        this.bridge_battery_level = bridge_battery_level;
    }

}
