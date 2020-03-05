package com.katic.githubapp.ui.repositorydetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.katic.api.ApiRepository
import com.katic.api.model.Repository
import com.katic.githubapp.util.LoadingResult
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.lang.Exception

class RepositoryDetailsViewModel(
    private val apiRepository: ApiRepository,
    private val user: String?,
    private val repo: String?
) : ViewModel() {

    val repoResult: LiveData<LoadingResult<Repository>> get() = _repoResult
    private val _repoResult = MutableLiveData<LoadingResult<Repository>>()

    private var repoDisposable: Disposable? = null

    init {
        Timber.d("init")
        // load repo
        fetchRepo()
    }

    private fun fetchRepo() {
        Timber.d("fetchRepo: $user, $repo")

        repoDisposable?.dispose()

        if (user == null || repo == null) {
            _repoResult.value =
                LoadingResult.exception(_repoResult.value, Exception("Missing parameters"))
            return
        }

        repoDisposable = apiRepository.fetchRepository(user, repo)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { repository ->
                    Timber.d("repository: $repository")
                    // signal to observers that operation is done
                    _repoResult.value = LoadingResult.loaded(repository)
                },
                { throwable ->
                    Timber.e(throwable, "fetchRepo")
                    // signal to observers that operation ended in error
                    _repoResult.value = LoadingResult.exception(_repoResult.value, throwable)
                }
            )
    }

    override fun onCleared() {
        Timber.d("onCleared")
        repoDisposable?.dispose()
    }

}