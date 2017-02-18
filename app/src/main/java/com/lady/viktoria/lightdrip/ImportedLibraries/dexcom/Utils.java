package com.lady.viktoria.lightdrip.ImportedLibraries.dexcom;

import com.lady.viktoria.lightdrip.ImportedLibraries.dexcom.records.EGVRecord;
import com.lady.viktoria.lightdrip.ImportedLibraries.dexcom.records.GlucoseDataSet;
import com.lady.viktoria.lightdrip.ImportedLibraries.dexcom.records.SensorRecord;

import java.util.Date;
import java.util.TimeZone;

// This code and this particular library are from the NightScout android uploader
// Check them out here: https://github.com/nightscout/android-uploader
// Some of this code may have been modified for use in this project

public class Utils {

    public static Date receiverTimeToDate(long delta) {
        int currentTZOffset = TimeZone.getDefault().getRawOffset();
        long epochMS = 1230768000000L;  // Jan 01, 2009 00:00 in UTC
        long milliseconds = epochMS - currentTZOffset;
        long timeAdd = milliseconds + (1000L * delta);
        TimeZone tz = TimeZone.getDefault();
        if (tz.inDaylightTime(new Date())) timeAdd = timeAdd - (1000 * 60 * 60);
        return new Date(timeAdd);
    }

    public static GlucoseDataSet[] mergeGlucoseDataRecords(EGVRecord[] egvRecords,
                                                           SensorRecord[] sensorRecords) {
        int egvLength = egvRecords.length;
        int sensorLength = sensorRecords.length;
        int smallerLength = egvLength < sensorLength ? egvLength : sensorLength;
        GlucoseDataSet[] glucoseDataSets = new GlucoseDataSet[smallerLength];
        for (int i = 1; i <= smallerLength; i++) {
            glucoseDataSets[smallerLength - i] = new GlucoseDataSet(egvRecords[egvLength - i], sensorRecords[sensorLength - i]);
        }
        return glucoseDataSets;
    }
}
