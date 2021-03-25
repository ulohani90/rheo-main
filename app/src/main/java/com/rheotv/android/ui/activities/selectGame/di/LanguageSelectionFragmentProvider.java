package com.rheotv.android.ui.activities.selectGame.di;

import com.rheotv.android.ui.activities.selectGame.LanguageSelectionFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class LanguageSelectionFragmentProvider {
    @ContributesAndroidInjector(modules = LanguageSelectionFragmentModule.class)
    abstract LanguageSelectionFragment languageSelectionFragment();
}
