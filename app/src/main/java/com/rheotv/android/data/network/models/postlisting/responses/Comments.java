package com.rheotv.android.data.network.models.postlisting.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Comments {

    @SerializedName("next")
    @Expose
    String next;

    @SerializedName("results")
    @Expose
    List<CommentChat> results;

    @SerializedName("previous")
    @Expose
    String previous;


    @SerializedName("fanboy_texts")
    @Expose
    List<String> slangs;

    @SerializedName("can_comment")
    @Expose
    boolean canComment;

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public List<CommentChat> getResults() {
        return results;
    }

    public void setResults(List<CommentChat> results) {
        this.results = results;
    }

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    public List<String> getSlangs() {
        return slangs;
    }

    public boolean isCanComment() {
        return canComment;
    }
}
