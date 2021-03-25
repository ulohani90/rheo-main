package com.rheotv.android.utils;

import com.rheotv.android.data.network.models.objects.PostObject;
import com.rheotv.android.data.network.models.postlisting.responses.HomeResult;
import com.rheotv.android.data.network.models.postlisting.responses.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ListHolder {

    private List<Result> postList = new ArrayList<>();
    private HashMap<String, HomeResult> tabMap = new HashMap<>();

    private Result alertInfoObject;

    private List<String> postIds = new ArrayList<>();

    private static ListHolder listHolder;

    public static ListHolder getInstance() {
        if (listHolder == null) {
            listHolder = new ListHolder();
        }
        return listHolder;
    }

    public void extractPostIds(List<PostObject> results) {
        postIds.clear();
        for (PostObject obj : results) {
            if (obj.getVideoUrl() != null)
                postIds.add(obj.getId());
        }
    }

    public void setPostIds(List<String> postIds) {
        this.postIds = postIds;
    }

    public List<String> getPostIds() {
        return postIds;
    }

    public void setCurrentList(List<Result> list) {
        postList = list;
    }

    public List<Result> getPostList() {
        return postList;
    }

    public void clearPostList() {
        postList.clear();
    }

    public void clearTabMap() {
        tabMap.clear();
    }

    public HashMap<String, HomeResult> getTabMap() {
        return tabMap;
    }

    public void setTabMap(HashMap<String, HomeResult> tabMap) {
        this.tabMap = tabMap;
    }

    public void addItemInMap(String key, HomeResult value) {
        tabMap.put(key, value);
    }

    public void setAlertInfoObject(Result alertInfoObject) {
        this.alertInfoObject = alertInfoObject;
    }

    public Result getAlertInfoObject() {
        return alertInfoObject;
    }


}
