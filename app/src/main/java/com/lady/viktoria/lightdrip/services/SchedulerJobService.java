package com.lady.viktoria.lightdrip.services;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;

import com.lady.viktoria.lightdrip.R;

public class SchedulerJobService extends JobService {
    private final static String TAG = SchedulerJobService.class.getSimpleName();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startForeground(R.string.app_name, new Notification());
        return START_STICKY;
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
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

        jobFinished(jobParameters, true);
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
