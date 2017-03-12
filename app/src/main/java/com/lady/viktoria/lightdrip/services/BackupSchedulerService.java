package com.lady.viktoria.lightdrip.services;

import android.content.Intent;
import android.util.Log;

import com.firebase.jobdispatcher.JobService;

public class BackupSchedulerService extends JobService {
    private final static String TAG = BackupSchedulerService.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public boolean onStartJob(com.firebase.jobdispatcher.JobParameters job) {
        Log.v(TAG, "CgmBle Service is running");
        return false;
    }

    @Override
    public boolean onStopJob(com.firebase.jobdispatcher.JobParameters job) {
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent("com.lady.viktoria.lightdrip.services.RestartSchedulerJobService");
        sendBroadcast(broadcastIntent);
    }

}
