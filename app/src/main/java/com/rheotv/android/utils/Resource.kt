package com.rheotv.android.utils

data class Resource<out T>(var status: Status, val data: T?, val message: String?, val code: Int) {
    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null, -1)
        }

        fun <T> error(msg: String?, code: Int): Resource<T> {
            return Resource(Status.ERROR, null, msg, code)
        }

        fun <T> loading(): Resource<T> {
            return Resource(Status.LOADING, null, null, -1)
        }
    }
}