package com.lady.viktoria.lightdrip.services;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.firebase.jobdispatcher.JobService;

public class SchedulerJobService extends JobService {
    private final static String TAG = SchedulerJobService.class.getSimpleName();

    @Override
    public boolean onStartJob(com.firebase.jobdispatcher.JobParameters job) {
        CgmBleService mCgmBleService = new CgmBleService();
        Intent mServiceCgmBleIntent = new Intent(getApplicationContext(), CgmBleService.class);
        if (!isMyServiceRunning(mCgmBleService.getClass())) {
            startService(mServiceCgmBleIntent);
            Log.v(TAG, "Restart CgmBle Service");
        }
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
