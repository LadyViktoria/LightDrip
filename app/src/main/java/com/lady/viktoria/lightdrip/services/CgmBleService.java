package com.lady.viktoria.lightdrip.services;

import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.lady.viktoria.lightdrip.RealmActions.TransmitterRecord;
import com.lady.viktoria.lightdrip.utils.ConvertHexString;
import com.lady.viktoria.lightdrip.utils.ConvertTxID;
import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.internal.RxBleLog;
import com.polidea.rxandroidble.utils.ConnectionSharingAdapter;

import net.grandcentrix.tray.AppPreferences;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;


public class CgmBleService extends Service {
    private final static String TAG = CgmBleService.class.getSimpleName();

    public final static UUID UUID_BG_MEASUREMENT = UUID.fromString(GattAttributes.HM_RX_TX);
    public final static String ACTION_BLE_CONNECTED = "ACTION_BLE_CONNECTED";
    public final static String ACTION_BLE_DISCONNECTED = "ACTION_BLE_DISCONNECTED";
    public final static String ACTION_BLE_DATA_AVAILABLE = "ACTION_BLE_DATA_AVAILABLE";
    public final static String EXTRA_BLE_DATA = "EXTRA_BLE_DATA";
    public final static String BEACON_SNACKBAR = "BEACON_SNACKBAR";

    private RxBleClient rxBleClient;
    private RxBleDevice bleDevice;
    private AppPreferences mTrayPreferences;
    private PublishSubject<Void> disconnectTriggerSubject = PublishSubject.create();
    private Observable<RxBleConnection> connectionObservable;
    Handler handler;
    Subscription writeNotificationSubscription;
    Subscription writeCharacteristicSubscription;
    Timer timer;
    TimerTask backtask;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        handler = new Handler();
        startJobScheduler();
        //timerTask();

        // get mac address from selected wixelbridge
        mTrayPreferences = new AppPreferences(this);
        final String BTDeviceAddress = mTrayPreferences.getString("BT_MAC_Address", "00:00:00:00:00:00");

        //init rxBleClient
        rxBleClient = RxBleClient.create(this);
        bleDevice = rxBleClient.getBleDevice(BTDeviceAddress);
        // logging for RxBleClient
        RxBleClient.setLogLevel(RxBleLog.INFO);
        connectionObservable = bleDevice
                .establishConnection(true)
                //.takeUntil(disconnectTriggerSubject)
                .doOnUnsubscribe(this::clearSubscription)
                .compose(new ConnectionSharingAdapter());

