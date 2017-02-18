package com.lady.viktoria.lightdrip.ImportedLibraries.dexcom;

// This code and this particular library are from the NightScout android uploader
// Check them out here: https://github.com/nightscout/android-uploader
// Some of this code may have been modified for use in this project

public class Constants {

    public final static int EGV_VALUE_MASK = 1023;
    public final static int EGV_TREND_ARROW_MASK = 15;
    public final static int EGV_NOISE_MASK = 112;


    public enum TREND_ARROW_VALUES {
        NONE(0),
        DOUBLE_UP(1, "\u21C8", "DoubleUp"),
        SINGLE_UP(2, "\u2191", "SingleUp"),
        UP_45(3, "\u2197", "FortyFiveUp"),
        FLAT(4, "\u2192", "Flat"),
        DOWN_45(5, "\u2198", "FortyFiveDown"),
        SINGLE_DOWN(6, "\u2193", "SingleDown"),
        DOUBLE_DOWN(7, "\u21CA", "DoubleDown"),
        NOT_COMPUTABLE(8, "", "NOT_COMPUTABLE"),
        OUT_OF_RANGE(9, "", "OUT_OF_RANGE");

        private String arrowSymbol;
        private String trendName;
        private int myID;

        TREND_ARROW_VALUES(int id, String a, String t) {
            myID = id;
            arrowSymbol = a;
            trendName = t;
        }

        TREND_ARROW_VALUES(int id) {
            this(id, null, null);
        }

        public String Symbol() {
            if (arrowSymbol == null) {
                return "\u2194";
            } else {
                return arrowSymbol;
            }
        }
    }
}
