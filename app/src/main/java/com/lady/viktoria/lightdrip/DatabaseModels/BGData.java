package com.lady.viktoria.lightdrip.DatabaseModels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

public class BGData extends RealmObject {

    @SerializedName("a")
    @Expose
    private Double a;
    public double geta() {
        return a;
    }
    public void seta(final double a) {
        this.a = a;
    }

    @SerializedName("age_adjusted_raw_value")
    @Expose
    private Double ageAdjustedRawValue;
    public double getageAdjustedRawValue() {
        return ageAdjustedRawValue;
    }
    public void setageAdjustedRawValue(final double ageAdjustedRawValue) {
        this.ageAdjustedRawValue = ageAdjustedRawValue;
    }

    @SerializedName("b")
    @Expose
    private Double b;
    public Double getB() {
        return b;
    }
    public void setB(Double b) {
        this.b = b;
    }

    @SerializedName("c")
    @Expose
    private Double c;
    public Double getC() {
        return c;
    }
    public void setC(Double c) {
        this.c = c;
    }

    @SerializedName("calculated_value")
    @Expose
    private Double calculatedValue;
    public Double getCalculatedValue() {
        return calculatedValue;
    }
    public void setCalculatedValue(Double calculatedValue) {
        this.calculatedValue = calculatedValue;
    }

    @SerializedName("calculated_value_slope")
    @Expose
    private Double calculatedValueSlope;
    public Double getCalculatedValueSlope() {
        return calculatedValueSlope;
    }
    public void setCalculatedValueSlope(Double calculatedValueSlope) {
        this.calculatedValueSlope = calculatedValueSlope;
    }

    @SerializedName("calibration_flag")
    @Expose
    private Boolean calibrationFlag;
    public Boolean getCalibrationFlag() {
        return calibrationFlag;
    }
    public void setCalibrationFlag(Boolean calibrationFlag) {
        this.calibrationFlag = calibrationFlag;
    }

    @SerializedName("calibration_uuid")
    @Expose
    private String calibrationUuid;
    public String getCalibrationUuid() {
        return calibrationUuid;
    }
    public void setCalibrationUuid(String calibrationUuid) {
        this.calibrationUuid = calibrationUuid;
    }

    @SerializedName("filtered_data")
    @Expose
    private Double filteredData;
    public Double getFilteredData() {
        return filteredData;
    }
    public void setFilteredData(Double filteredData) {
        this.filteredData = filteredData;
    }

    @SerializedName("ra")
    @Expose
    private Double ra;
    public Double getRa() {
        return ra;
    }
    public void setRa(Double ra) {
        this.ra = ra;
    }

    @SerializedName("raw_data")
    @Expose
    private Double rawData;
    public Double getRawData() {
        return rawData;
    }
    public void setRawData(Double rawData) {
        this.rawData = rawData;
    }

    @SerializedName("rb")
    @Expose
    private Double rb;
    public Double getRb() {
        return rb;
    }
    public void setRb(Double rb) {
        this.rb = rb;
    }

    @SerializedName("rc")
    @Expose
    private Double rc;
    public Double getRc() {
        return rc;
    }
    public void setRc(Double rc) {
        this.rc = rc;
    }

    @SerializedName("sensor_uuid")
    @Expose
    private String sensorUuid;
    public String getSensorUuid() {
        return sensorUuid;
    }
    public void setSensorUuid(String sensorUuid) {
        this.sensorUuid = sensorUuid;
    }

    @SerializedName("time_since_sensor_started")
    @Expose
    private Double timeSinceSensorStarted;
    public Double getTimeSinceSensorStarted() {
        return timeSinceSensorStarted;
    }
    public void setTimeSinceSensorStarted(Double timeSinceSensorStarted) {
        this.timeSinceSensorStarted = timeSinceSensorStarted;
    }

    @SerializedName("timestamp")
    @Expose
    private double timestamp;
    public double getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(double timestamp) {
        this.timestamp = timestamp;
    }

    @SerializedName("uuid")
    @Expose
    private String uuid;
    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}