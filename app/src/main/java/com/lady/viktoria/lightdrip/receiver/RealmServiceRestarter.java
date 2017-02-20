package com.lady.viktoria.lightdrip.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lady.viktoria.lightdrip.services.RealmService;

public class RealmServiceRestarter extends BroadcastReceiver {
    private final static String TAG = RealmServiceRestarter.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, RealmService.class));;
    }
}