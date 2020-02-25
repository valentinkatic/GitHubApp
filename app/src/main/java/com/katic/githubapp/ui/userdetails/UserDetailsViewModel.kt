package com.katic.githubapp.ui.userdetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.katic.api.ApiRepository
import com.katic.api.log.Log
import com.katic.api.model.User
import com.katic.githubapp.util.LoadingResult
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.lang.Exception

class UserDetailsViewModel(
    private val apiRepository: ApiRepository,
    private val user: String?
) : ViewModel() {

    companion object {
        private val log = Log.getLog("UserDetailsViewModel")
    }

    val userResult: LiveData<LoadingResult<User>> get() = _userResult
    private val _userResult = MutableLiveData<LoadingResult<User>>()

    private var userDisposable: Disposable? = null

    init {
        if (Log.LOG) log.d("init")
        // load user
        fetchUser()
    }

    private fun fetchUser() {
        if (Log.LOG) log.d("fetchUser: $user")

        userDisposable?.dispose()

        if (user == null) {
            _userResult.value =
                LoadingResult.exception(_userResult.value, Exception("Missing parameters"))
            return
        }

        userDisposable = apiRepository.fetchUser(user)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { user ->
                    if (Log.LOG) log.d("user: $user")
                    // signal to observers that operation is done
                    _userResult.value = LoadingResult.loaded(user)
                },
                { throwable ->
                    if (Log.LOG) log.e("fetchUser", throwable)
                    // signal to observers that operation ended in error
                    _userResult.value = LoadingResult.exception(_userResult.value, throwable)
                }
            )
    }

    override fun onCleared() {
        if (Log.LOG) log.d("onCleared")
        userDisposable?.dispose()
    }

}