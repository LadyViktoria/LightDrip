package com.lady.viktoria.lightdrip;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lady.viktoria.lightdrip.DatabaseModels.SensorData;
import com.lady.viktoria.lightdrip.RealmConfig.RealmBaseFragment;
import com.lady.viktoria.lightdrip.RealmSerialize.SensorDataSerializer;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

import static io.realm.Realm.getInstance;

public class SensorActionFragment extends RealmBaseFragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private final static String TAG = SensorActionFragment.class.getSimpleName();

    public SensorActionFragment() {
    }

    Calendar SensorStart;
    int mYear, mMonthOfYear, mDayOfMonth, mHourOfDay, mMinute;
    private Realm mRealm;
    private Gson gson;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sensoraction, container, false);
        SensorStart = Calendar.getInstance();
        Realm.init(getActivity());
        mRealm = getInstance(getRealmConfig());
        SensorDialog();
        try {
            serializeToJson();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        currentSensor();
        return view;
    }

    protected void SensorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Please choose if you want to stop current Sensor or start a new Sensor");
        builder.setTitle("Sensor Actions");
        builder.setPositiveButton("Start Sensor", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!isSensorActive()) {
                    StartSensor();
                    dialog.dismiss();
                } else {
                    Snackbar.make(getView(), "Please stop current Sensor first!", Snackbar.LENGTH_LONG).show();
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Stop sensor", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                StopSensor();
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void StartSensor() {
            Calendar now = Calendar.getInstance();
            DatePickerDialog dpd = DatePickerDialog.newInstance(
                    SensorActionFragment.this,
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            );
            dpd.show(getFragmentManager(), "Datepickerdialog");
    }

    private void StopSensor() {
        try {
            long stopped_at = new Date().getTime();
            RealmResults<SensorData> results = mRealm.where(SensorData.class).findAll();
            String lastUUID = results.last().getuuid();
            SensorData mSensorData = mRealm.where(SensorData.class).equalTo("uuid", lastUUID).findFirst();
            mRealm.beginTransaction();
            mSensorData.setstopped_at(stopped_at);
            mRealm.commitTransaction();
            mRealm.close();
        } catch (Exception e) {
            Log.v(TAG, "stopSensor try_set_realm_obj " + e.getMessage());
        }
    }

    private void currentSensor() {
        if (isSensorActive()) {
            try {
                RealmResults<SensorData> results = mRealm.where(SensorData.class).findAll();
                String lastUUID = results.last().getuuid();
                SensorData mSensorData = mRealm.where(SensorData.class).equalTo("uuid", lastUUID).findFirst();
                //get realm object
                Log.v(TAG, "currentSensor realm object" + String.valueOf(mSensorData));
                // transform into json
                String Json = gson.toJson(mRealm.copyFromRealm(mSensorData));
                Log.v(TAG, "currentSensor json: "  + Json);
            } catch (Exception e) {
                Log.v(TAG, "currentSensor try_get_realm_obj " + e.getMessage());
            }
        } else {
            //Snackbar.make(getView(), "Please stop current Sensor fist!", Snackbar.LENGTH_LONG).show();
        }
    }

    private boolean isSensorActive() {
        try {
            RealmResults<SensorData> results = mRealm.where(SensorData.class).findAll();
            String lastUUID = results.last().getuuid();
            SensorData mSensorData = mRealm.where(SensorData.class).equalTo("uuid", lastUUID).findFirst();
            if (mSensorData.getstopped_at() == 0L) {
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.v(TAG, "isSensorActive try_get_realm_obj " + e.getMessage());
        }
        return false;
    }

    private void serializeToJson() throws ClassNotFoundException {
        gson = new GsonBuilder()
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getDeclaringClass().equals(RealmObject.class);
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .registerTypeAdapter(Class.forName("com.lady.viktoria.lightdrip.DatabaseModels.SensorData"), new SensorDataSerializer())
                .create();
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        mYear = year;
        mMonthOfYear = monthOfYear;
        mDayOfMonth = dayOfMonth;
        Calendar now = Calendar.getInstance();
        TimePickerDialog tpd = TimePickerDialog.newInstance(
                SensorActionFragment.this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true
        );
        tpd.show(getFragmentManager(), "Timepickerdialog");
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        mHourOfDay = Integer.parseInt(hourOfDay < 10 ? "0"+hourOfDay : ""+hourOfDay);
        mMinute = Integer.parseInt(minute < 10 ? "0"+minute : ""+minute);
        SensorStart.set(mYear, mMonthOfYear, mDayOfMonth, mHourOfDay, mMinute, 0);
        long startTime = SensorStart.getTime().getTime();
        String uuid = UUID.randomUUID().toString();
        try {
            mRealm.beginTransaction();
            SensorData mSensorData = mRealm.createObject(SensorData.class, uuid);
            mSensorData.setstarted_at(startTime);
            mRealm.commitTransaction();
            mRealm.close();
        } catch (Exception e) {
            Log.v(TAG, "onTimeSet try_set_realm_obj " + e.getMessage());
        }
    }
}