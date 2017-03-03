package com.lady.viktoria.lightdrip;

import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lady.viktoria.lightdrip.RealmActions.SensorRecord;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;

public class SensorActionFragment extends Fragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private final static String TAG = SensorActionFragment.class.getSimpleName();

    public SensorActionFragment() {
    }

    Calendar SensorStart;
    int mYear, mMonthOfYear, mDayOfMonth, mHourOfDay, mMinute;
    SensorRecord sensorRecord;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sensoraction, container, false);
        SensorStart = Calendar.getInstance();
        sensorRecord = new SensorRecord();
        SensorDialog();
        return view;
    }

    protected void SensorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Please choose if you want to stop current Sensor or start a new Sensor");
        builder.setTitle("Sensor Actions");
        builder.setPositiveButton("Start Sensor", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!sensorRecord.isSensorActive()) {
                    Calendar now = Calendar.getInstance();
                    DatePickerDialog dpd = DatePickerDialog.newInstance(
                            SensorActionFragment.this,
                            now.get(Calendar.YEAR),
                            now.get(Calendar.MONTH),
                            now.get(Calendar.DAY_OF_MONTH)
                    );
                    dpd.show(getFragmentManager(), "Datepickerdialog");
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
                sensorRecord.StopSensor();
                dialog.dismiss();
            }
        });
        builder.create().show();
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
        sensorRecord.StartSensor(startTime);
    }
}