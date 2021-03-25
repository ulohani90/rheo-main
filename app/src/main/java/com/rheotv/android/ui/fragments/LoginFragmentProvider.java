package com.rheotv.android.ui.fragments;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class LoginFragmentProvider {

    @ContributesAndroidInjector(modules = LoginModule.class)
    abstract LoginFragmentBottomDialog provideBlogFragmentFactory();

    @ContributesAndroidInjector(modules = LoginModule.class)
    abstract LoginFragment provideLoginFragmentFactory();

}