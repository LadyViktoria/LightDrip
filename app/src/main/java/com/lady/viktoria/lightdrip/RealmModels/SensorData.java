package com.lady.viktoria.lightdrip.RealmModels;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;


public class SensorData extends RealmObject {

    @PrimaryKey
    @Index
    private long id;
    public long getid() {
        return id;
    }
    public void setid(final long id) {
        this.id = id;
    }

    private long started_at;
    public long getstarted_at() {
        return started_at;
    }
    public void setstarted_at(final long started_at) {
        this.started_at = started_at;
    }

    private long stopped_at;
    public long getstopped_at() {
        return stopped_at;
    }
    public void setstopped_at(final long stopped_at) {
        this.stopped_at = stopped_at;
    }

}