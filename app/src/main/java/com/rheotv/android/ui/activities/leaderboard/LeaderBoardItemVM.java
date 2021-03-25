package com.rheotv.android.ui.activities.leaderboard;


import androidx.databinding.ObservableField;

import com.rheotv.android.data.network.models.postlisting.responses.Author;
import com.rheotv.android.utils.NumberUtils;

public class LeaderBoardItemVM {
    private Author author;
    public ObservableField<String> imageUrl = new ObservableField<>();
    public ObservableField<String> name = new ObservableField<>();
    public ObservableField<String> rank = new ObservableField<>();
    public ObservableField<String> userName = new ObservableField<>();
    public ObservableField<String> totalCount = new ObservableField<>();
    public ObservableField<String> displayAttribute = new ObservableField<>();
    public ObservableField<String> followCount = new ObservableField<>();
    public ObservableField<Boolean> isFollowed = new ObservableField<>();
    public ObservableField<Boolean> canFollow = new ObservableField<>(true);

    public void setData(Author result, boolean isForFollower) {
        this.author = result;
        imageUrl.set(result.getUser().getProfilePic());
        name.set(result.getUser().getUserFullName());
        rank.set(result.getUser().getUsername());
        userName.set(result.getUser().getUsername());
        if (isForFollower) {
            if (result.getFollowersCount() != null)
                followCount.set(NumberUtils.getFormattedCount(result.getFollowersCount()));
        } else {
            displayAttribute.set(NumberUtils.getFormattedCount(result.getDisplayAttribute()) + " " + result.getDisplayUnit());
        }
        isFollowed.set(result.getUser().isFollowed());
    }

    public ObservableField<Boolean> getIsFollowed() {
        return isFollowed;
    }

    public void setIsFollowed(boolean isFollowed) {
        this.isFollowed.set(isFollowed);
        if (author != null && author.getUser() != null) {
            author.getUser().setFollowed(isFollowed);
        }
    }

    public void setCanFollow(boolean canFollow) {
        this.canFollow.set(canFollow);
    }

    public ObservableField<Boolean> getCanFollow() {
        return canFollow;
    }
}
