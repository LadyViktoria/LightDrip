package com.lady.viktoria.lightdrip.services;

import android.app.ActivityManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SchedulerJobService extends JobService {
    private final static String TAG = SchedulerJobService.class.getSimpleName();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent("com.lady.viktoria.lightdrip.services.RestartSchedulerJobService");
        sendBroadcast(broadcastIntent);
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {

        BGMeterGattService mBGMeterGattService = new BGMeterGattService();
        Intent mServiceBGMeterGattIntent = new Intent(getApplicationContext(), BGMeterGattService.class);
        if (!isMyServiceRunning(mBGMeterGattService.getClass())) {
            startService(mServiceBGMeterGattIntent);
            Log.v(TAG, "Restart GATT Service");
        }
        Log.v(TAG, "GATT Service is running");

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
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
