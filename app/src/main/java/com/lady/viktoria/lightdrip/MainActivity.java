package com.lady.viktoria.lightdrip;

import android.animation.Animator;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lady.viktoria.lightdrip.DatabaseModels.ActiveBluetoothDevice;
import com.lady.viktoria.lightdrip.services.BGMeterGattService;
import com.lady.viktoria.lightdrip.services.RealmService;

import de.jonasrottmann.realmbrowser.RealmBrowser;
import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends RealmBaseActivity implements View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private final static String TAG = MainActivity.class.getSimpleName();

    TextView bgmac;
    public String mDeviceAddress = "00:00:00:00:00:00";
    public String mDeviceName;
    public String BTDeviceAddress = "00:00:00:00:00:00";
    private BGMeterGattService mBGMeterGattService;
    private TextView mConnectionState;
    private boolean mConnected = false;
    private TextView mDataField;
    private Realm mRealm;
    private RealmService mRealmService;
    FloatingActionButton fab, fab1, fab2;
    LinearLayout fabLayout1, fabLayout2;
    View fabBGLayout;
    boolean isFABOpen=false;
    Intent mServiceRealmIntent, mServiceBGMeterGattIntent;
    Context context;
    public Context getcontext() {
        return context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mRealmService = new RealmService(getcontext());
        mServiceRealmIntent = new Intent(getcontext(), mServiceRealmIntent.getClass());
        if (!isMyServiceRunning(mRealmService.getClass())) {
            startService(mServiceRealmIntent);
        }


        /*
        mBGMeterGattService = new BGMeterGattService(getcontext());
        mServiceBGMeterGattIntent = new Intent(getcontext(), mBGMeterGattService.getClass());
        if (!isMyServiceRunning(mBGMeterGattService.getClass())) {
            startService(mServiceBGMeterGattIntent);
        }
        */

        bgmac = (TextView)findViewById(R.id.bgmac);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.bgreading);
        fabLayout1= (LinearLayout) findViewById(R.id.fabLayout1);
        fabLayout2= (LinearLayout) findViewById(R.id.fabLayout2);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab2= (FloatingActionButton) findViewById(R.id.fab2);
        fabBGLayout=findViewById(R.id.fabBGLayout);
        fab.setOnClickListener(this);
        fab1.setOnClickListener(this);
        fab2.setOnClickListener(this);
        fabBGLayout.setOnClickListener(this);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        getLastBTDevice();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

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
        if (id == R.id.action_preferences) {
            startActivity(new Intent(this, PreferncesActivity.class));
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
        getLastBTDevice();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        startService(mServiceRealmIntent);
        startService(mServiceBGMeterGattIntent);
        mBGMeterGattService = null;
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

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    private void getLastBTDevice() {
        try {
            mRealm = Realm.getInstance(getRealmConfig());
            RealmResults<ActiveBluetoothDevice> results = mRealm.where(ActiveBluetoothDevice.class).findAll();
            BTDeviceAddress = results.last().getaddress();
            mDeviceName = results.last().getname();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("last_connected_btdevice", BTDeviceAddress);
            editor.apply();
            if (!BTDeviceAddress.equals(null)) {
                mDeviceAddress = BTDeviceAddress;
                bgmac.setText("BGMeter MAC: \n" + mDeviceName + "\n" + mDeviceAddress);
            }
        } catch (Exception e) {
            Log.v(TAG, "Error try_get_realm_obj " + e.getMessage());
        } finally {
            mRealm.close();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mDeviceName = preferences.getString("BT_Name", "NULL");
        mDeviceAddress = preferences.getString("BT_MAC_Address", "00:00:00:00:00:00");
        bgmac.setText("BGMeter MAC: \n" + mDeviceName + "\n" + mDeviceAddress);
        try {
            if (!BTDeviceAddress.equals(mDeviceAddress)) {

                try {
                    mRealm = Realm.getInstance(getRealmConfig());
                    RealmResults<ActiveBluetoothDevice> results = mRealm.where(ActiveBluetoothDevice.class).findAll();
                    results.last();
                    mRealm.beginTransaction();
                    results.deleteAllFromRealm();
                    mRealm.commitTransaction();
                } catch (Exception e) {
                    Log.v(TAG, "Error try_delete_realm_obj " + e.getMessage());
                } finally {
                    mRealm.close();
                }

                mRealm = Realm.getInstance(getRealmConfig());
                mRealm.beginTransaction();
                ActiveBluetoothDevice BTDevice = mRealm.createObject(ActiveBluetoothDevice.class);
                BTDevice.setname(mDeviceName);
                BTDevice.setaddress(mDeviceAddress);
                mRealm.commitTransaction();
            }
        } catch (Exception e) {
            Log.v(TAG, "Error try_set_realm_obj " + e.getMessage());
        } finally {
            mRealm.close();
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                if(!isFABOpen){
                    showFABMenu();
                }else{
                    closeFABMenu();
                }
                break;
            case R.id.fab1:
                RealmBrowser.startRealmModelsActivity(this, getRealmConfig());
                break;
            case R.id.fab2:
                mRealm = Realm.getInstance(getRealmConfig());
                RealmResults<ActiveBluetoothDevice> results = mRealm.where(ActiveBluetoothDevice.class).findAll();
                String address = results.last().getaddress();
                mRealm.close();
                final Snackbar snackBar = Snackbar.make(view, "get BT MAC From DB: " + address,
                        Snackbar.LENGTH_INDEFINITE);
                snackBar.setAction("Dismiss", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackBar.dismiss();
                    }
                }).show();
                break;
            case R.id.fabBGLayout:
                closeFABMenu();
                break;
            default:
                break;
        }
    }

    private void showFABMenu(){
        isFABOpen=true;
        fabLayout1.setVisibility(View.VISIBLE);
        fabLayout2.setVisibility(View.VISIBLE);
        fabBGLayout.setVisibility(View.VISIBLE);

        fab.animate().rotationBy(180);
        fabLayout1.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        fabLayout2.animate().translationY(-getResources().getDimension(R.dimen.standard_100));
    }

    private void closeFABMenu(){
        isFABOpen=false;
        fabBGLayout.setVisibility(View.GONE);
        fab.animate().rotationBy(-180);
        fabLayout1.animate().translationY(0);
        fabLayout2.animate().translationY(0);
        fabLayout1.setVisibility(View.GONE);
        fabLayout2.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        if(isFABOpen){
            closeFABMenu();
        }else{
            super.onBackPressed();
        }
    }
}