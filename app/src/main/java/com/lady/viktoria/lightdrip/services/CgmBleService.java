package com.lady.viktoria.lightdrip.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;

import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.internal.RxBleLog;


import net.grandcentrix.tray.AppPreferences;

import rx.Subscription;


public class CgmBleService extends Service {
    private final static String TAG = CgmBleService.class.getSimpleName();


    private RxBleClient rxBleClient;
    private RxBleDevice bleDevice;
    private AppPreferences mTrayPreferences;
    private Subscription connectionSubscription;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        rxBleClient = RxBleClient.create(this);
        RxBleClient.setLogLevel(RxBleLog.VERBOSE);
        mTrayPreferences = new AppPreferences(this);
        bleDevice = rxBleClient.getBleDevice(getBTDeviceMAC());


        Subscription subscription = bleDevice.establishConnection(this, true) // <-- autoConnect flag
                .subscribe(rxBleConnection -> {
                    Log.v(TAG, "rxBleConnection: " + rxBleConnection);
                });
        // When done... unsubscribe and forget about connection teardown :)
        subscription.unsubscribe();

        if (isConnected()) {
            Log.v(TAG, "isConnected: " + isConnected());

        } else {

        }


        return START_STICKY;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private String getBTDeviceMAC() {
        final String BTDeviceAddress = mTrayPreferences.getString("BT_MAC_Address", "00:00:00:00:00:00");
        Log.v(TAG, "BTDeviceAddress: " + BTDeviceAddress);

        return BTDeviceAddress;
    }

    private void connect() {
    }



    private boolean isConnected() {
        return bleDevice.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED;
    }
}
