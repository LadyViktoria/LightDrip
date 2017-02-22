package com.lady.viktoria.lightdrip;


import com.lady.viktoria.lightdrip.ImportedLibraries.dexcom.records.EGVRecord;
import com.lady.viktoria.lightdrip.ImportedLibraries.dexcom.records.GlucoseDataSet;
import com.lady.viktoria.lightdrip.ImportedLibraries.dexcom.records.SensorRecord;

import java.util.Date;
import java.util.TimeZone;

public class utils {

    public static Integer convertSrc(final String Src) {
        Integer res = 0;
        String tmpSrc = Src.toUpperCase();
        res |= getSrcValue(tmpSrc.charAt(0)) << 20;
        res |= getSrcValue(tmpSrc.charAt(1)) << 15;
        res |= getSrcValue(tmpSrc.charAt(2)) << 10;
        res |= getSrcValue(tmpSrc.charAt(3)) << 5;
        res |= getSrcValue(tmpSrc.charAt(4));
        return res;
    }

    private static int getSrcValue(char ch) {
        int i;
        char[] cTable = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M',
                'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'W', 'X', 'Y'};
        for (i = 0; i < cTable.length; i++) {
            if (cTable[i] == ch) break;
        }
        return i;
    }

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