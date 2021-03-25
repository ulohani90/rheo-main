package com.rheotv.android.data;

import com.google.gson.annotations.SerializedName;

public class ModeratorQuestionOption {

    @SerializedName("order")
    int order;

    @SerializedName("option")
    String option;

    @SerializedName("id")
    String id;

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
