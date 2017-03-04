package com.lady.viktoria.lightdrip.RealmConfig;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public abstract class RealmBaseApplication extends Application {

    private RealmConfiguration realmConfiguration;

    protected RealmConfiguration getRealmConfig() {
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
}