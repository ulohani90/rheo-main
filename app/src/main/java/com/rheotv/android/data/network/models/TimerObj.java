package com.rheotv.android.data.network.models;

public class TimerObj {

    String type;
    String value;

    public TimerObj(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
