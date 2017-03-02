package com.lady.viktoria.lightdrip;

import android.app.ActivityManager;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import com.lady.viktoria.lightdrip.RealmActions.GlucoseRecord;
import com.lady.viktoria.lightdrip.RealmActions.SensorRecord;
import com.lady.viktoria.lightdrip.RealmModels.CalibrationData;
import com.lady.viktoria.lightdrip.RealmModels.GlucoseData;
import com.lady.viktoria.lightdrip.RealmModels.SensorData;
import com.lady.viktoria.lightdrip.RealmModels.TransmitterData;
import com.lady.viktoria.lightdrip.RealmConfig.RealmBaseActivity;
import com.lady.viktoria.lightdrip.services.BGMeterGattService;
import com.lady.viktoria.lightdrip.services.RealmService;

import java.io.File;

import de.jonasrottmann.realmbrowser.RealmBrowser;
import io.realm.Realm;
import io.realm.RealmChangeListener;

import static io.realm.Realm.getInstance;

public class MainActivity extends RealmBaseActivity implements View.OnClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private final static String TAG = MainActivity.class.getSimpleName();

    private String mDeviceAddress = "00:00:00:00:00:00";
    private String mDeviceName;
    private BGMeterGattService mBGMeterGattService;
    private TextView mConnectionState ,mDatabaseSize, bgmac, mDataField;
    private boolean mConnected = false;
    private Realm mRealm;
    private RealmService mRealmService;
    private RealmChangeListener realmListener;

    FloatingActionButton fab, fab1, fab2, fab3;
    LinearLayout fabLabel1, fabLabel2, fabLabel3;
    View fabBGLayout;
    boolean isFABOpen = false;
    Intent mServiceRealmIntent, mServiceBGMeterGattIntent;
    Context context;

    public Context getcontext() {
        return context;
    }
    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        Realm.init(this);
        mRealm = getInstance(getRealmConfig());
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRealmService = new RealmService(getcontext());
        mServiceRealmIntent = new Intent(getcontext(), RealmService.class);
        if (!isMyServiceRunning(mRealmService.getClass())) {
            startService(mServiceRealmIntent);
        }

        mBGMeterGattService = new BGMeterGattService(getcontext());
        mServiceBGMeterGattIntent = new Intent(getcontext(),BGMeterGattService.class);
        if (!isMyServiceRunning(mBGMeterGattService.getClass())) {
            startService(mServiceBGMeterGattIntent);
        }

        bgmac = (TextView)findViewById(R.id.bgmac);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.bgreading);
        mDatabaseSize = (TextView) findViewById(R.id.databasesize);
        fabLabel1= (LinearLayout) findViewById(R.id.fabLabel1);
        fabLabel1.setOnClickListener(this);
        fabLabel2= (LinearLayout) findViewById(R.id.fabLabel2);
        fabLabel2.setOnClickListener(this);
        fabLabel3= (LinearLayout) findViewById(R.id.fabLabel3);
        fabLabel3.setOnClickListener(this);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
        fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab1.setOnClickListener(this);
        fab2= (FloatingActionButton) findViewById(R.id.fab2);
        fab2.setOnClickListener(this);
        fab3= (FloatingActionButton) findViewById(R.id.fab3);
        fab3.setOnClickListener(this);
        fabBGLayout=findViewById(R.id.fabBGLayout);
        fabBGLayout.setOnClickListener(this);
        getBTDevice();
        getDatabaseSize();
        try {
            realmListener = new RealmChangeListener() {

                @Override
                public void onChange(Object element) {
                    getDatabaseSize();
                    GlucoseRecord glucoserecord = new GlucoseRecord();
                    CalibrationData calibrationRecords = mRealm.where(CalibrationData.class).findFirst();
                    if(calibrationRecords == null && glucoserecord.countRecordsByLastSensorID() >= 2){
                        calibrationSnackbar();
                    }
                }};
            mRealm.addChangeListener(realmListener);
        } catch (Exception e) {
            Log.v(TAG, "Error try_delete_realm_obj " + e.getMessage());
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                if(!isFABOpen) {showFABMenu();}
                else {closeFABMenu();}
                break;
            case R.id.fab1:
            case R.id.fabLabel1:
                closeFABMenu();
                RealmBrowser.startRealmModelsActivity(this, getRealmConfig());
                break;
            case R.id.fab2:
            case R.id.fabLabel2:
                SensorRecord sensorRecord = new SensorRecord();
                GlucoseRecord glucoserecord = new GlucoseRecord();
                if (!sensorRecord.isSensorActive()) {
                    closeFABMenu();
                    startSensorSnackbar();
                } else if (glucoserecord.countRecordsByLastSensorID() < 2) {
                    closeFABMenu();
                    Snackbar.make(view, "Please wait until we got 2 Sensor Readings!", Snackbar.LENGTH_LONG).show();
                } else {
                    closeFABMenu();
                    FragmentManager fm = getFragmentManager();
                    CalibrationDialogFragment dialogFragment = new CalibrationDialogFragment ();
                    dialogFragment.show(fm, "Calibration Dialog Fragment");
                }
                break;
            case R.id.fab3:
            case R.id.fabLabel3:
                closeFABMenu();
                android.app.FragmentTransaction ft3 = getFragmentManager().beginTransaction();
                //ft.addToBackStack(null);
                ft3.replace(R.id.fragment, new SensorActionFragment());
                ft3.commit();
                break;
            case R.id.fabBGLayout:
                closeFABMenu();
                break;
            default:
                break;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
       getBTDevice();
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
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
        getBTDevice();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.removeChangeListener(realmListener);
        mRealm.close();
        Realm.compactRealm(getRealmConfig());
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

    private void displayData(String data) {
        if (data != null) {
            mDataField.setText(data);
        }
    }

    private void getBTDevice() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mDeviceName = preferences.getString("BT_Name", "NULL");
        mDeviceAddress = preferences.getString("BT_MAC_Address", "00:00:00:00:00:00");
        bgmac.setText("BGMeter MAC: \n" + mDeviceName + "\n" + mDeviceAddress);
    }

    private void showFABMenu(){
        isFABOpen=true;
        fabLabel1.setVisibility(View.VISIBLE);
        fabLabel2.setVisibility(View.VISIBLE);
        fabLabel3.setVisibility(View.VISIBLE);
        fabBGLayout.setVisibility(View.VISIBLE);

        fab.animate().rotationBy(180);
        fabLabel1.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        fabLabel2.animate().translationY(-getResources().getDimension(R.dimen.standard_100));
        fabLabel3.animate().translationY(-getResources().getDimension(R.dimen.standard_145));
    }

    private void closeFABMenu(){
        isFABOpen=false;
        fabBGLayout.setVisibility(View.GONE);
        fab.animate().rotationBy(-180);
        fabLabel1.animate().translationY(0);
        fabLabel2.animate().translationY(0);
        fabLabel3.animate().translationY(0);
        fabLabel1.setVisibility(View.GONE);
        fabLabel2.setVisibility(View.GONE);
        fabLabel3.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        if(isFABOpen){
            closeFABMenu();
        }else{
            super.onBackPressed();
        }
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

    private void calibrationSnackbar() {
        final Snackbar snackBar = Snackbar.make(fabBGLayout
                , "We have got 2 Readings please Add double Calibration"
                , Snackbar.LENGTH_INDEFINITE);
        snackBar.setAction("Add Calibration", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackBar.dismiss();
                FragmentManager fm = getFragmentManager();
                CalibrationDialogFragment dialogFragment = new CalibrationDialogFragment ();
                dialogFragment.show(fm, "Calibration Dialog Fragment");
            }
        });
        snackBar.show();
    }

    private void startSensorSnackbar() {
        final Snackbar snackBar = Snackbar.make(fabBGLayout
                , "Please start Sensor first!"
                , Snackbar.LENGTH_LONG);
        snackBar.setAction("Start Sensor", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackBar.dismiss();
                android.app.FragmentTransaction ft3 = getFragmentManager().beginTransaction();
                ft3.replace(R.id.fragment, new SensorActionFragment());
                ft3.commit();
            }
        });
        snackBar.show();
    }

    private void getDatabaseSize() {
        try {
            int itemSizeGlucoseData = mRealm.where(GlucoseData.class).findAll().size();
            int itemSizeCalibrationData = mRealm.where(CalibrationData.class).findAll().size();
            int itemSizeSensorData = mRealm.where(SensorData.class).findAll().size();
            int itemSizeTransmitterData = mRealm.where(TransmitterData.class).findAll().size();
            int itemSizeAll = itemSizeGlucoseData + itemSizeCalibrationData
                    + itemSizeSensorData + itemSizeTransmitterData;

            String FileSize = null;
            File writableFolder = MainActivity.this.getFilesDir();
            File realmFile = new File(writableFolder, Realm.DEFAULT_REALM_NAME);
            if (realmFile.length() >= 0) {
                FileSize = realmFile.length() + " bytes";
            }
            if (realmFile.length() / 1024 >= 1) {
                FileSize = realmFile.length() / 1024 + " Kb";
            }
            if (realmFile.length() / 1024 / 1024 >= 1) {
                FileSize = realmFile.length() / 1024 / 1024 + " Mb";
            }
            mDatabaseSize.setText(String.format("Items in Database: %d", itemSizeAll)
                    + String.format("\nItems in GlucoseData: %d",itemSizeGlucoseData)
                    + String.format("\nItems in CalibrationData: %d",itemSizeCalibrationData)
                    + String.format("\nItems in SensorData: %d",itemSizeSensorData)
                    + String.format("\nItems in TransmitterData: %d",itemSizeTransmitterData)
                    + "\nDatabase Size: " + FileSize);
            mDatabaseSize.invalidate();
        } catch (Exception e) {
        Log.v(TAG, "Error try_get_realm_obj " + e.getMessage());
        }
    }
}