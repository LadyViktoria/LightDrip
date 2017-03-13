package com.lady.viktoria.lightdrip;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.evernote.android.job.JobManager;
import com.lady.viktoria.lightdrip.RealmBackup.Backup;
import com.lady.viktoria.lightdrip.RealmBackup.GoogleDriveBackup;
import com.lady.viktoria.lightdrip.RealmConfig.PrimaryKeyFactory;
import com.lady.viktoria.lightdrip.RealmConfig.RealmBaseApplication;
import com.lady.viktoria.lightdrip.scheduler.BackupJobCreator;
import com.lady.viktoria.lightdrip.scheduler.BackupSyncJob;
import com.lady.viktoria.lightdrip.services.CgmBleService;

import io.realm.Realm;
import xiaofei.library.hermeseventbus.HermesEventBus;

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
        HermesEventBus.getDefault().init(this);
        JobManager.create(this).addJobCreator(new BackupJobCreator());
        BackupSyncJob.schedule(this);

        CgmBleService mCgmBleService = new CgmBleService();
        Intent mServiceCgmBleIntent = new Intent(getApplicationContext(), CgmBleService.class);
        if (!isMyServiceRunning(mCgmBleService.getClass())) {
            startService(mServiceCgmBleIntent);
            Log.v(TAG, "Restart CgmBleService");
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

    @NonNull
    public Backup getBackup() {
        return new GoogleDriveBackup();
    }
}
