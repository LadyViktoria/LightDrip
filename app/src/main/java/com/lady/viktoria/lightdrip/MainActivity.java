package com.lady.viktoria.lightdrip;

import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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
import com.lady.viktoria.lightdrip.services.CgmBleService;

import net.grandcentrix.tray.AppPreferences;
import net.grandcentrix.tray.core.OnTrayPreferenceChangeListener;
import net.grandcentrix.tray.core.TrayItem;

import java.io.File;
import java.util.Collection;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.jonasrottmann.realmbrowser.RealmBrowser;
import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.Sort;

import static io.realm.Realm.getDefaultInstance;

public class MainActivity extends AppCompatActivity implements OnTrayPreferenceChangeListener {

    private final static String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.bgmac) TextView bgmac;
    @BindView(R.id.connection_state) TextView mConnectionState;
    @BindView(R.id.databasesize) TextView mDatabaseSize;
    @BindView(R.id.bgreading) TextView mDataField;
    @BindView(R.id.fabLabel1) LinearLayout fabLabel1;
    @BindView(R.id.fabLabel2) LinearLayout fabLabel2;
    @BindView(R.id.fabLabel3) LinearLayout fabLabel3;
    @BindView(R.id.fabLabel4) LinearLayout fabLabel4;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.fab1) FloatingActionButton fab1;
    @BindView(R.id.fab2) FloatingActionButton fab2;
    @BindView(R.id.fab3) FloatingActionButton fab3;
    @BindView(R.id.fab4) FloatingActionButton fab4;
    @BindView(R.id.fabBGLayout) View fabBGLayout;

    private boolean isFABOpen = false;
    private Realm mRealm;
    RealmChangeListener realmListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        Realm.init(this);
        mRealm = getDefaultInstance();
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        getBTDevice();
        getDatabaseSize();

        try {
            realmListener = element -> {
                getDatabaseSize();
                CalibrationData calibrationRecords = mRealm.where(CalibrationData.class).findFirst();
                TransmitterData transmitterData = mRealm.where(TransmitterData.class)
                        .findAllSorted("id", Sort.DESCENDING)
                        .where()
                        .findFirst();
                GlucoseRecord glucoserecord = new GlucoseRecord();
                SensorRecord sensorRecord = new SensorRecord();
                if (calibrationRecords == null && glucoserecord.countRecordsByLastSensorID() >= 2) {
                    calibrationSnackbar();
                } else if (calibrationRecords == null && transmitterData != null && !sensorRecord.isSensorActive()) {
                    startSensorSnackbar("Received Transmitter Data please Start Sensor");
                }
            };
        } catch (Exception e) {
            Log.v(TAG, "onCreate " + e.getMessage());
        }
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
        Realm.compactRealm(mRealm.getConfiguration());
    }

    @Override
    public void onBackPressed() {
        if (isFABOpen) {
            closeFABMenu();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onTrayPreferenceChanged(Collection<TrayItem> items) {
        getBTDevice();
    }

    @OnClick({R.id.fab, R.id.fab1, R.id.fabLabel1,
            R.id.fab2, R.id.fabLabel2,
            R.id.fab3, R.id.fabLabel3,
            R.id.fab4, R.id.fabLabel4 })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                if (!isFABOpen) {
                    showFABMenu();
                } else {
                    closeFABMenu();
                }
                break;
            case R.id.fab1:
            case R.id.fabLabel1:
                closeFABMenu();
                RealmBrowser.startRealmModelsActivity(this, mRealm.getConfiguration());
                break;
            case R.id.fab2:
            case R.id.fabLabel2:
                GlucoseRecord glucoserecord = new GlucoseRecord();
                SensorRecord sensorRecord = new SensorRecord();
                if (!sensorRecord.isSensorActive()) {
                    closeFABMenu();
                    startSensorSnackbar("Please start Sensor first!");
                } else if (glucoserecord.countRecordsByLastSensorID() < 2) {
                    closeFABMenu();
                    sensorReadingsSnackbar();
                } else {
                    closeFABMenu();
                    FragmentManager fm = getFragmentManager();
                    CalibrationDialogFragment dialogFragment = new CalibrationDialogFragment();
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

    private void showFABMenu() {
        isFABOpen = true;
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

    private void closeFABMenu() {
        isFABOpen = false;
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

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (CgmBleService.ACTION_GATT_CONNECTED.equals(action)) {
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (CgmBleService.ACTION_GATT_DISCONNECTED.equals(action)) {
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
            } else if (CgmBleService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(CgmBleService.EXTRA_DATA));
            } else if (CgmBleService.BEACON_SNACKBAR.equals(action)) {
                beaconSnackbar();
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CgmBleService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(CgmBleService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(CgmBleService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(CgmBleService.BEACON_SNACKBAR);
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
        runOnUiThread(() -> mConnectionState.setText(resourceId));
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
                    + String.format("\nItems in GlucoseData: %d", itemSizeGlucoseData)
                    + String.format("\nItems in CalibrationData: %d", itemSizeCalibrationData)
                    + String.format("\nItems in SensorData: %d", itemSizeSensorData)
                    + String.format("\nItems in TransmitterData: %d", itemSizeTransmitterData)
                    + "\nDatabase Size: " + FileSize);
            mDatabaseSize.invalidate();
        } catch (Exception e) {
            Log.v(TAG, "getDatabaseSize " + e.getMessage());
        }
    }

    private void calibrationSnackbar() {
        final Snackbar snackBar = Snackbar.make(fabBGLayout
                , "We have got 2 Readings please Add double Calibration"
                , Snackbar.LENGTH_INDEFINITE);
        snackBar.setAction("Add Calibration", v -> {
            snackBar.dismiss();
            FragmentManager fm = getFragmentManager();
            CalibrationDialogFragment dialogFragment = new CalibrationDialogFragment();
            dialogFragment.show(fm, "Calibration Dialog Fragment");
        });
        View snackBarView = snackBar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBackground));
        snackBar.show();
    }

    private void startSensorSnackbar(String msg) {
        final Snackbar snackBar = Snackbar.make(fabBGLayout
                , msg
                , Snackbar.LENGTH_INDEFINITE);
        snackBar.setAction("Start Sensor", v -> {
            snackBar.dismiss();
            android.app.FragmentTransaction ft3 = getFragmentManager().beginTransaction();
            ft3.replace(R.id.fragment, new SensorActionFragment());
            ft3.commit();
        });
        View snackBarView = snackBar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBackground));
        snackBar.show();
    }

    private void beaconSnackbar() {
        final Snackbar snackBar = Snackbar.make(fabBGLayout
                , "We have got a Beacon Package please check Transmitter ID"
                , Snackbar.LENGTH_INDEFINITE);
        snackBar.setAction("Dismiss", v -> snackBar.dismiss());
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