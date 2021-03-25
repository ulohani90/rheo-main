/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 03/01/19 01:16 AM
 *
 */

package com.rheotv.android.ui.activities.tabcontainer.posts;

import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.ui.activities.tabcontainer.posts.multiviewitem.MultiViewRecyclerAdapter;
import com.rheotv.android.ui.adapters.PostListAdapter;
import com.rheotv.android.ui.adapters.CarouselAdapter;

import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.ArrayList;

import dagger.Module;
import dagger.Provides;


@Module
public class PostListFragmentModule {

    @Provides
    PostViewModel blogViewModel(DataManager dataManager,
                                SchedulerProvider schedulerProvider) {
        return new PostViewModel(dataManager, schedulerProvider);
    }

    @Provides
    PostListAdapter provideBlogAdapter() {
        return new PostListAdapter(new ArrayList<>());
    }

    @Provides
    MultiViewRecyclerAdapter provideMultiViewRecyclerAdapter() {
        return new MultiViewRecyclerAdapter(new ArrayList<>());
    }

    @Provides
    CarouselAdapter provideCarouselAdapter() {
        return new CarouselAdapter(new ArrayList<>());
    }

    @Provides
    ViewModelProvider.Factory provideBlogViewModel(PostViewModel blogViewModel) {
        return new ViewModelProviderFactory<>(blogViewModel);
    }

}
