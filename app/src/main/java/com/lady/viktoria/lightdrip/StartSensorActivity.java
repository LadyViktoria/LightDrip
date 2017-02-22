package com.lady.viktoria.lightdrip;


import android.content.Context;
import android.os.Bundle;

import com.lady.viktoria.lightdrip.RealmConfig.RealmBaseActivity;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;

public class StartSensorActivity extends RealmBaseActivity implements DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {

    public StartSensorActivity(Context applicationContext) {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StartSensor();
    }

    public void StartSensor() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                StartSensorActivity.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)

        );
        dpd.show(getFragmentManager(), "Datepickerdialog");
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        String time = "You picked the following time: "+hourOfDay+"h"+minute+"m"+second;

    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String date = "You picked the following date: "+dayOfMonth+"/"+(monthOfYear+1)+"/"+year;
        //dateTextView.setText(date);
        Calendar now = Calendar.getInstance();
        TimePickerDialog timepickerdialog = TimePickerDialog.newInstance(StartSensorActivity.this,
                now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
        timepickerdialog.show(getFragmentManager(), "Datepickerdialog");


    }
}
