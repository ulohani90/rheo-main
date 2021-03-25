package com.rheotv.android.model

sealed class VideoQuality {
    object Auto : VideoQuality() {
        override fun toString(): String = "Auto"
    }

    object Medium : VideoQuality() {
        override fun toString(): String = "Medium"
    }

    object High : VideoQuality() {
        override fun toString(): String = "High"
    }

    object Low : VideoQuality() {
        override fun toString(): String = "Low"
    }

    object Audio : VideoQuality() {
        override fun toString(): String = "Audio"
    }
}