package com.katic.githubapp.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.katic.api.ApiRepository
import com.katic.api.ApiService
import com.katic.api.log.Log
import com.katic.githubapp.util.LoadingResult
import com.katic.githubapp.util.runCatchCancel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SearchViewModel(private val apiRepository: ApiRepository) : ViewModel() {

    companion object {
        private val log = Log.getLog("SearchViewModel")
    }

    /** Result of repository search. */
    val searchResult: LiveData<LoadingResult<ApiRepository.RepositoriesPaginator>> get() = _searchResult
    private val _searchResult =
        MutableLiveData<LoadingResult<ApiRepository.RepositoriesPaginator>>()

    private var searchJob: Job? = null

    /** List of sort keys. */
    val sortItems = listOf(
        ApiService.RepositorySort.STARS,
        ApiService.RepositorySort.FORKS,
        ApiService.RepositorySort.UPDATED
    )

    var selectedSortItem: String = sortItems[0]

    /** Fetch repositories. */
    fun fetchRepositories(query: String?) {
        if (Log.LOG) log.d("fetchRepositories: query: $query")

        searchJob?.cancel()

        // signal to observers that loading is in progress
        _searchResult.value = LoadingResult.loading(_searchResult.value)

        return fetchRepositories(
            ApiRepository.RepositoriesPaginator(
                query = query ?: "",
                sort = selectedSortItem
            )
        )
    }

    /** Should be called by view when sort is changed by the user to reload repositories. */
    fun onSelectedSortChange(sort: String?) {
        if (Log.LOG) log.d("onSelectedSortChange: $sort")
        selectedSortItem = sort ?: sortItems[0]
        fetchRepositories(_searchResult.value?.data?.query)
    }

    /** Load next page of repositories. */
    fun fetchRepositoriesNext() {
        val paginator = _searchResult.value?.data
        if (Log.LOG) log.d("fetchRepositoriesNext: $paginator")
        if (paginator != null) {
            fetchRepositories(paginator)
        }
    }

    private fun fetchRepositories(paginator: ApiRepository.RepositoriesPaginator) {
        if (Log.LOG) log.d("fetchRepositories: $paginator")

        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            runCatchCancel(
                run = {
                    val pg = apiRepository.fetchRepositories(paginator)
                    if (Log.LOG) log.d("fetchRepositories: $pg")
                    // add loaded repo to all repos
                    pg.allItems.addAll(pg.loadedItems)
                    // signal to observers that operation is done
                    _searchResult.value = LoadingResult.loaded(pg)
                },
                catch = { t ->
                    if (Log.LOG) log.e("fetchRepositories", t)
                    // signal to observers that operation ended in error
                    _searchResult.value = LoadingResult.exception(_searchResult.value, t)
                },
                cancel = {
                    if (Log.LOG) log.i("fetchRepositories canceled")
                }
            )
        }
    }

    override fun onCleared() {
        if (Log.LOG) log.d("onCleared")
        super.onCleared()
    }

}