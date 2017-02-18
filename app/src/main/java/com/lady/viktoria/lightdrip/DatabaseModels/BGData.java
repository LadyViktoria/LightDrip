package com.lady.viktoria.lightdrip.DatabaseModels;

import io.realm.RealmObject;

public class BGData extends RealmObject {

    private Double a;
    public double geta() {
        return a;
    }
    public void seta(final double a) {
        this.a = a;
    }

    private Double ageAdjustedRawValue;
    public double getageAdjustedRawValue() {
        return ageAdjustedRawValue;
    }
    public void setageAdjustedRawValue(final double ageAdjustedRawValue) {
        this.ageAdjustedRawValue = ageAdjustedRawValue;
    }

    private Double b;
    public Double getB() {
        return b;
    }
    public void setB(Double b) {
        this.b = b;
    }

    private Double c;
    public Double getC() {
        return c;
    }
    public void setC(Double c) {
        this.c = c;
    }

    private Double calculatedValue;
    public Double getCalculatedValue() {
        return calculatedValue;
    }
    public void setCalculatedValue(Double calculatedValue) {
        this.calculatedValue = calculatedValue;
    }

    private Double calculatedValueSlope;
    public Double getCalculatedValueSlope() {
        return calculatedValueSlope;
    }
    public void setCalculatedValueSlope(Double calculatedValueSlope) {
        this.calculatedValueSlope = calculatedValueSlope;
    }

    private Boolean calibrationFlag;
    public Boolean getCalibrationFlag() {
        return calibrationFlag;
    }
    public void setCalibrationFlag(Boolean calibrationFlag) {
        this.calibrationFlag = calibrationFlag;
    }

    private String calibrationUuid;
    public String getCalibrationUuid() {
        return calibrationUuid;
    }
    public void setCalibrationUuid(String calibrationUuid) {
        this.calibrationUuid = calibrationUuid;
    }

    private Double filteredData;
    public Double getFilteredData() {
        return filteredData;
    }
    public void setFilteredData(Double filteredData) {
        this.filteredData = filteredData;
    }

    private Double ra;
    public Double getRa() {
        return ra;
    }
    public void setRa(Double ra) {
        this.ra = ra;
    }

    private Double rawData;
    public Double getRawData() {
        return rawData;
    }
    public void setRawData(Double rawData) {
        this.rawData = rawData;
    }

    private Double rb;
    public Double getRb() {
        return rb;
    }
    public void setRb(Double rb) {
        this.rb = rb;
    }

    private Double rc;
    public Double getRc() {
        return rc;
    }
    public void setRc(Double rc) {
        this.rc = rc;
    }

    private String sensorUuid;
    public String getSensorUuid() {
        return sensorUuid;
    }
    public void setSensorUuid(String sensorUuid) {
        this.sensorUuid = sensorUuid;
    }

    private Double timeSinceSensorStarted;
    public Double getTimeSinceSensorStarted() {
        return timeSinceSensorStarted;
    }
    public void setTimeSinceSensorStarted(Double timeSinceSensorStarted) {
        this.timeSinceSensorStarted = timeSinceSensorStarted;
    }

    private double timestamp;
    public double getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(double timestamp) {
        this.timestamp = timestamp;
    }

    private String uuid;
    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

}