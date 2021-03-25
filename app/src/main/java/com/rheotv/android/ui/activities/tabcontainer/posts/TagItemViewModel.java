package com.rheotv.android.ui.activities.tabcontainer.posts;


import android.util.Log;

public class TagItemViewModel {

    public final String origin;

    public TagItemViewModel(String origin) {
        this.origin = "#" + origin;
        Log.d("KKKK", "origin : " + origin);
    }

    public void onItemClick() {
//        mBlogitemViewModelListener.onItemClick(mListItem.getId());
    }


}


