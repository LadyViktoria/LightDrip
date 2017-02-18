package com.lady.viktoria.lightdrip.DatabaseModels;

import io.realm.RealmObject;
import io.realm.annotations.Required;


public class CallibrationData extends RealmObject {

    @Required
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

}