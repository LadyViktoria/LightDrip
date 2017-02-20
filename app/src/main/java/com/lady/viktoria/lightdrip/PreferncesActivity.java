package com.lady.viktoria.lightdrip;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

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

        if (key.equals("bt_device")) {
            ListPreference listPref = (ListPreference) getPreference("bt_device");
            String BTDevice = (String) listPref.getEntry();
            String BTDeviceArray[] = BTDevice.split("\\r?\\n");
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("BT_Name", BTDeviceArray[0]);
            editor.putString("BT_MAC_Address", BTDeviceArray[1]);
            editor.apply();
        }
        if (key.equals("transmitter_id")) {
            String txid = sharedPreferences.getString("transmitter_id", "00000");
            Log.v(TAG, txid);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("Transmitter_Id", txid);
            editor.apply();
        }
    }

    public Preference getPreference (String key) {
        return findPreference(key);
    }
}

