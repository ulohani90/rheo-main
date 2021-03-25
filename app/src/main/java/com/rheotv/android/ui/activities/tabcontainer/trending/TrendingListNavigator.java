package com.rheotv.android.ui.activities.tabcontainer.trending;

import com.rheotv.android.data.network.models.postlisting.responses.PostListingResponse;

public interface TrendingListNavigator {
    void handleError(Throwable throwable);

    void updateBlog(PostListingResponse postListingResponse);

    void switchFragment(String id);

    void updateCoins();
}

