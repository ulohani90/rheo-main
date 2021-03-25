package com.rheotv.android.di.keys

import androidx.work.ListenableWorker
import androidx.work.Worker
import dagger.MapKey
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@MapKey
annotation class WorkerKey(val value: KClass<out ListenableWorker>)