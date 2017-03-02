package com.lady.viktoria.lightdrip;

import android.app.DialogFragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

public class CalibrationDialogFragment extends DialogFragment {
    private final static String TAG = CalibrationDialogFragment.class.getSimpleName();

    private CalibrationDialogFragment.OnFragmentInteractionListener mListener;

    Switch sButton;
    EditText glucosereading1, glucosereading2;


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
        getDialog().setTitle("Calibration Dialog");
        glucosereading2 = (EditText) view.findViewById(R.id.glucosereading2);
        sButton = (Switch) view.findViewById(R.id.switch_doublecalibration);
        sButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton cb, boolean on){
                if(on) {
                    sButton.setText("Double Calibration");
                    glucosereading2.setVisibility(View.VISIBLE);
                } else {
                    sButton.setText("Single Calibration");
                    glucosereading2.setVisibility(View.GONE);
                }
            }
        });
        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
