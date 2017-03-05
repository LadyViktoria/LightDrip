package com.lady.viktoria.lightdrip.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.lady.viktoria.lightdrip.utils;
import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.internal.RxBleLog;
import com.polidea.rxandroidble.utils.ConnectionSharingAdapter;

import net.grandcentrix.tray.AppPreferences;

import java.util.UUID;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public class CgmBleService extends Service {
    private final static String TAG = CgmBleService.class.getSimpleName();

    private RxBleClient rxBleClient;
    private RxBleDevice bleDevice;
    private AppPreferences mTrayPreferences;
    private PublishSubject<Void> disconnectTriggerSubject = PublishSubject.create();
    private Observable<RxBleConnection> connectionObservable;

    public final static UUID UUID_BG_MEASUREMENT =
            UUID.fromString(GattAttributes.HM_RX_TX);
    public final static UUID UUID_HM10_SERVICE =
            UUID.fromString(GattAttributes.HM_10_SERVICE);

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        // get mac address from selected wixelbridge
        mTrayPreferences = new AppPreferences(this);
        final String BTDeviceAddress = mTrayPreferences.getString("BT_MAC_Address", "00:00:00:00:00:00");

        //init rxBleClient
        rxBleClient = RxBleClient.create(this);
        bleDevice = rxBleClient.getBleDevice(BTDeviceAddress);
        // logging for RxBleClient
        RxBleClient.setLogLevel(RxBleLog.INFO);
        connectionObservable = prepareConnectionObservable();
        connect();

        return START_STICKY;
    }

    private Observable<RxBleConnection> prepareConnectionObservable() {
        return bleDevice
                .establishConnection(true)
                .takeUntil(disconnectTriggerSubject)
                //.compose(bindUntilEvent(PAUSE)
                //.doOnUnsubscribe(this::clearSubscription)
                .compose(new ConnectionSharingAdapter());
    }

    public void connect() {
        if (isConnected()) {
            //triggerDisconnect();
        } else {
            connectionObservable.subscribe(this::onConnectionReceived, this::onConnectionFailure);
        }
    }

    public void readCharacteristick() {
        if (isConnected()) {
            connectionObservable
                    .flatMap(rxBleConnection -> rxBleConnection.readCharacteristic(UUID_BG_MEASUREMENT))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bytes -> {
                        //readOutputView.setText(new String(bytes));
                        //readHexOutputView.setText(utils.bytesToHex(bytes));
                        //writeInput.setText(utils.bytesToHex(bytes));
                    }, this::onReadFailure);
        }
    }

    public void writeCharacteristic() {
        if (isConnected()) {
            connectionObservable
                    .flatMap(rxBleConnection -> rxBleConnection
                            .writeCharacteristic(UUID_BG_MEASUREMENT,
                                    getInputBytes(String.valueOf(UUID_BG_MEASUREMENT))))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bytes -> {
                        onWriteSuccess();
                    }, this::onWriteFailure);
        }
    }

    public void writeNotificationCharacteristic() {
        if (isConnected()) {
            connectionObservable
                    .flatMap(rxBleConnection -> rxBleConnection.setupNotification(UUID_BG_MEASUREMENT))
                    //.doOnNext(notificationObservable -> runOnUiThread(this::notificationHasBeenSetUp))
                    .flatMap(notificationObservable -> notificationObservable)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onNotificationReceived, this::onNotificationSetupFailure);
        }
    }

    private boolean isConnected() {
        return bleDevice.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED;
    }

    private void clearSubscription() {
        updateUI();
    }

    private void triggerDisconnect() {
        disconnectTriggerSubject.onNext(null);
    }

    private void updateUI() {
     //   connectButton.setText(isConnected() ? getString(R.string.disconnect) : getString(R.string.connect));
      //  readButton.setEnabled(isConnected());
      //  writeButton.setEnabled(isConnected());
       // notifyButton.setEnabled(isConnected());
    }

    private byte[] getInputBytes(String uuid) {
        return utils.hexToBytes(uuid);
    }

    private void onConnectionFailure(Throwable throwable) {
        //noinspection ConstantConditions
        Log.v(TAG, "Connection Failure");
        connect();
    }

    private void onConnectionReceived(RxBleConnection connection) {
        //noinspection ConstantConditions
        Log.v(TAG, "Hey, connection has been established!");
        writeNotificationCharacteristic();
    }

    private void onReadFailure(Throwable throwable) {
        //noinspection ConstantConditions
        Log.v(TAG, "Read error: " + throwable);
    }

    private void onWriteSuccess() {
        //noinspection ConstantConditions
        Log.v(TAG, "Write success");
    }

    private void onWriteFailure(Throwable throwable) {
        //noinspection ConstantConditions
        Log.v(TAG, "Write error: " + throwable);
    }

    private void onNotificationReceived(byte[] bytes) {
        //noinspection ConstantConditions
        Log.v(TAG, "Change: "  + utils.bytesToHex(bytes));
    }

    private void onNotificationSetupFailure(Throwable throwable) {
        //noinspection ConstantConditions
        Log.v(TAG, "Notifications error: " + throwable);
    }

    private void notificationHasBeenSetUp() {
        //noinspection ConstantConditions
        Log.v(TAG, "Notifications has been set up");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent("com.lady.viktoria.lightdrip.services.RestartCgmBleService");
        sendBroadcast(broadcastIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
