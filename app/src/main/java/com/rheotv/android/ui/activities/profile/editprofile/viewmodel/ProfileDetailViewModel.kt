package com.rheotv.android.ui.activities.profile.editprofile.viewmodel

import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import com.rheotv.android.data.DataManager
import com.rheotv.android.data.network.models.useProfile.responses.ProfileResult
import com.rheotv.android.ui.base.BaseViewModel
import com.rheotv.android.utils.rx.SchedulerProvider

class ProfileDetailViewModel constructor(
        dataManager: DataManager,
        schedulerProvider: SchedulerProvider
) : BaseViewModel<Any>(dataManager, schedulerProvider), Observable {
    private val callbacks = PropertyChangeRegistry()
    var profile: ProfileResult? = null
    var source: String? = null

    var fullName: String?
        @Bindable
        get() = profile?.user?.fullName
        set(value) {
            profile?.user?.fullName = value
        }

    var username: String?
        @Bindable
        get() = profile?.user?.username
        set(value) {
            profile?.user?.username = value
        }

    var intro: String?
        @Bindable
        get() = profile?.intro
        set(value) {
            profile?.intro = value
        }

    var bio: String?
        @Bindable
        get() = profile?.bio
        set(value) {
            profile?.bio = value
        }

    var configurationUsed: String?
        @Bindable
        get() = profile?.profileDetail?.configurationUsed
        set(value) {
            profile?.profileDetail?.configurationUsed = value
        }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        callbacks.remove(callback)
    }

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        callbacks.add(callback)
    }

    /**
     * Notifies observers that all properties of this instance have changed.
     */
    internal fun notifyChange() {
        callbacks.notifyCallbacks(this, 0, null)
    }

    /**
     * Notifies observers that a specific property has changed. The getter for the
     * property that changes should be marked with the @Bindable annotation to
     * generate a field in the BR class to be used as the fieldId parameter.
     *
     * @param fieldId The generated BR id for the Bindable field.
     */
    internal fun notifyPropertyChanged(fieldId: Int) {
        callbacks.notifyCallbacks(this, fieldId, null)
    }

}