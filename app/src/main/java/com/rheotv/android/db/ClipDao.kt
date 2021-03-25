package com.rheotv.android.db

import androidx.room.*
import io.reactivex.Observable

@Dao
interface ClipDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertClip(clip: ClipItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertClips(clip: List<ClipItem>)

    @Delete
    fun deleteClip(clip: ClipItem)

    @Query("DELETE FROM clip")
    suspend fun deleteAllClips()

    @Query("SELECT * FROM clip")
    fun getClips(): Observable<List<ClipItem>>

    @Query("SELECT clip.videoUrl  FROM clip")
    suspend fun getClipVideoUrlList(): List<String>

}