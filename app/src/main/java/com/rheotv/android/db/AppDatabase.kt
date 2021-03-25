package com.rheotv.android.db

import android.content.Context
import androidx.room.*

@Database(entities = [ClipItem::class, AppPushNotification::class, UserFollowItem::class], version = 5, exportSchema = false)
@TypeConverters(Converter.AuthorConverter::class, Converter.AuthorConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun clipDao(): ClipDao

    abstract fun notificationDao(): PushNotificationDao

    abstract fun userFollowDao(): UserFollowDao

    companion object {

        // For Singleton instantiation
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build()
        }

    }
}