package com.lady.viktoria.lightdrip.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lady.viktoria.lightdrip.services.CgmBleService;


public class CgmBleServiceRestarter extends BroadcastReceiver {
    private final static String TAG = CgmBleServiceRestarter.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, CgmBleService.class));;
    }
}