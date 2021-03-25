package com.rheotv.android.utils.hourglass

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

abstract class HourglassAsync(private var startValue: Long, private var timeUnit: TimeUnit) {
    private var disposable: Disposable? = null

    abstract fun onTimerTick(remainingTime: Long)

    abstract fun onTimerFinish()

    fun startTimer() =
            Observable.zip(
                    Observable.rangeLong(0, startValue),
                    Observable.interval(1, timeUnit),
                    { _, _ ->
                        startValue -= 1
                        startValue
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Observer<Long?> {
                        override fun onSubscribe(d: Disposable) {
                            disposable = d
                        }

                        override fun onNext(remainingTime: Long) {
                            if (remainingTime < 0) {
                                onComplete()
                                return
                            }
                            onTimerTick(remainingTime)
                        }

                        override fun onError(e: Throwable) {
                            e.printStackTrace()
                        }

                        override fun onComplete() {
                            if (startValue <= 0)
                                onTimerFinish()
                        }
                    })

    fun isPaused(): Boolean = disposable?.isDisposed ?: true

    fun isRunning(): Boolean = !isPaused()

    fun stopTimer() = disposable?.dispose()
}