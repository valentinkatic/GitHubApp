package com.katic.githubapp.ui.userdetails

import android.webkit.CookieManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.katic.api.ApiRepository
import com.katic.api.model.User
import com.katic.githubapp.util.LoadingResult
import com.katic.githubapp.util.ServiceInterceptor
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class UserDetailsViewModel(
    private val apiRepository: ApiRepository,
    private val serviceInterceptor: ServiceInterceptor,
    private val user: String?
) : ViewModel() {

    val userResult: LiveData<LoadingResult<User>> get() = _userResult
    private val _userResult = MutableLiveData<LoadingResult<User>>()

    private var userDisposable: Disposable? = null

    init {
        Timber.d("init")
        // load user
        fetchUser()
    }

    private fun fetchUser() {
        Timber.d("fetchUser: $user")

        userDisposable?.dispose()

        userDisposable =
            (if (user == null) apiRepository.fetchCurrentUser() else apiRepository.fetchUser(user))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { user ->
                        Timber.d("user: $user")
                        // signal to observers that operation is done
                        _userResult.value = LoadingResult.loaded(user)
                    },
                    { throwable ->
                        Timber.e(throwable, "fetchUser")
                        // signal to observers that operation ended in error
                        _userResult.value = LoadingResult.exception(_userResult.value, throwable)
                    }
                )
    }

    fun logout() {
        val cookieManager: CookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies(null)
        cookieManager.flush()
        serviceInterceptor.token = null
    }

    override fun onCleared() {
        Timber.d("onCleared")
        userDisposable?.dispose()
    }

}