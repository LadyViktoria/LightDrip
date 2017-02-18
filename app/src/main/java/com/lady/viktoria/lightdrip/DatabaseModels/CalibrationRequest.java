package com.lady.viktoria.lightdrip.DatabaseModels;

import io.realm.RealmObject;

public class CalibrationRequest extends RealmObject {

    private double requestIfAbove;
    public double getrequestIfAbove() {
        return requestIfAbove;
    }
    public void setrequestIfAbove(final double requestIfAbove) {
        this.requestIfAbove = requestIfAbove;
    }

    private double requestIfBelow;
    public double getrequestIfBelow() {
        return requestIfBelow;
    }
    public void setrequestIfBelow(final double requestIfBelow) {
        this.requestIfBelow = requestIfBelow;
    }

}