        bleDevice.observeConnectionStateChanges()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onConnectionStateChange);

        connect();
        return START_STICKY;
    }

    public void connect() {
        if (isConnected()) {
            triggerDisconnect();
        } else {
            connectionObservable.subscribe(this::onConnectionReceived, this::onConnectionFailure);
        }
    }

    public void writeCharacteristic(final ByteBuffer byteBuffer) {
        byte[] bytearray = byteBuffer.array();
        if (isConnected()) {
            writeCharacteristicSubscription = connectionObservable
                    .flatMap(rxBleConnection -> rxBleConnection
                            .writeCharacteristic(UUID_BG_MEASUREMENT, bytearray))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bytes -> onWriteSuccess(), this::onWriteFailure);
        }
    }

    public void writeNotificationCharacteristic() {
        if (isConnected()) {
            writeNotificationSubscription = connectionObservable
                    .flatMap(rxBleConnection -> rxBleConnection.setupNotification(UUID_BG_MEASUREMENT))
                    .doOnNext(notificationObservable -> runOnUiThread(this::notificationHasBeenSetUp))
                    .flatMap(notificationObservable -> notificationObservable)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onNotificationReceived, this::onNotificationSetupFailure);
        }
    }

    private void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
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

    private void onConnectionStateChange(RxBleConnection.RxBleConnectionState newState) {
        if (newState == RxBleConnection.RxBleConnectionState.CONNECTING) {
            Log.v(TAG, "connectionstat CONNECTING");
        }
        if (newState == RxBleConnection.RxBleConnectionState.DISCONNECTING) {
            Log.v(TAG, "connectionstat DISCONNECTING");
        }
        if (newState == RxBleConnection.RxBleConnectionState.CONNECTED) {
            Log.v(TAG, "connectionstat CONNECTED");
            broadcastUpdate(ACTION_BLE_CONNECTED);
            try {
                backtask.cancel();
                timerTask();
            } catch (Exception e) {
                Log.v(TAG, "connectionstat CONNECTED" + e.getMessage());
            }
        }
        if (newState == RxBleConnection.RxBleConnectionState.DISCONNECTED) {
            Log.v(TAG, "connectionstat DISCONNECTED");
            broadcastUpdate(ACTION_BLE_DISCONNECTED);
            try {
                backtask.cancel();
                timerTask();
            } catch (Exception e) {
                Log.v(TAG, "connectionstat DISCONNECTED" + e.getMessage());
            }
        }
    }

    private void onConnectionFailure(Throwable throwable) {
        //noinspection ConstantConditions
        Log.v(TAG, "Connection Failure");
        writeNotificationSubscription.unsubscribe();
        writeNotificationSubscription.unsubscribe();
        try {
            backtask.cancel();
            timerTask();
        } catch (Exception e) {
            Log.v(TAG, "onConnectionFailure " + e.getMessage());
        }
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
        try {
            backtask.cancel();
            timerTask();
        } catch (Exception e) {
            Log.v(TAG, "onWriteFailure " + e.getMessage());
        }
    }

    private void onNotificationReceived(byte[] bytes) {
        //noinspection ConstantConditions
        Log.v(TAG, "Change: "  + ConvertHexString.bytesToHex(bytes));
        long timestamp = new Date().getTime();
        int packatlength = bytes[0];
        if (packatlength >= 2) {
            if (CheckTransmitterID(bytes, bytes.length)) {
                TransmitterRecord.create(bytes, bytes.length, timestamp);
            } else {
                broadcastUpdate(BEACON_SNACKBAR);
            }
        } else if (packatlength <= 1) {
            writeAcknowledgePacket();
        }
    }

    private void onNotificationSetupFailure(Throwable throwable) {
        //noinspection ConstantConditions
        Log.v(TAG, "Notifications error: " + throwable);
        try {
            backtask.cancel();
            timerTask();
        } catch (Exception e) {
            Log.v(TAG, "onNotificationSetupFailure " + e.getMessage());
        }
    }

    private void notificationHasBeenSetUp() {
        //noinspection ConstantConditions
        Log.v(TAG, "Notifications has been set up");
    }

    public boolean CheckTransmitterID(byte[] packet, int len) {
        int DexSrc;
        int TransmitterID;
        ByteBuffer tmpBuffer;
        final String TxId = mTrayPreferences.getString("Transmitter_Id", "00000");
        TransmitterID = ConvertTxID.convertSrc(TxId);

        tmpBuffer = ByteBuffer.allocate(len);
        tmpBuffer.order(ByteOrder.LITTLE_ENDIAN);
        tmpBuffer.put(packet, 0, len);

        if (packet[0] == 7) {
            Log.i(TAG, "Received Beacon packet.");
            broadcastUpdate(BEACON_SNACKBAR);
            writeTxIdPacket(TransmitterID);
            return false;
        } else if (packet[0] >= 21 && packet[1] == 0) {
            Log.i(TAG, "Received Data packet");
            DexSrc = tmpBuffer.getInt(12);
            TransmitterID = ConvertTxID.convertSrc(TxId);
            if (Integer.compare(DexSrc, TransmitterID) != 0) {
                writeTxIdPacket(TransmitterID);
                return false;
            } else {
                return true;
            }
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

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    public void startJobScheduler() {
        final long REFRESH_INTERVAL = 15 * 60 * 1000;
        ComponentName serviceComponent = new ComponentName(this, SchedulerJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setRequiresDeviceIdle(false);
        builder.setRequiresCharging(false);
        builder.setPeriodic(REFRESH_INTERVAL);
        //builder.setPersisted(true);
        JobScheduler jobScheduler = (JobScheduler) this.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int result = jobScheduler.schedule(builder.build());
        if (result == JobScheduler.RESULT_SUCCESS) Log.d(TAG, "Job scheduled successfully!");
    }

    public void stopJobScheduler() {
        JobScheduler jobScheduler = (JobScheduler) this.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(0);
    }

    public void timerTask() {
        timer = new Timer();
        backtask = new TimerTask() {
            @Override
            public void run() {
                handler.post(() -> {
                    try {
                        connect();
                        Log.d("check","Check Run" );
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                    }
                });
            }
        };
        timer.schedule(backtask , 0, 4 * 60 * 1000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent("com.lady.viktoria.lightdrip.services.RestartCgmBleService");
        sendBroadcast(broadcastIntent);
        stopJobScheduler();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
