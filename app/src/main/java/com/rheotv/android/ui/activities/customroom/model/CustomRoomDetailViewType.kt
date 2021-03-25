package com.rheotv.android.ui.activities.customroom.model

sealed class CustomRoomDetailViewType {
    object Requested : CustomRoomDetailViewType() {
        override val value: Int = 0x0000
    }

    object CreateCustomRoom : CustomRoomDetailViewType() {
        override val value: Int = 0x0001
        var customRoomCount = -1
    }

    object RequestRoomIdAndPassword : CustomRoomDetailViewType() {
        override val value: Int = 0x0002
    }

    object ShowRoomIdAndPassword : CustomRoomDetailViewType() {
        override val value: Int = 0x0003
    }

    object GameUserInput : CustomRoomDetailViewType() {
        override val value: Int = 0x0006
    }

    abstract val value: Int
}