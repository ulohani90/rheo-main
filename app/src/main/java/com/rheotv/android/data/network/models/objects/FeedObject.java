package com.rheotv.android.data.network.models.objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.rheotv.android.data.network.models.useProfile.responses.ProfileResult;

import java.util.ArrayList;
import java.util.List;

public class FeedObject {

    @SerializedName("type")
    @Expose
    int type;

    @SerializedName("count")
    @Expose
    int count;

    @SerializedName("title")
    @Expose
    String title;

    @SerializedName("posts")
    @Expose
    List<PostObject> posts;

    @SerializedName("games")
    @Expose
    List<GameObject> games;

    @SerializedName("streamers")
    @Expose
    List<StreamerObject> streamers;

    @SerializedName("story_author")
    @Expose
    ArrayList<ProfileResult> storyAuthors;

    @SerializedName("post")
    @Expose
    PostObject post;

    @SerializedName("game")
    @Expose
    GameObject game;

    @SerializedName("game_id")
    @Expose
    String gameId;

    public FeedObject(int type, ArrayList<ProfileResult> storyAuthors) {
        this.type = type;
        this.storyAuthors = storyAuthors;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<PostObject> getPosts() {
        return posts;
    }

    public void setPosts(List<PostObject> posts) {
        this.posts = posts;
    }

    public List<GameObject> getGames() {
        return games;
    }

    public void setGames(List<GameObject> games) {
        this.games = games;
    }

    public List<StreamerObject> getStreamers() {
        return streamers;
    }

    public void setStreamers(List<StreamerObject> streamers) {
        this.streamers = streamers;
    }

    public PostObject getPost() {
        return post;
    }

    public void setPost(PostObject post) {
        this.post = post;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public GameObject getGame() {
        return game;
    }

    public void setGame(GameObject game) {
        this.game = game;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public ArrayList<ProfileResult> getStoryAuthors() {
        return storyAuthors;
    }

    public void setStoryAuthors(ArrayList<ProfileResult> storyAuthors) {
        this.storyAuthors = storyAuthors;
    }
}
