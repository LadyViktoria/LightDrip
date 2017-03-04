package com.lady.viktoria.lightdrip.services;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.lady.viktoria.lightdrip.RealmConfig.PrimaryKeyFactory;
import com.lady.viktoria.lightdrip.RealmConfig.RealmBaseService;

import io.realm.Realm;

import static io.realm.Realm.getInstance;

public class RealmService extends RealmBaseService {
    private final static String TAG = RealmService.class.getSimpleName();

    private Realm mRealm;

    public RealmService(Context applicationContext) {
        super();
    }

    public RealmService() {

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startJobScheduler();
        Realm.init(this);
        mRealm = getInstance(getRealmConfig());
        initializePrimaryKeyFactory();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Try to restart RealmService!");
        Intent broadcastIntent = new Intent("com.lady.viktoria.lightdrip.services.RestartRealmervice");
        sendBroadcast(broadcastIntent);
        stopJobScheduler();
        mRealm.close();
        Realm.compactRealm(getRealmConfig());
    }

    public void startJobScheduler() {
        final long REFRESH_INTERVAL  = 15 * 60 * 1000;
        ComponentName serviceComponent = new ComponentName(this, SchedulerJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(1, serviceComponent);
        builder.setRequiresDeviceIdle(false);
        builder.setRequiresCharging(false);
        builder.setPeriodic(REFRESH_INTERVAL);
        //builder.setPersisted(true);
        JobScheduler jobScheduler = (JobScheduler)this.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int result = jobScheduler.schedule(builder.build());
        if (result == JobScheduler.RESULT_SUCCESS) Log.d(TAG, "Job scheduled successfully!");
    }

    public void stopJobScheduler() {
        JobScheduler jobScheduler = (JobScheduler)this.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(1);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void initializePrimaryKeyFactory() {
        try {
            Log.v(TAG, "Start PrimaryKeyFactory ");
            PrimaryKeyFactory.getInstance().initialize(mRealm);
        } catch (Exception e) {
            Log.v(TAG, "initializePrimaryKeyFactory " + e.getMessage());
        }
    }
}
