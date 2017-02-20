package com.lady.viktoria.lightdrip.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.lady.viktoria.lightdrip.services.BGMeterGattService;


public class BGMeterGattServiceRestarter extends BroadcastReceiver {
    private final static String TAG = BGMeterGattServiceRestarter.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, BGMeterGattService.class));;
    }
}