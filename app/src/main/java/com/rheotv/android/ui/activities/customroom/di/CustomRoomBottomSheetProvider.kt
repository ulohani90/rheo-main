package com.rheotv.android.ui.activities.customroom.di

import com.rheotv.android.ui.activities.customroom.view.CustomRoomBottomSheet
import com.rheotv.android.ui.activities.customroom.view.CustomRoomFragment
import com.rheotv.android.ui.activities.customroom.view.CustomRoomDetailFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class CustomRoomBottomSheetProvider {
    @ContributesAndroidInjector(modules = [CustomRoomBottomSheetModule::class])
    abstract fun customRoomBottomSheet(): CustomRoomBottomSheet?

    @ContributesAndroidInjector(modules = [CustomRoomPlayerFragmentModule::class])
    abstract fun customRoomDetailFragment(): CustomRoomDetailFragment?

    @ContributesAndroidInjector(modules = [CustomRoomFragmentModule::class])
    abstract fun customRoomFragment(): CustomRoomFragment?

//    @ContributesAndroidInjector(modules = [])
//    abstract fun customRoomWinnerDialogFragment()
}