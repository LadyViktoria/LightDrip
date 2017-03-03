package com.lady.viktoria.lightdrip;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.lady.viktoria.lightdrip.RealmActions.CalibrationRecord;
import com.lady.viktoria.lightdrip.RealmActions.GlucoseRecord;
import com.lady.viktoria.lightdrip.RealmConfig.RealmBaseDialogFragment;
import com.lady.viktoria.lightdrip.RealmModels.CalibrationData;

import io.realm.Realm;

import static io.realm.Realm.getInstance;


public class CalibrationDialogFragment extends RealmBaseDialogFragment {
    private final static String TAG = CalibrationDialogFragment.class.getSimpleName();

    private Switch sButton;
    private Button calButton;
    private EditText glucosereading1, glucosereading2;
    private Boolean doubleCalFlag = false;
    private Realm mRealm;
    private GlucoseRecord glucoserecord;
    private CalibrationRecord calibration;
    public CalibrationDialogFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        glucoserecord = new GlucoseRecord();
        calibration = new CalibrationRecord();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_calibrationdialog, container, false);
        glucosereading1 = (EditText) view.findViewById(R.id.glucosereading1);
        glucosereading2 = (EditText) view.findViewById(R.id.glucosereading2);
        sButton = (Switch) view.findViewById(R.id.switch_doublecalibration);
        calButton = (Button) view.findViewById(R.id.btn_addcalibration);
        Realm.init(getActivity());
        mRealm = getInstance(getRealmConfig());

        CalibrationData calibrationRecords = mRealm.where(CalibrationData.class).findFirst();
        if(calibrationRecords == null && glucoserecord.countRecordsByLastSensorID() >= 2){
            sButton.setChecked(true);
            glucosereading2.setVisibility(View.VISIBLE);
            sButton.setEnabled(false);
            sButton.setText("Double Calibration");
            doubleCalFlag = true;
        }

        calButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (doubleCalFlag) {
                    try {
                        double bg1 = Double.parseDouble(String.valueOf(glucosereading1.getText()));
                        double bg2 = Double.parseDouble(String.valueOf(glucosereading2.getText()));
                        calibration.initialCalibration(bg1, bg2);
                    } catch (Exception e) {
                        Log.v(TAG, "setOnClickListener " + e.getMessage());
                    } finally {
                        dismiss();
                    }
                } else {
                    try {
                        double bg1 = Double.parseDouble(String.valueOf(glucosereading1.getText()));
                        calibration.singleCalibration(bg1);
                    } catch (Exception e) {
                        Log.v(TAG, "setOnClickListener " + e.getMessage());
                    } finally {
                        dismiss();
                    }
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
