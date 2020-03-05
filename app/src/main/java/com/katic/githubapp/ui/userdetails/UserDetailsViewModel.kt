package com.katic.githubapp.ui.userdetails

import android.webkit.CookieManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.katic.api.ApiRepository
import com.katic.api.log.Log
import com.katic.api.model.User
import com.katic.githubapp.util.LoadingResult
import com.katic.githubapp.util.ServiceInterceptor
import com.katic.githubapp.util.runCatchCancel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class UserDetailsViewModel(
    private val apiRepository: ApiRepository,
    private val serviceInterceptor: ServiceInterceptor,
    private val user: String?
) : ViewModel() {

    companion object {
        private val log = Log.getLog("UserDetailsViewModel")
    }

    val userResult: LiveData<LoadingResult<User>> get() = _userResult
    private val _userResult = MutableLiveData<LoadingResult<User>>()

    init {
        if (Log.LOG) log.d("init")
        // load user
        fetchUser()
    }

    private fun fetchUser() {
        if (Log.LOG) log.d("fetchUser: $user")

        viewModelScope.launch {
            runCatchCancel(
                run = {
                    val user = (if (user == null) apiRepository.fetchCurrentUser() else apiRepository.fetchUser(user))
                    if (Log.LOG) log.d("user: $user")
                    // signal to observers that operation is done
                    _userResult.value = LoadingResult.loaded(user)
                },
                catch = {
                    t ->
                    if (Log.LOG) log.e("fetchUser", t)
                    // signal to observers that operation ended in error
                    _userResult.value = LoadingResult.exception(_userResult.value, t)
                },
                cancel = {
                    if (Log.LOG) log.i("fetchUser canceled")
                }
            )
        }
    }

    fun logout() {
        val cookieManager: CookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies(null)
        cookieManager.flush()
        serviceInterceptor.token = null
    }
}