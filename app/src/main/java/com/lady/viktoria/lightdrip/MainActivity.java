package com.lady.viktoria.lightdrip;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.lady.viktoria.lightdrip.DatabaseModels.ActiveBluetoothDevice;
import com.lady.viktoria.lightdrip.services.BGMeterGattService;

import de.jonasrottmann.realmbrowser.RealmBrowser;
import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends RealmBaseActivity implements View.OnClickListener {

    private final static String TAG = MainActivity.class.getSimpleName();

    Button btnAct, browserrealm, addtimestamptodb;
    TextView bgmac;
    Bundle b;
    public String mDeviceAddress = "00:00:00:00:00:00";
    public String mDeviceName;
    public String BTDeviceAddress = "00:00:00:00:00:00";
    BGMeterGattService mBGMeterGattService;
    private TextView mConnectionState;
    private boolean mConnected = false;
    private TextView mDataField;
    private Realm mRealm;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBGMeterGattService = ((BGMeterGattService.LocalBinder) service).getService();
            if (!mBGMeterGattService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            Log.v(TAG, mDeviceAddress);
            mBGMeterGattService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBGMeterGattService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnAct = (Button) findViewById(R.id.listpaireddevices);
        bgmac = (TextView)findViewById(R.id.bgmac);
        btnAct.setOnClickListener(this);
        browserrealm = (Button) findViewById(R.id.browserealm);
        browserrealm.setOnClickListener(this);
        addtimestamptodb = (Button) findViewById(R.id.addtimestamp);
        addtimestamptodb.setOnClickListener(this);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        Intent gattServiceIntent = new Intent(this, BGMeterGattService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        mDataField = (TextView) findViewById(R.id.bgreading);
        // Initialize Realm
        Realm.init(this);
        mRealm = Realm.getInstance(getRealmConfig());

        try {
            RealmResults<ActiveBluetoothDevice> results = mRealm.where(ActiveBluetoothDevice.class).findAll();
            BTDeviceAddress = results.last().getaddress();
            if (!BTDeviceAddress.equals(null)) {
                mDeviceAddress = BTDeviceAddress;
                bgmac.setText("BGMeter MAC: \n" + mDeviceAddress);
            }
        } catch (Exception e) {
            Log.v("try_get_realm_obj", "Error " + e.getMessage());
        }

        try {
            b = getIntent().getExtras();
            mDeviceAddress = b.getString("BT MAC Address");
            mDeviceName = b.getString("BT Name");
            bgmac.setText("BGMeter MAC: \n" + mDeviceAddress);
            if (!BTDeviceAddress.equals(mDeviceAddress)) {
                mRealm = Realm.getInstance(getRealmConfig());

                try {
                    RealmResults<ActiveBluetoothDevice> results = mRealm.where(ActiveBluetoothDevice.class).findAll();
                    results.last();
                    mRealm.beginTransaction();
                    results.deleteAllFromRealm();
                    mRealm.commitTransaction();
                    mRealm.close();
                } catch (Exception e) {
                    Log.v("try_delete_realm_obj", "Error " + e.getMessage());
                }

                mRealm.beginTransaction();
                ActiveBluetoothDevice BTDevice = mRealm.createObject(ActiveBluetoothDevice.class);
                BTDevice.setname(mDeviceName);
                BTDevice.setaddress(mDeviceAddress);
                mRealm.commitTransaction();
                mRealm.close();
            }
        } catch (Exception e) {
            Log.v("try_set_realm_obj", "Error " + e.getMessage());
        }
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BGMeterGattService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BGMeterGattService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
            } else if (BGMeterGattService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BGMeterGattService.EXTRA_DATA));
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBGMeterGattService != null) {
            final boolean result = mBGMeterGattService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBGMeterGattService = null;
        mRealm.close();
    }

    private void displayData(String data) {
        if (data != null) {
            mDataField.setText(data);
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BGMeterGattService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BGMeterGattService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BGMeterGattService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.listpaireddevices:
                Intent intent = new Intent (this, BGMeterActivity.class);
                startActivity (intent);
                break;
            case R.id.browserealm:
                RealmBrowser.startRealmModelsActivity(this, getRealmConfig());
                break;
            case R.id.addtimestamp:
                RealmResults<ActiveBluetoothDevice> results = mRealm.where(ActiveBluetoothDevice.class).findAll();
                String address = results.last().getaddress();
                Log.v(TAG, "address: " + address);
                break;
            default:
                break;
        }
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }
}