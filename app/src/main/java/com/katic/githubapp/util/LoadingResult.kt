package com.katic.githubapp.util

import io.reactivex.Observable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.subjects.BehaviorSubject

/**
 * Generic class with status [isLoading], [data]: [T] and/or [exception].
 */
class LoadingResult<T>(val isLoading: Boolean, val data: T?, private var exception: Throwable?) {

    fun getException(clearException: Boolean = true): Throwable? {
        val t = exception
        if (clearException) {
            exception = null
        }
        return t
    }

    val isLoaded: Boolean
        get() = !isLoading

    val isError: Boolean
        get() = exception != null

    internal var consumed = LinkedHashSet<Int>()

    override fun toString(): String {
        return "LoadingResult(isLoading=$isLoading, data=$data, exception=$exception, consumed=$consumed)"
    }

    companion object {

        /**
         * Returns new result with previous data (if any) and [isLoading] true.
         *
         * @param previous previous data or null
         * @param <T> data type
         * @return new result
        </T> */
        fun <T> loading(previous: LoadingResult<T>? = null): LoadingResult<T> {
            return LoadingResult(true, previous?.data, null)
        }

        /**
         * Returns new result with loaded data and [isLoading] false.
         *
         * @param data loaded data
         * @param <T> data type
         * @return new result
        </T> */
        fun <T> loaded(data: T): LoadingResult<T> {
            return LoadingResult(false, data, null)
        }

        /**
         * Returns new result with previous data (if any), exception and [isLoading] false.
         *
         * @param previous previous data or null
         * @param exception exception
         * @param <T> data type
         * @return new result
        </T> */
        fun <T> exception(previous: LoadingResult<T>?, exception: Throwable): LoadingResult<T> {
            return LoadingResult(false, previous?.data, exception)
        }

        fun <T> singleToSubject(subject: BehaviorSubject<LoadingResult<T>>): DisposableSingleObserver<T> {
            return object : DisposableSingleObserver<T>() {
                override fun onSuccess(newData: T) {
                    subject.onNext(loaded(newData))
                }

                override fun onError(e: Throwable) {
                    subject.onNext(exception(subject.value, e))
                }
            }
        }
    }
}

/**
 * Extension function for [Observable] which filters out consumed loaded results.
 */
fun <Y, T: LoadingResult<Y>> Observable<T>.filterConsumedLoaded(observerId: Int): Observable<T> = filter {
    when {
        it.consumed.contains(observerId) -> false // filter out if already consumed by this observer
        it.isLoaded -> {
            // mark consumed
            it.consumed.add(observerId)
            // return true to pass it once
            true
        }
        else -> true // pass it
    }
}
