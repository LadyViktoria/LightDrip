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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

import static com.lady.viktoria.lightdrip.utils.convertSrc;

public class CgmBleService extends Service {
    private final static String TAG = CgmBleService.class.getSimpleName();

    private RxBleClient rxBleClient;
    private RxBleDevice bleDevice;
    private AppPreferences mTrayPreferences;
    private PublishSubject<Void> disconnectTriggerSubject = PublishSubject.create();
    private Observable<RxBleConnection> connectionObservable;

    public final static UUID UUID_BG_MEASUREMENT =
            UUID.fromString(GattAttributes.HM_RX_TX);

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

    public void writeCharacteristic(final ByteBuffer byteBuffer) {
        byte[] bytearray = byteBuffer.array();
        if (isConnected()) {
            connectionObservable
                    .flatMap(rxBleConnection -> rxBleConnection
                            .writeCharacteristic(UUID_BG_MEASUREMENT, bytearray))
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

    public boolean CheckTransmitterID(byte[] packet, int len) {
        int DexSrc;
        int TransmitterID;
        ByteBuffer tmpBuffer;
        final String TxId = mTrayPreferences.getString("Transmitter_Id", "00000");
        TransmitterID = convertSrc(TxId);

        tmpBuffer = ByteBuffer.allocate(len);
        tmpBuffer.order(ByteOrder.LITTLE_ENDIAN);
        tmpBuffer.put(packet, 0, len);

        if (packet[0] == 7) {
            Log.i(TAG, "Received Beacon packet.");
            //String intentAction = BEACON_SNACKBAR;
            //broadcastUpdate(intentAction);
            writeTxIdPacket(TransmitterID);
            return false;
        } else if (packet[0] >= 21 && packet[1] == 0) {
            Log.i(TAG, "Received Data packet");
            DexSrc = tmpBuffer.getInt(12);
            TransmitterID = convertSrc(TxId);
            if (Integer.compare(DexSrc, TransmitterID) != 0) {
                writeTxIdPacket(TransmitterID);
                return false;
            } else {return true;}
        }
        return false;
    }

    private void writeTxIdPacket(int TransmitterID) {
        Log.v(TAG, "try to set transmitter ID");
        ByteBuffer txidMessage = ByteBuffer.allocate(6);
        txidMessage.order(ByteOrder.LITTLE_ENDIAN);
        txidMessage.put(0, (byte) 0x06);
        txidMessage.put(1, (byte) 0x01);
        txidMessage.putInt(2, TransmitterID);
        writeCharacteristic(txidMessage);
    }

    private void writeAcknowledgePacket() {
        Log.d(TAG, "Sending Acknowledge Packet, to put wixel to sleep");
        ByteBuffer ackMessage = ByteBuffer.allocate(2);
        ackMessage.put(0, (byte) 0x02);
        ackMessage.put(1, (byte) 0xF0);
        writeCharacteristic(ackMessage);
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
