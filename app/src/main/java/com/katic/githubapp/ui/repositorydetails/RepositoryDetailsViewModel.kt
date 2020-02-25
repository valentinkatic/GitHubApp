package com.katic.githubapp.ui.repositorydetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.katic.api.ApiRepository
import com.katic.api.log.Log
import com.katic.api.model.Repository
import com.katic.githubapp.util.LoadingResult
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.lang.Exception

class RepositoryDetailsViewModel(
    private val apiRepository: ApiRepository,
    private val user: String?,
    private val repo: String?
) : ViewModel() {

    companion object {
        private val log = Log.getLog("RepositoryDetailsViewModel")
    }

    val repoResult: LiveData<LoadingResult<Repository>> get() = _repoResult
    private val _repoResult = MutableLiveData<LoadingResult<Repository>>()

    private var repoDisposable: Disposable? = null

    init {
        if (Log.LOG) log.d("init")
        // load repo
        fetchRepo()
    }

    private fun fetchRepo() {
        if (Log.LOG) log.d("fetchRepo: $user, $repo")

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
                    if (Log.LOG) log.d("repository: $repository")
                    // signal to observers that operation is done
                    _repoResult.value = LoadingResult.loaded(repository)
                },
                { throwable ->
                    if (Log.LOG) log.e("fetchRepo", throwable)
                    // signal to observers that operation ended in error
                    _repoResult.value = LoadingResult.exception(_repoResult.value, throwable)
                }
            )
    }

    override fun onCleared() {
        if (Log.LOG) log.d("onCleared")
        repoDisposable?.dispose()
    }

}