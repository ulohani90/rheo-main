/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 03/01/19 01:16 AM
 *
 */

package com.rheotv.android.ui.activities.tabcontainer.posts;

import com.rheotv.android.data.network.models.postlisting.responses.PostListingResponse;


public interface PostListNavigator {

    void handleError(Throwable throwable);

    void updateBlog(PostListingResponse postListingResponse);

    void switchFragment(String id);

    void notifyDataSetFromStorage();

    void setRecyclerViewChildrenCount();

    void handleLogin();

    void stopLoading();

    void showProgressBarLoading(String message);

    void hideProgressBarLoading();

    void showToast();

    void setEnableClips(boolean enableClips);

    void setEnableGoLive(boolean enableGoLive);

    void hidePaginationLoader();
}
