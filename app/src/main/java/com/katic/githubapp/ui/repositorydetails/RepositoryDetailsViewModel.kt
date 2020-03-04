package com.katic.githubapp.ui.repositorydetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.katic.api.ApiRepository
import com.katic.api.log.Log
import com.katic.api.model.Repository
import com.katic.githubapp.util.LoadingResult
import com.katic.githubapp.util.runCatchCancel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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

    private var repoJob: Job? = null

    init {
        if (Log.LOG) log.d("init")
        // load repo
        fetchRepo()
    }

    private fun fetchRepo() {
        if (Log.LOG) log.d("fetchRepo: $user, $repo")

        repoJob?.cancel()

        if (user == null || repo == null) {
            _repoResult.value =
                LoadingResult.exception(_repoResult.value, Exception("Missing parameters"))
            return
        }

        repoJob = viewModelScope.launch {
            runCatchCancel(
                run = {
                    val repository = apiRepository.fetchRepository(user, repo)
                    if (Log.LOG) log.d("repository: $repository")
                    // signal to observers that operation is done
                    _repoResult.value = LoadingResult.loaded(repository)
                },
                catch = { t ->
                    if (Log.LOG) log.e("fetchRepo", t)
                    // signal to observers that operation ended in error
                    _repoResult.value = LoadingResult.exception(_repoResult.value, t)
                },
                cancel = {
                    if (Log.LOG) log.i("fetchRepo canceled")
                }
            )
        }
    }

    override fun onCleared() {
        if (Log.LOG) log.d("onCleared")
        super.onCleared()
    }

}