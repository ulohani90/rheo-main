/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 03/01/19 01:16 AM
 *
 */

package com.rheotv.android.ui.activities.tabcontainer.posts;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
;

@Module
public abstract class PostListFragmentProvider {

    @ContributesAndroidInjector(modules = PostListFragmentModule.class)
    abstract PostListFragment provideBlogFragmentFactory();
}
