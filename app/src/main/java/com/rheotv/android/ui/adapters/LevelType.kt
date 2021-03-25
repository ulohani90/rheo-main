package com.rheotv.android.ui.adapters

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class AchievementType {
    object Level : AchievementType() {
        override fun toString(): String {
            return "0"
        }
    }

    object Bonus : AchievementType() {
        override fun toString(): String {
            return "1"
        }
    }
}

sealed class LevelType : Parcelable {
    @Parcelize
    object Unassigned : LevelType() {
        override fun toString(): String {
            return "Unassigned"
        }
    }

    @Parcelize
    object Bronze : LevelType() {
        override fun toString(): String = "Bronze"
    }

    @Parcelize
    object Silver : LevelType() {
        override fun toString(): String = "Silver"
    }

    @Parcelize
    object Gold : LevelType() {
        override fun toString(): String = "Gold"
    }

    companion object {
        fun getLevelType(levelType: String?): LevelType =
                when (levelType) {
                    Bronze.toString() -> Bronze
                    Silver.toString() -> Silver
                    Gold.toString() -> Gold
                    else -> Unassigned
                }
    }
}