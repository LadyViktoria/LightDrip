package com.lady.viktoria.lightdrip.services;

import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.lady.viktoria.lightdrip.RealmActions.TransmitterRecord;

import net.grandcentrix.tray.AppPreferences;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.UUID;

import static android.bluetooth.BluetoothAdapter.STATE_DISCONNECTING;
import static com.lady.viktoria.lightdrip.utils.convertSrc;

public class BGMeterGattService extends Service {
    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    public final static String BEACON_SNACKBAR =
            "com.example.bluetooth.le.BEACON_SNACKBAR";
    public final static UUID UUID_BG_MEASUREMENT =
            UUID.fromString(GattAttributes.HM_RX_TX);
    public final static UUID UUID_HM10_SERVICE =
            UUID.fromString(GattAttributes.HM_10_SERVICE);
    private final static String TAG = BGMeterGattService.class.getSimpleName();
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private final IBinder mBinder = new LocalBinder();
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothDevice mBluetoothDevice;
    private int mConnectionState = STATE_DISCONNECTED;
    private Context mContext;
    private AppPreferences mTrayPreferences;
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Services discovered");
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                BluetoothGattService service = gatt.getService(UUID.fromString(GattAttributes.HM_10_SERVICE));
                if (service != null) {
                    BluetoothGattCharacteristic glucoseCharactersitic = service.getCharacteristic(UUID.fromString(GattAttributes.HM_RX_TX));
                    setCharacteristicNotification(glucoseCharactersitic, true);
                } else {
                    Log.d(TAG, "glucose service not found");
                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startJobScheduler();
        mTrayPreferences = new AppPreferences(this);
        attemptConnection();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent("com.lady.viktoria.lightdrip.services.RestartBGMeterGattService");
        sendBroadcast(broadcastIntent);
        stopJobScheduler();
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        // This is special handling for the Glucose Measurement profile.  Data parsing is
        if (UUID_BG_MEASUREMENT.equals(characteristic.getUuid())) {
            final byte[] data = characteristic.getValue();
            long timestamp = new Date().getTime();
            int packatlength = data[0];
            if (data != null && packatlength >= 2) {
                if (CheckTransmitterID(data, data.length)) {
                    TransmitterRecord.create(data, data.length, timestamp);
                }
            } else if (data != null && packatlength <= 1) {
                writeAcknowledgePacket();
                return;
            }
        } else {
            return;
        }
        sendBroadcast(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(address);
        if (mBluetoothDevice == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to true.
        mBluetoothGatt = mBluetoothDevice.connectGatt(this, true, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        Log.v(TAG, "Closing GATT");
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Glucose Measurement.
        if (UUID_BG_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    public boolean writeCustomCharacteristic(final ByteBuffer value) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }
        BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID_HM10_SERVICE);
        if (mCustomService == null) {
            Log.w(TAG, "HM10 Service not found");
            return false;
        }
        BluetoothGattCharacteristic mWriteCharacteristic = mCustomService.getCharacteristic(UUID_BG_MEASUREMENT);
        byte[] bytevalue = value.array();
        mWriteCharacteristic.setValue(bytevalue);
        boolean status = mBluetoothGatt.writeCharacteristic(mWriteCharacteristic);
        if (!mBluetoothGatt.writeCharacteristic(mWriteCharacteristic)) {
            return status;
        } else {
            return status;
        }
    }

    public void attemptConnection() {
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothManager == null) {
            return;
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            return;
        }

        if (mBluetoothDevice != null) {
            mConnectionState = STATE_DISCONNECTED;
            for (BluetoothDevice mBluetoothDevice : mBluetoothManager.getConnectedDevices(BluetoothProfile.GATT)) {
                if (mBluetoothDevice.getAddress().compareTo(mBluetoothDevice.getAddress()) == 0) {
                    mConnectionState = STATE_CONNECTED;
                }
            }
        }

        Log.i(TAG, "attemptConnection: Connection state: " + getStateStr(mConnectionState));

        if (mConnectionState == STATE_DISCONNECTED || mConnectionState == STATE_DISCONNECTING) {

            if (getBTDeviceMAC() != null) {
                if (mBluetoothAdapter.isEnabled() && mBluetoothAdapter.getRemoteDevice(getBTDeviceMAC()) != null) {
                    connect(getBTDeviceMAC());
                    return;
                }
            }
        } else if (mConnectionState == STATE_CONNECTED) {
            Log.i(TAG, "attemptConnection: Looks like we are already connected, going to read!");
            return;
        }
    }

    private String getStateStr(int mConnectionState) {
        switch (mConnectionState) {
            case STATE_CONNECTED:
                return "CONNECTED";
            case STATE_CONNECTING:
                return "CONNECTING";
            case STATE_DISCONNECTED:
                return "DISCONNECTED";
            case STATE_DISCONNECTING:
                return "DISCONNECTING";
            default:
                return "UNKNOWN STATE!";
        }
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
            String intentAction = BEACON_SNACKBAR;
            broadcastUpdate(intentAction);
            writeTxIdPacket(TransmitterID);
            return false;
        } else if (packet[0] >= 21 && packet[1] == 0) {
            Log.i(TAG, "Received Data packet");
            DexSrc = tmpBuffer.getInt(12);
            TransmitterID = convertSrc(TxId);
            if (Integer.compare(DexSrc, TransmitterID) != 0) {
                writeTxIdPacket(TransmitterID);
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    private String getBTDeviceMAC() {
        final String BTDeviceAddress = mTrayPreferences.getString("BT_MAC_Address", "00:00:00:00:00:00");
        return BTDeviceAddress;
    }

    public boolean writeTxIdPacket(int TransmitterID) {
        Log.v(TAG, "try to set transmitter ID");
        ByteBuffer txidMessage = ByteBuffer.allocate(6);
        txidMessage.order(ByteOrder.LITTLE_ENDIAN);
        txidMessage.put(0, (byte) 0x06);
        txidMessage.put(1, (byte) 0x01);
        txidMessage.putInt(2, TransmitterID);
        if (writeCustomCharacteristic(txidMessage)) {
            Log.v(TAG, "Write TXID Packet Successful");
            return true;
        } else {
            Log.v(TAG, "Write TXID Packet failed!");
            return false;
        }
    }

    public boolean writeAcknowledgePacket() {
        Log.d(TAG, "Sending Acknowledge Packet, to put wixel to sleep");
        ByteBuffer ackMessage = ByteBuffer.allocate(2);
        ackMessage.put(0, (byte) 0x02);
        ackMessage.put(1, (byte) 0xF0);
        if (writeCustomCharacteristic(ackMessage)) {
            Log.v(TAG, "Write Acknowledge Packet Successful");
            return true;
        } else {
            Log.v(TAG, "Write Acknowledge Packet failed!");
            return false;
        }
    }

    public class LocalBinder extends Binder {
        public BGMeterGattService getService() {
            return BGMeterGattService.this;
        }
    }
}