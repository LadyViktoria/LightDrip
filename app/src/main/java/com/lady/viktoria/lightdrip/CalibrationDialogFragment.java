package com.lady.viktoria.lightdrip;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.lady.viktoria.lightdrip.RealmActions.CalibrationRecord;
import com.lady.viktoria.lightdrip.RealmActions.SensorRecord;

public class CalibrationDialogFragment extends DialogFragment {
    private final static String TAG = CalibrationDialogFragment.class.getSimpleName();

    Switch sButton;
    Button calButton;
    EditText glucosereading1, glucosereading2;
    Boolean doubleCalFlag = false;
    CalibrationRecord calibration = new CalibrationRecord();



    public CalibrationDialogFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_calibrationdialog, container, false);
        getDialog().setTitle("Calibration Dialog");
        glucosereading1 = (EditText) view.findViewById(R.id.glucosereading1);
        glucosereading2 = (EditText) view.findViewById(R.id.glucosereading2);
        sButton = (Switch) view.findViewById(R.id.switch_doublecalibration);
        calButton = (Button) view.findViewById(R.id.btn_addcalibration);

        calButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (doubleCalFlag) {
                    double bg1 = Double.parseDouble(String.valueOf(glucosereading1.getText()));
                    double bg2 = Double.parseDouble(String.valueOf(glucosereading2.getText()));
                    calibration.initialCalibration(bg1, bg2);
                } else {
                    double bg1 = Double.parseDouble(String.valueOf(glucosereading1.getText()));
                }
            }
        });

        sButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton cb, boolean on) {
                if (on) {
                    sButton.setText("Double Calibration");
                    glucosereading2.setVisibility(View.VISIBLE);
                    doubleCalFlag = true;
                } else {
                    sButton.setText("Single Calibration");
                    glucosereading2.setVisibility(View.GONE);
                    doubleCalFlag = false;
                }
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
