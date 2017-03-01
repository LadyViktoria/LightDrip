package com.lady.viktoria.lightdrip.Models;

import android.util.Log;

import java.util.Date;
import java.util.UUID;


public class Sensor {


    public long started_at;


    public long stopped_at;


    public int latest_battery_level;

    public String uuid;

    public String sensor_location;

    public static Sensor create(long started_at) {
        Sensor sensor = new Sensor();
        sensor.started_at = started_at;
        sensor.uuid = UUID.randomUUID().toString();

        //sensor.save();
        //SensorSendQueue.addToQueue(sensor);
        Log.d("SENSOR MODEL:", sensor.toString());
        return sensor;
    }

    public static void stopSensor() {
        Sensor sensor = currentSensor();
        if (sensor == null) {
            return;
        }
        sensor.stopped_at = new Date().getTime();
        Log.i("NEW SENSOR", "Sensor stopped at " + sensor.stopped_at);
        //sensor.save();
        //SensorSendQueue.addToQueue(sensor);

    }

    public static Sensor currentSensor() {
        //Sensor sensor = new Select()
        //      .from(Sensor.class)
        //    .where("started_at != 0")
        //  .where("stopped_at = 0")
        //  .orderBy("_ID desc")
        //  .limit(1)
        // .executeSingle();
        //return sensor;
        return null;
    }

    public static boolean isActive() {
        //Sensor sensor = new Select()
        //       .from(Sensor.class)
        //      .where("started_at != 0")
        //     .where("stopped_at = 0")
        //    .orderBy("_ID desc")
        //   .limit(1)
        //  .executeSingle();
        //   if (sensor == null) {
        //     return false;
        // } else {
        return true;
        // }
    }

}

