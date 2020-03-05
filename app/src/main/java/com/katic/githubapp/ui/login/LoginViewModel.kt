package com.katic.githubapp.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.katic.api.AuthRepository
import com.katic.api.model.TokenResponse
import com.katic.githubapp.BuildConfig
import com.katic.githubapp.util.LoadingResult
import com.katic.githubapp.util.ServiceInterceptor
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val serviceInterceptor: ServiceInterceptor
) : ViewModel() {

    val tokenResult: LiveData<LoadingResult<TokenResponse>> get() = _tokenResult
    private val _tokenResult = MutableLiveData<LoadingResult<TokenResponse>>()

    private var tokenDisposable: Disposable? = null

    fun fetchToken(code: String) {
        Timber.d("fetchToken: $code")

        tokenDisposable?.dispose()

        tokenDisposable = authRepository.fetchToken(
            BuildConfig.APPCI,
            BuildConfig.APPCS,
            code
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { tokenResponse ->
                    Timber.d("tokenResponse: $tokenResponse")
                    serviceInterceptor.token = tokenResponse.token
                    // signal to observers that operation is done
                    _tokenResult.value = LoadingResult.loaded(tokenResponse)
                },
                { throwable ->
                    Timber.e(throwable, "fetchToken")
                    // signal to observers that operation ended in error
                    _tokenResult.value = LoadingResult.exception(_tokenResult.value, throwable)
                }
            )
    }
}