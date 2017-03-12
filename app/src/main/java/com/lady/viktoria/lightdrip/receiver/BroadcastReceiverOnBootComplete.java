package com.lady.viktoria.lightdrip.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lady.viktoria.lightdrip.services.CgmBleService;
import com.lady.viktoria.lightdrip.services.SchedulerJobService;

public class BroadcastReceiverOnBootComplete extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            Intent mCgmBleServiceIntent = new Intent(context, CgmBleService.class);
            context.startService(mCgmBleServiceIntent);
        }
    }
}
