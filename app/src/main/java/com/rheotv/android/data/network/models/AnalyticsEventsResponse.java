package com.rheotv.android.data.network.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class AnalyticsEventsResponse {


    @SerializedName("amp_events")
    @Expose
    List<AnalyticsEvent> events;
    @SerializedName("moengage_events")
    @Expose
    List<AnalyticsEvent> moengageEvents;

    public List<String> getEvents() {
        if (events == null) return new ArrayList<>();
        List<String> eventNames = new ArrayList<>();
        for (AnalyticsEvent event : events) {
            eventNames.add(event.getName());
        }
        return eventNames;
    }


    public List<String> getMoengageEvents() {
        if (moengageEvents == null) return new ArrayList<>();
        List<String> eventNames = new ArrayList<>();
        for (AnalyticsEvent event : moengageEvents) {
            eventNames.add(event.getName());
        }
        return eventNames;
    }

    public class AnalyticsEvent {
        @SerializedName("name")
        @Expose
        String name;

        public String getName() {
            return name;
        }
    }
}
