package com.rheotv.android.factories;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

public class ViewModelProviderFactoryV2<V> implements ViewModelProvider.Factory {
    private final Map<Class<? extends V>, Provider<V>> mProviderMap;

    @Inject
    public ViewModelProviderFactoryV2(Map<Class<? extends V>, Provider<V>> providerMap) {
        mProviderMap = providerMap;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
//        if (modelClass.isAssignableFrom(mProviderMap.get(modelClass).getClass())) {
        return (T) mProviderMap.get(modelClass).get();
//        }
//        throw new IllegalArgumentException("Unknown class name");
    }
}
