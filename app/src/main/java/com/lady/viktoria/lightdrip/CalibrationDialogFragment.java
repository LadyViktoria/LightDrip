package com.lady.viktoria.lightdrip;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import com.lady.viktoria.lightdrip.RealmActions.CalibrationRecord;
import com.lady.viktoria.lightdrip.RealmActions.GlucoseRecord;
import com.lady.viktoria.lightdrip.RealmModels.CalibrationData;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.realm.Realm;

import static io.realm.Realm.getDefaultInstance;


public class CalibrationDialogFragment extends DialogFragment {
    private final static String TAG = CalibrationDialogFragment.class.getSimpleName();

    private Boolean doubleCalFlag = false;
    private Realm mRealm;
    @BindView(R.id.glucosereading1) EditText glucosereading1;
    @BindView(R.id.glucosereading2) EditText glucosereading2;
    @BindView(R.id.btn_addcalibration) Button calButton;
    @BindView(R.id.switch_doublecalibration) Switch sButton;
    private Unbinder unbinder;


    public CalibrationDialogFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_calibrationdialog, container, false);
        ButterKnife.bind(this, view);
        Realm.init(getActivity());
        mRealm = getDefaultInstance();


        GlucoseRecord glucoserecord = new GlucoseRecord();
        CalibrationData calibrationRecords = mRealm.where(CalibrationData.class).findFirst();
        if (calibrationRecords == null && glucoserecord.countRecordsByLastSensorID() >= 2) {
            sButton.setChecked(true);
            glucosereading2.setVisibility(View.VISIBLE);
            sButton.setEnabled(false);
            sButton.setText("Double Calibration");
            doubleCalFlag = true;
        }

        calButton.setOnClickListener(view1 -> {
            if (doubleCalFlag) {
                try {
                    double bg1 = Double.parseDouble(String.valueOf(glucosereading1.getText()));
                    double bg2 = Double.parseDouble(String.valueOf(glucosereading2.getText()));
                    CalibrationRecord calibration = new CalibrationRecord();
                    calibration.initialCalibration(bg1, bg2);
                } catch (Exception e) {
                    Log.v(TAG, "setOnClickListener " + e.getMessage());
                } finally {
                    dismiss();
                }
            } else {
                try {
                    double bg1 = Double.parseDouble(String.valueOf(glucosereading1.getText()));
                    CalibrationRecord calibration = new CalibrationRecord();
                    calibration.singleCalibration(bg1);
                } catch (Exception e) {
                    Log.v(TAG, "setOnClickListener " + e.getMessage());
                } finally {
                    dismiss();
                }
            }
        });

        sButton.setOnCheckedChangeListener((cb, on) -> {
            if (on) {
                sButton.setText("Double Calibration");
                glucosereading2.setVisibility(View.VISIBLE);
                doubleCalFlag = true;
            } else {
                sButton.setText("Single Calibration");
                glucosereading2.setVisibility(View.GONE);
                doubleCalFlag = false;
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

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
