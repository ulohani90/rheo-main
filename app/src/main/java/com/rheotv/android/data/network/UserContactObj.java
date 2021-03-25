package com.rheotv.android.data.network;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UserContactObj {

    @SerializedName("name")
    private String name;

    @SerializedName("numbers")
    private List<String> numbers;

    public UserContactObj() {

    }

    public UserContactObj(String name, List<String> numbers) {
        this.name = name;
        this.numbers = numbers;
    }

    public String getName() {
        return name;
    }

    public List<String> getNumber() {
        return numbers;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNumber(List<String> numbers) {
        this.numbers = numbers;
    }
}
