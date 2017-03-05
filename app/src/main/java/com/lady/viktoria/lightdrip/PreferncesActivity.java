package com.lady.viktoria.lightdrip;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.lady.viktoria.lightdrip.services.CgmBleService;

import net.grandcentrix.tray.AppPreferences;

public class PreferncesActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final static String TAG = PreferncesActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        final AppPreferences appPreferences = new AppPreferences(this);
        if (key.equals("bt_device")) {
            ListPreference listPref = (ListPreference) getPreference("bt_device");
            String BTDevice = (String) listPref.getEntry();
            String BTDeviceArray[] = BTDevice.split("\\r?\\n");
            appPreferences.put("BT_Name", BTDeviceArray[0]);
            appPreferences.put("BT_MAC_Address", BTDeviceArray[1]);
            stopService(new Intent(this, CgmBleService.class));
        }
        if (key.equals("transmitter_id")) {
            int txidlength = sharedPreferences.getString("transmitter_id", "00000").length();
            if (txidlength != 5) {
                EditTextPreference mEditTextPreference = ((EditTextPreference) getPreferenceScreen()
                        .findPreference("transmitter_id"));
                mEditTextPreference.setText("00000");
                Toast.makeText(this, "Should be 5 digits and not " + txidlength, Toast.LENGTH_LONG).show();
                return;
            }
            String txid = sharedPreferences.getString("transmitter_id", "00000");
            appPreferences.put("Transmitter_Id", txid);
            stopService(new Intent(this, CgmBleService.class));
        }
    }

    public Preference getPreference(String key) {
        return findPreference(key);
    }
}