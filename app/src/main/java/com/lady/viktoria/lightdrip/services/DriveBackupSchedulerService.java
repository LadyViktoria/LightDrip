package com.lady.viktoria.lightdrip.services;

import android.content.Intent;
import android.util.Log;

import com.firebase.jobdispatcher.JobService;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import xiaofei.library.hermeseventbus.HermesEventBus;

public class DriveBackupSchedulerService extends JobService {
    private final static String TAG = DriveBackupSchedulerService.class.getSimpleName();

    @Override
    public boolean onStartJob(com.firebase.jobdispatcher.JobParameters job) {
        startService(new Intent(this, DriveBackupService.class));
        return false;
    }

    @Override
    public boolean onStopJob(com.firebase.jobdispatcher.JobParameters job) {
        stopService(new Intent(this, DriveBackupService.class));
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent("com.lady.viktoria.lightdrip.services.RestartDriveBackupSchedulerService");
        sendBroadcast(broadcastIntent);
    }

}
