package com.rheotv.android.ui.activities.tabcontainer.posts;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;
import androidx.lifecycle.ViewModel;

import com.rheotv.android.data.network.models.postlisting.responses.Result;

import java.util.List;

public class TagsViewModel extends ViewModel {

    public final ObservableList<Result> blogObservableArrayList = new ObservableArrayList<>();
    private List<String> postList;

    public TagsViewModel() {
        super();
    }

    public ObservableList<Result> getBlogObservableArrayList() {
        return blogObservableArrayList;
    }

    public List<String> getPostList() {
        return postList;
    }

    public void setPostList(List<String> postList) {
        this.postList = postList;
    }
}
