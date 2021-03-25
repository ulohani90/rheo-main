package com.rheotv.story;

import com.rheotv.story.model.Story;

public interface StoryCallback {
    void done();

    void backward();

    void dismiss();

    void watched(Story story);

    void moreOption(Story story);

    void viewAuthorProfile(String authorName);

    void viewMentionedProfile(String authorName);

    void onInterest(Story story);

    void onMentionFollow(Story story, String author, String authorId, boolean isFollowing);
}
