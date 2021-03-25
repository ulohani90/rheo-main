/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 1/1/19 10:38 AM
 *
 */

package com.rheotv.android.ui.activities.tabcontainer.posts;


public class PostEmptyItemViewModel {

    private PostEmptyItemViewModelListener mListener;

    public PostEmptyItemViewModel(PostEmptyItemViewModelListener listener) {
        this.mListener = listener;
    }
    public void onRetryClick() {
        mListener.onRetryClick();
    }

    public interface PostEmptyItemViewModelListener {

        void onRetryClick();
    }
}
