package com.katic.githubapp.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.katic.api.ApiRepository
import com.katic.api.ApiService
import com.katic.githubapp.util.LoadingResult
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class SearchViewModel @Inject constructor(private val apiRepository: ApiRepository) : ViewModel() {

    /** Result of repository search. */
    val searchResult: LiveData<LoadingResult<ApiRepository.RepositoriesPaginator>> get() = _searchResult
    private val _searchResult =
        MutableLiveData<LoadingResult<ApiRepository.RepositoriesPaginator>>()

    private var searchDisposable: Disposable? = null

    /** List of sort keys. */
    val sortItems = listOf(
        ApiService.RepositorySort.STARS,
        ApiService.RepositorySort.FORKS,
        ApiService.RepositorySort.UPDATED
    )

    var selectedSortItem: String = sortItems[0]

    /** Fetch repositories. */
    fun fetchRepositories(query: String?) {
        Timber.d("fetchRepositories: query: $query")
        searchDisposable?.dispose()

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
        Timber.d("onSelectedSortChange: $sort")
        selectedSortItem = sort ?: sortItems[0]
        fetchRepositories(_searchResult.value?.data?.query)
    }

    /** Load next page of repositories. */
    fun fetchRepositoriesNext() {
        val paginator = _searchResult.value?.data
        Timber.d("fetchRepositoriesNext: $paginator")
        if (paginator != null) {
            fetchRepositories(paginator)
        }
    }

    private fun fetchRepositories(paginator: ApiRepository.RepositoriesPaginator) {
        Timber.d("fetchRepositories: $paginator")

        searchDisposable?.dispose()

        searchDisposable = apiRepository.fetchRepositories(paginator)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { pg ->
                    Timber.d("fetchRepositories: $pg")
                    // add loaded repo to all repos
                    pg.allItems.addAll(pg.loadedItems)
                    // signal to observers that operation is done
                    _searchResult.value = LoadingResult.loaded(pg)
                },
                { throwable ->
                    Timber.e(throwable, "fetchRepositories")
                    // signal to observers that operation ended in error
                    _searchResult.value = LoadingResult.exception(_searchResult.value, throwable)
                }
            )
    }

    override fun onCleared() {
        Timber.d("onCleared")
        searchDisposable?.dispose()
    }

}