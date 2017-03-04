package com.lady.viktoria.lightdrip;

import android.app.ActivityManager;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.lady.viktoria.lightdrip.RealmActions.GlucoseRecord;
import com.lady.viktoria.lightdrip.RealmActions.SensorRecord;
import com.lady.viktoria.lightdrip.RealmModels.CalibrationData;
import com.lady.viktoria.lightdrip.RealmModels.GlucoseData;
import com.lady.viktoria.lightdrip.RealmModels.SensorData;
import com.lady.viktoria.lightdrip.RealmModels.TransmitterData;
import com.lady.viktoria.lightdrip.RealmConfig.RealmBaseActivity;
import com.lady.viktoria.lightdrip.services.BGMeterGattService;
import com.lady.viktoria.lightdrip.services.RealmService;

import net.grandcentrix.tray.AppPreferences;
import net.grandcentrix.tray.core.OnTrayPreferenceChangeListener;
import net.grandcentrix.tray.core.TrayItem;

import io.fabric.sdk.android.Fabric;
import java.io.File;
import java.util.Collection;

import de.jonasrottmann.realmbrowser.RealmBrowser;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.Sort;

import static io.realm.Realm.getInstance;

public class MainActivity extends RealmBaseActivity implements View.OnClickListener,
        OnTrayPreferenceChangeListener {

    private final static String TAG = MainActivity.class.getSimpleName();

    private TextView mConnectionState ,mDatabaseSize, bgmac, mDataField;
    private boolean mConnected = false;
    private Realm mRealm;
    private RealmChangeListener realmListener;
    private FloatingActionButton fab;
    private LinearLayout fabLabel1, fabLabel2, fabLabel3, fabLabel4;
    private View fabBGLayout;
    private boolean isFABOpen = false;
    private Context context;
    private GlucoseRecord glucoserecord;
    private SensorRecord sensorRecord;

    public Context getcontext() {
        return context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        context = this;
        Realm.init(this);
        mRealm = getInstance(getRealmConfig());
        glucoserecord = new GlucoseRecord();
        sensorRecord = new SensorRecord();
        setContentView(R.layout.activity_main);

        RealmService mRealmService = new RealmService(getApplicationContext());
        Intent mServiceRealmIntent = new Intent(getApplicationContext(), RealmService.class);
        if (!isMyServiceRunning(mRealmService.getClass())) {
            startService(mServiceRealmIntent);
        }

        BGMeterGattService mBGMeterGattService = new BGMeterGattService(getApplicationContext());
        Intent mServiceBGMeterGattIntent = new Intent(getApplicationContext(), BGMeterGattService.class);
        if (!isMyServiceRunning(mBGMeterGattService.getClass())) {
            startService(mServiceBGMeterGattIntent);
        }

        bgmac = (TextView) findViewById(R.id.bgmac);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.bgreading);
        mDatabaseSize = (TextView) findViewById(R.id.databasesize);
        fabLabel1 = (LinearLayout) findViewById(R.id.fabLabel1);
        fabLabel1.setOnClickListener(this);
        fabLabel2 = (LinearLayout) findViewById(R.id.fabLabel2);
        fabLabel2.setOnClickListener(this);
        fabLabel3 = (LinearLayout) findViewById(R.id.fabLabel3);
        fabLabel3.setOnClickListener(this);
        fabLabel4 = (LinearLayout) findViewById(R.id.fabLabel4);
        fabLabel4.setOnClickListener(this);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
        FloatingActionButton fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab1.setOnClickListener(this);
        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab2.setOnClickListener(this);
        FloatingActionButton fab3 = (FloatingActionButton) findViewById(R.id.fab3);
        fab3.setOnClickListener(this);
        FloatingActionButton fab4 = (FloatingActionButton) findViewById(R.id.fab4);
        fab4.setOnClickListener(this);
        fabBGLayout = findViewById(R.id.fabBGLayout);
        fabBGLayout.setOnClickListener(this);
        getBTDevice();
        getDatabaseSize();

        try {
            realmListener = new RealmChangeListener() {

                @Override
                public void onChange(Object element) {
                    getDatabaseSize();
                    CalibrationData calibrationRecords = mRealm.where(CalibrationData.class).findFirst();
                    TransmitterData transmitterData = mRealm.where(TransmitterData.class)
                            .findAllSorted("id", Sort.DESCENDING)
                            .where()
                            .findFirst();
                    if (calibrationRecords == null && glucoserecord.countRecordsByLastSensorID() >= 2) {
                        calibrationSnackbar();
                    } else if (calibrationRecords == null && transmitterData != null && !sensorRecord.isSensorActive()) {
                        startSensorSnackbar("Received Transmitter Data please Start Sensor");
                    }
                }
            };
            mRealm.addChangeListener(realmListener);
        } catch (Exception e) {
            Log.v(TAG, "onCreate " + e.getMessage());
        }
    }

    @Override
    public void onTrayPreferenceChanged(Collection<TrayItem> items) {
        getBTDevice();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        LocalBroadcastManager.getInstance(this).registerReceiver(mBeaconMessageReceiver, new IntentFilter("BEACON_SNACKBAR"));
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

    @Override
    public void onBackPressed() {
        if(isFABOpen){
            closeFABMenu();
        }else{
            super.onBackPressed();
        }
    }

    private final BroadcastReceiver mBeaconMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            beaconSnackbar();
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
                if (!sensorRecord.isSensorActive()) {
                    closeFABMenu();
                    startSensorSnackbar("Please start Sensor first!");
                } else if (glucoserecord.countRecordsByLastSensorID() < 2) {
                    closeFABMenu();
                    sensorReadingsSnackbar();
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
            case R.id.fab4:
            case R.id.fabLabel4:
                closeFABMenu();
                startActivity(new Intent(this, PreferncesActivity.class));
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
        fabLabel1.setVisibility(View.VISIBLE);
        fabLabel2.setVisibility(View.VISIBLE);
        fabLabel3.setVisibility(View.VISIBLE);
        fabLabel4.setVisibility(View.VISIBLE);
        fabBGLayout.setVisibility(View.VISIBLE);

        fab.animate().rotationBy(180);
        fabLabel1.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        fabLabel2.animate().translationY(-getResources().getDimension(R.dimen.standard_100));
        fabLabel3.animate().translationY(-getResources().getDimension(R.dimen.standard_145));
        fabLabel4.animate().translationY(-getResources().getDimension(R.dimen.standard_190));
    }

    private void closeFABMenu(){
        isFABOpen=false;
        fabBGLayout.setVisibility(View.GONE);
        fab.animate().rotationBy(-180);
        fabLabel1.animate().translationY(0);
        fabLabel1.setVisibility(View.GONE);
        fabLabel2.animate().translationY(0);
        fabLabel2.setVisibility(View.GONE);
        fabLabel3.animate().translationY(0);
        fabLabel3.setVisibility(View.GONE);
        fabLabel4.animate().translationY(0);
        fabLabel4.setVisibility(View.GONE);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BGMeterGattService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BGMeterGattService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BGMeterGattService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private void getBTDevice() {
        final AppPreferences appPreferences = new AppPreferences(this);
        final String mDeviceName = appPreferences.getString("BT_Name", "NULL");
        final String mDeviceAddress = appPreferences.getString("BT_MAC_Address", "00:00:00:00:00:00");
        bgmac.setText("BGMeter MAC: \n" + mDeviceName + "\n" + mDeviceAddress);
        fabBGLayout.invalidate();
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
            Log.v(TAG, "getDatabaseSize " + e.getMessage());
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
        View snackBarView = snackBar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBackground));
        snackBar.show();
    }

    private void startSensorSnackbar(String msg) {
        final Snackbar snackBar = Snackbar.make(fabBGLayout
                , msg
                , Snackbar.LENGTH_INDEFINITE);
        snackBar.setAction("Start Sensor", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackBar.dismiss();
                android.app.FragmentTransaction ft3 = getFragmentManager().beginTransaction();
                ft3.replace(R.id.fragment, new SensorActionFragment());
                ft3.commit();
            }
        });
        View snackBarView = snackBar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBackground));
        snackBar.show();
    }

    private void beaconSnackbar() {
        final Snackbar snackBar = Snackbar.make(fabBGLayout
                , "We have got a Beacon Package please check Transmitter ID"
                , Snackbar.LENGTH_INDEFINITE);
        snackBar.setAction("Dismiss", new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                snackBar.dismiss();
            }
        });
        View snackBarView = snackBar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBackground));
        snackBar.show();
    }

    private void sensorReadingsSnackbar() {
        final Snackbar snackBar = Snackbar.make(fabBGLayout
                , "Please wait until we got 2 Sensor Readings!"
                , Snackbar.LENGTH_LONG);
        View snackBarView = snackBar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBackground));
        snackBar.show();
    }
}