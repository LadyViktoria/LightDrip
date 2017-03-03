package com.lady.viktoria.lightdrip.services;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;


import com.lady.viktoria.lightdrip.RealmConfig.PrimaryKeyFactory;
import com.lady.viktoria.lightdrip.RealmConfig.RealmBaseService;

import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;

import static io.realm.Realm.getInstance;

public class RealmService extends RealmBaseService {
    private final static String TAG = RealmService.class.getSimpleName();

    private Realm mRealm;

    public RealmService(Context applicationContext) {
        super();
    }

    public RealmService() {

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startTimer();
        Realm.init(this);
        mRealm = getInstance(getRealmConfig());
        initializePrimaryKeyFactory();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Try to restart RealmService!");
        Intent broadcastIntent = new Intent("com.lady.viktoria.lightdrip.services.RestartRealmervice");
        sendBroadcast(broadcastIntent);
        stoptimertask();
        mRealm.close();
        Realm.compactRealm(getRealmConfig());
    }

    private Timer timer;
    private TimerTask timerTask;
    long oldTime=0;
    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 1000); //
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
            }
        };
    }

    /**
     * not needed
     */
    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void initializePrimaryKeyFactory() {
        try {
            Log.v(TAG, "Start PrimaryKeyFactory ");
            PrimaryKeyFactory.getInstance().initialize(mRealm);
        } catch (Exception e) {
            Log.v(TAG, "initializePrimaryKeyFactory " + e.getMessage());
        }
    }
}
