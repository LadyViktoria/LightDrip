package com.lady.viktoria.lightdrip;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.lady.viktoria.lightdrip.services.BGMeterGattService;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Calendar;
import java.util.Date;

import static com.lady.viktoria.lightdrip.utils.convertSrc;

public class AndroidBluetooth extends Activity {
    private final static String TAG = AndroidBluetooth.class.getSimpleName();


    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PAIRED_DEVICE = 2;
    private static final BGMeterGattService mBGMeterGattService = new BGMeterGattService();


    /** Called when the activity is first created. */
    TextView stateBluetooth;
    BluetoothAdapter bluetoothAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        stateBluetooth = (TextView)findViewById(R.id.bluetoothstate);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        CheckBlueToothState();
    }

    private void CheckBlueToothState(){
        if (bluetoothAdapter == null){
            stateBluetooth.setText("Bluetooth NOT support");
        }else{
            if (bluetoothAdapter.isEnabled()){
                if(bluetoothAdapter.isDiscovering()){
                    stateBluetooth.setText("Bluetooth is currently in device discovery process.");
                }else{
                    stateBluetooth.setText("Bluetooth is Enabled.");
                }
            }else{
                stateBluetooth.setText("Bluetooth is NOT Enabled!");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    public static boolean CheckTransmitterID(byte[] packet, int len) {
        int DexSrc;
        int TransmitterID;
        String TxId;

        TxId = "6GAX2";
        TransmitterID = convertSrc(TxId);

        ByteBuffer tmpBuffer = ByteBuffer.allocate(len);
        tmpBuffer.order(ByteOrder.LITTLE_ENDIAN);
        tmpBuffer.put(packet, 0, len);
        DexSrc = tmpBuffer.getInt(12);

        ByteBuffer txidMessage = ByteBuffer.allocate(6);
        txidMessage.order(ByteOrder.LITTLE_ENDIAN);
        txidMessage.put(0, (byte) 0x06);
        txidMessage.put(1, (byte) 0x01);
        txidMessage.putInt(2, TransmitterID);

        if (packet[0] == 0x07 && packet[1] == -15) {
            Log.i(TAG, "Received Beacon packet.");
            if (TxId.compareTo("00000") != 0 && Integer.compare(DexSrc, TransmitterID) != 0) {
                Log.v(TAG, "TXID wrong.  Expected " + TransmitterID + " but got " + DexSrc);
                Log.v(TAG, "try to set transmitter ID");
                mBGMeterGattService.writeCustomCharacteristic(txidMessage);
                return false;
            } else {
                Log.v(TAG, "TXID from settings " + TransmitterID + " matches with " + DexSrc);
                return true;
            }
        }
        else if (packet[0] == 0x11 && packet[1] == 0x00) {
            Log.i(TAG, "Received Data packet");
            if (len >= 0x11) {
                DexSrc = tmpBuffer.getInt(12);
                TransmitterID = convertSrc(TxId);
                if (Integer.compare(DexSrc, TransmitterID) != 0) {
                    Log.v(TAG, "TXID wrong.  Expected " + TransmitterID + " but got " + DexSrc);
                    Log.v(TAG, "try to set transmitter ID");
                    mBGMeterGattService.writeCustomCharacteristic(txidMessage);
                    return false;
                } else {
                    Log.v(TAG, "TXID from settings " + TransmitterID + " matches with " + DexSrc);
                    writeAcknowledgePacket();
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean writeAcknowledgePacket() {
        Log.d(TAG, "Sending Acknowledge Packet, to put wixel to sleep");
        ByteBuffer ackMessage = ByteBuffer.allocate(2);
        ackMessage.put(0, (byte) 0x02);
        ackMessage.put(1, (byte) 0xF0);
        mBGMeterGattService.writeCustomCharacteristic(ackMessage);
        return true;
    }

    private Button.OnClickListener btnListPairedDevicesOnClickListener
            = new Button.OnClickListener(){

        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            Intent intent = new Intent();
            intent.setClass(AndroidBluetooth.this, BGMeterActivity.class);
            startActivityForResult(intent, REQUEST_PAIRED_DEVICE);
        }};

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if(requestCode == REQUEST_ENABLE_BT){
            CheckBlueToothState();
        }if (requestCode == REQUEST_PAIRED_DEVICE){
            if(resultCode == RESULT_OK){

            }
        }
    }
}