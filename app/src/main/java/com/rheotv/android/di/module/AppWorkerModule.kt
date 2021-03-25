package com.rheotv.android.di.module

import com.rheotv.android.di.keys.WorkerKey
import com.rheotv.android.factories.ChildWorkerFactory
import com.rheotv.android.utils.worker.ClearNotificationWorker
import com.rheotv.android.utils.worker.ClipSyncWorker
import com.rheotv.android.utils.worker.OfflineNotificationWorker
import com.rheotv.android.utils.worker.SyncFcmTokenWorker
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class AppWorkerModule {

    @Binds
    @IntoMap
    @WorkerKey(ClipSyncWorker::class)
    internal abstract fun bindDailyClipWorker(worker: ClipSyncWorker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(OfflineNotificationWorker::class)
    internal abstract fun bindNotificationWorker(worker: OfflineNotificationWorker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(SyncFcmTokenWorker::class)
    internal abstract fun bindSyncFcmTokenWorker(worker: SyncFcmTokenWorker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(ClearNotificationWorker::class)
    internal abstract fun bindClearNotificationWorker(worker: ClearNotificationWorker.Factory): ChildWorkerFactory
}