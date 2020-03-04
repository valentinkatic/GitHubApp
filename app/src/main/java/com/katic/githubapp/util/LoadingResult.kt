package com.katic.githubapp.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer

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
    }
}

/**
 * Extension function for [LiveData] which returns new LiveData that filters out consumed loaded results.
 */
fun <Y, T : LoadingResult<Y>> LiveData<T>.filterConsumedLoaded(observerId: Int): LiveData<T> =
    FilterMediatorLiveData(this) {
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

/**
 * [MediatorLiveData] that emits data from source but filters them through [filter]
 * before delivering to attached observer.
 */
private class FilterMediatorLiveData<T>(source: LiveData<T>, private val filter: (T) -> Boolean) :
    MediatorLiveData<T>() {

    init {
        // connect it to original LiveData and pass values
        addSource(source) { value -> this.value = value }
    }

    private val filtersMap: MutableMap<Observer<in T>, FilterObserver<in T>> = mutableMapOf()

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        // wrap given observer in our FilterObserver to filter emitted data
        val filter = FilterObserver(observer, filter)
        super.observe(owner, filter)
        filtersMap[observer] = filter
    }

    override fun observeForever(observer: Observer<in T>) {
        // wrap given observer in our FilterObserver to filter emitted data
        val filter = FilterObserver(observer, filter)
        super.observeForever(filter)
        filtersMap[observer] = filter
    }

    override fun removeObserver(observer: Observer<in T>) {
        // we need to remove our wrapper FilterObserver
        // and also remove it as observer
        filtersMap.remove(observer)?.also {
            super.removeObserver(it)
        }
    }
}

/**
 * [Observer] that wraps [delegate] observer and filters emitted data through [filter].
 */
private class FilterObserver<T>(
    private val delegate: Observer<in T>,
    private val filter: (T) -> Boolean
) : Observer<T> {
    override fun onChanged(t: T) {
        if (filter(t)) {
            delegate.onChanged(t)
        }
    }

}