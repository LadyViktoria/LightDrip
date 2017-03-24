package com.lady.viktoria.lightdrip.RealmConfig;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public abstract class RealmBaseApplication extends Application {

    private RealmConfiguration realmConfiguration;

    protected RealmConfiguration getRealmConfig(String name) {
        if (realmConfiguration == null) {
            realmConfiguration = new RealmConfiguration
                    .Builder()
                    .deleteRealmIfMigrationNeeded()
                    .name(name)
                    .schemaVersion(0)
                    .build();
        }
        Realm.setDefaultConfiguration(realmConfiguration);
        return realmConfiguration;
    }

    protected void resetRealm() {
        Realm.deleteRealm(getRealmConfig("default.realm"));
    }
}