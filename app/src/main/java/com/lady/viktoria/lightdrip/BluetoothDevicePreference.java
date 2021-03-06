package com.lady.viktoria.lightdrip;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

import java.util.Set;

public class BluetoothDevicePreference extends ListPreference {

    public BluetoothDevicePreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
        Integer size = 0;
        if (bta != null) {
            size += bta.getBondedDevices().size();
        }
        CharSequence[] entries = new CharSequence[size];
        int i = 0;
        if (bta != null) {
            Set<BluetoothDevice> pairedDevices = bta.getBondedDevices();
            for (BluetoothDevice dev : pairedDevices) {
                entries[i] = dev.getName() + "\n" + dev.getAddress();
                i++;
            }
        }
        setEntries(entries);
        setEntryValues(entries);
    }

    public BluetoothDevicePreference(Context context) {
        this(context, null);
    }
}