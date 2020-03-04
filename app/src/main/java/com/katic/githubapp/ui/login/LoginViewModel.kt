package com.katic.githubapp.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.katic.api.AuthRepository
import com.katic.api.log.Log
import com.katic.api.model.TokenResponse
import com.katic.githubapp.BuildConfig
import com.katic.githubapp.util.LoadingResult
import com.katic.githubapp.util.ServiceInterceptor
import com.katic.githubapp.util.runCatchCancel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val serviceInterceptor: ServiceInterceptor
) : ViewModel() {

    companion object {
        private val log = Log.getLog("LoginViewModel")
    }

    val tokenResult: LiveData<LoadingResult<TokenResponse>> get() = _tokenResult
    private val _tokenResult = MutableLiveData<LoadingResult<TokenResponse>>()

    private var tokenJob: Job? = null

    fun fetchToken(code: String) {
        if (Log.LOG) log.d("fetchToken: $code")

        tokenJob?.cancel()

        tokenJob = viewModelScope.launch {
            runCatchCancel(
                run = {
                    val tokenResponse = authRepository.fetchToken(
                        BuildConfig.APPCI,
                        BuildConfig.APPCS,
                        code
                    )
                    if (Log.LOG) log.d("tokenResponse: $tokenResponse")
                    serviceInterceptor.token = tokenResponse.token
                    // signal to observers that operation is done
                    _tokenResult.value = LoadingResult.loaded(tokenResponse)
                },
                catch = { t ->
                    if (Log.LOG) log.e("fetchToken", t)
                    // signal to observers that operation ended in error
                    _tokenResult.value = LoadingResult.exception(_tokenResult.value, t)
                },
                cancel = {
                    if (Log.LOG) log.i("fetchToken canceled")
                }
            )
        }
    }
}