package com.rheotv.android.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object Converter {

    class AuthorConverter {
        @TypeConverter
        fun fromAuthor(value: Author): String {
            val gson = Gson()
            val type = object : TypeToken<Author>() {}.type
            return gson.toJson(value, type)
        }

        @TypeConverter
        fun toAuthor(value: String): Author {
            val gson = Gson()
            val type = object : TypeToken<Author>() {}.type
            return gson.fromJson(value, type)
        }
    }

    class UserConverter {
        @TypeConverter
        fun fromAuthor(value: User): String {
            val gson = Gson()
            val type = object : TypeToken<User>() {}.type
            return gson.toJson(value, type)
        }

        @TypeConverter
        fun toAuthor(value: String): User {
            val gson = Gson()
            val type = object : TypeToken<User>() {}.type
            return gson.fromJson(value, type)
        }
    }


}