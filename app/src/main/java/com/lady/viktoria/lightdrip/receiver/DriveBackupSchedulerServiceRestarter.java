package com.lady.viktoria.lightdrip.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lady.viktoria.lightdrip.services.DriveBackupSchedulerService;

public class DriveBackupSchedulerServiceRestarter extends BroadcastReceiver {
    private final static String TAG = SchedulerJobServiceRestarter.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            context.startService(new Intent(context, DriveBackupSchedulerService.class));
        }
}