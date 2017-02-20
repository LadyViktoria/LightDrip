package com.lady.viktoria.lightdrip;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RealmBaseService extends Service {

    private RealmConfiguration realmConfiguration;

    private RealmConfiguration getRealmConfig() {
        if (realmConfiguration == null) {
            realmConfiguration = new RealmConfiguration
                    .Builder()
                    .deleteRealmIfMigrationNeeded()
                    .build();
        }
        Realm.setDefaultConfiguration(realmConfiguration);
        return realmConfiguration;
    }

    protected void resetRealm() {
        Realm.deleteRealm(getRealmConfig());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}