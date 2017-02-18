package com.lady.viktoria.lightdrip.DatabaseModels;

import io.realm.RealmObject;

public class ActiveBluetoothDevice extends RealmObject {

    private String name;
    public String getname() {
        return name;
    }
    public void setname(final String name) {
        this.name = name;
    }

    private String address;
    public String getaddress() {
        return address;
    }
    public void setaddress(final String address) {
        this.address = address;
    }

    private boolean connected;
    public boolean getconnected() {
        return connected;
    }
    public void setconnected(final boolean connected) {
        this.connected = connected;
    }

}