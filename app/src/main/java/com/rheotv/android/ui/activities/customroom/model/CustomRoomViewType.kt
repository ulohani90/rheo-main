package com.rheotv.android.ui.activities.customroom.model

import java.util.*

sealed class CustomRoomViewType {
    object CustomRoomCreated : CustomRoomViewType() {

        override val value: Int = 0x0000
        override val name: String = "CREATED"
    }

    object CustomRoomFilled : CustomRoomViewType() {
        override val value: Int = 0x0001
        override val name: String = "FILLED"
    }

    object CustomRoomStarted : CustomRoomViewType() {
        override val value: Int = 0x0002
        override val name: String = "STARTED"
    }

    object CustomRoomEnded : CustomRoomViewType() {
        override val value: Int = 0x0003
        override val name: String = "ENDED"
    }

    object CustomRoomRefunded : CustomRoomViewType() {
        override val value: Int = 0x0004
        override val name: String = "REFUNDED"
    }

    abstract val value: Int
    abstract val name: String

    companion object {
        fun getViewType(name: String?): CustomRoomViewType = when (name) {
            CustomRoomFilled.name.toLowerCase(Locale.getDefault()) -> CustomRoomFilled
            CustomRoomStarted.name.toLowerCase(Locale.getDefault()) -> CustomRoomStarted
            CustomRoomEnded.name.toLowerCase(Locale.getDefault()) -> CustomRoomEnded
            CustomRoomRefunded.name.toLowerCase(Locale.getDefault()) -> CustomRoomRefunded
            else -> CustomRoomCreated
        }
    }
}