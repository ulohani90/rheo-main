package com.rheotv.android.ui.activities.customroom.model

sealed class CustomRoomUserAction {
    object AddCustomRoomClick : CustomRoomUserAction() {
        override var headerText: String? = "New Custom Room"
    }

    object DetailPageBackClick : CustomRoomUserAction() {
        override var headerText: String? = ""
    }

    object CustomRoomViewClick : CustomRoomUserAction() {
        override var headerText: String? = null
    }

    object SubmitRoomIdPasswordClick : CustomRoomUserAction() {
        override var headerText: String? = null
    }

    object CreateCustomRoomClick : CustomRoomUserAction() {
        override var headerText: String? = null
    }

    object SubmitGameUserName : CustomRoomUserAction() {
        override var headerText: String? = null
    }

    object UpdateCustomRoom : CustomRoomUserAction() {
        override var headerText: String? = null
    }

    object RemoveWinner : CustomRoomUserAction() {
        override var headerText: String? = null

    }

    object RefreshPlayerList : CustomRoomUserAction() {
        override var headerText: String? = null
    }

    object RefreshCustomRoom : CustomRoomUserAction() {
        override var headerText: String? = null
    }

    object SubmitUpdatedStartTime : CustomRoomUserAction() {
        override var headerText: String? = null
    }

    abstract var headerText: String?
}