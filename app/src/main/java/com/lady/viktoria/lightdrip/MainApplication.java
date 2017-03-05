package com.lady.viktoria.lightdrip;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.lady.viktoria.lightdrip.RealmConfig.PrimaryKeyFactory;
import com.lady.viktoria.lightdrip.RealmConfig.RealmBaseApplication;
import com.lady.viktoria.lightdrip.services.CgmBleService;
import com.lady.viktoria.lightdrip.services.SchedulerJobService;

import io.realm.Realm;

import static io.realm.Realm.getInstance;

public class MainApplication extends RealmBaseApplication {
    private final static String TAG = MainApplication.class.getSimpleName();

    Realm mRealm;

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        mRealm = getInstance(getRealmConfig());
        initializePrimaryKeyFactory();

        /*
        BGMeterGattService mBGMeterGattService = new BGMeterGattService();
        Intent mServiceBGMeterGattIntent = new Intent(getApplicationContext(), BGMeterGattService.class);
        if (!isMyServiceRunning(mBGMeterGattService.getClass())) {
            startService(mServiceBGMeterGattIntent);
            Log.v(TAG, "Restart BGMeterGattService");
        }
        */

        CgmBleService mCgmBleService = new CgmBleService();
        Intent mServiceCgmBleIntent = new Intent(getApplicationContext(), CgmBleService.class);
        if (!isMyServiceRunning(mCgmBleService.getClass())) {
            startService(mServiceCgmBleIntent);
            Log.v(TAG, "Restart BGMeterGattService");
        }


        SchedulerJobService mJobService = new SchedulerJobService();
        Intent mServiceSchedulerJobIntent = new Intent(getApplicationContext(), SchedulerJobService.class);
        if (!isMyServiceRunning(mJobService.getClass())) {
            startService(mServiceSchedulerJobIntent);
            Log.v(TAG, "Restart SchedulerJobService");
        }

    }

    public void initializePrimaryKeyFactory() {
        try {
            Log.v(TAG, "Start PrimaryKeyFactory ");
            PrimaryKeyFactory.getInstance().initialize(mRealm);
        } catch (Exception e) {
            Log.v(TAG, "initializePrimaryKeyFactory " + e.getMessage());
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
}
