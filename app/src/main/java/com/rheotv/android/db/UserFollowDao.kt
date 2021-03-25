package com.rheotv.android.db

import androidx.room.*

@Dao
interface UserFollowDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun updateUserEntry(userFollowItem: UserFollowItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun updateMultipleUserEntry(userFollowItem: List<UserFollowItem>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertUserWithIgnore(userFollowItem: UserFollowItem): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMultipleUserWithIgnore(userFollowItem: List<UserFollowItem>): List<Long>

    @Query("Select * from user_follow where user_follow.user_id=:userId")
    fun checkIfIsFollowedWithUserId(userId: Int): UserFollowItem?

    @Query("Select * from user_follow where user_follow.user_name=:username")
    fun checkIfIsFollowedWithUsername(username: String): UserFollowItem?

    @Query("Select * from user_follow where user_name in (:filterValues)")
    fun fetchFollowerList(filterValues: List<String>): List<UserFollowItem>
}