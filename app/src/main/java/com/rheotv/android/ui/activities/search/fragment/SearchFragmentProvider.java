package com.rheotv.android.ui.activities.search.fragment;


import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class SearchFragmentProvider {
    @ContributesAndroidInjector(modules = SearchFragmentModule.class)
    abstract SearchFragment searchFragment();
}