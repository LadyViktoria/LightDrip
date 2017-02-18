package com.lady.viktoria.lightdrip.DatabaseModels;

import io.realm.RealmObject;


public class SensorData extends RealmObject {

    private long started_at;
    public long getstarted_at() {
        return started_at;
    }
    public void setstarted_at(final long started_at) {
        this.started_at = started_at;
    }

    private long stopped_at;
    public long getstopped_at() {
        return stopped_at;
    }
    public void setstopped_at(final long stopped_at) {
        this.stopped_at = stopped_at;
    }

    private int latest_battery_level;
    public int getlatest_battery_level() {
        return latest_battery_level;
    }
    public void setlatest_battery_level(final int latest_battery_level) {
        this.latest_battery_level = latest_battery_level;
    }

    private String uuid;
    public String getuuid() {
        return uuid;
    }
    public void setuuid(final String uuid) {
        this.uuid = uuid;
    }

    private String sensor_location;
    public String getsensor_location() {
        return sensor_location;
    }
    public void setsensor_location(final String sensor_location) {
        this.sensor_location = sensor_location;
    }

}