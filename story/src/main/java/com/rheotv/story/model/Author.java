package com.rheotv.story.model;

import java.io.Serializable;
import java.util.List;

public class Author implements Serializable {

    String authorId;

    private String name;

    private String profileUrl;

    private String createdAt;

    private List<Story> storyList;

    public Author(String authorId, String name, String profileUrl, String createdAt, List<Story> storyList) {
        this.authorId = authorId;
        this.name = name;
        this.profileUrl = profileUrl;
        this.createdAt = createdAt;
        this.storyList = storyList;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getName() {
        return name;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public List<Story> getStoryList() {
        return storyList;
    }
}
