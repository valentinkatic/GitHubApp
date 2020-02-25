package com.katic.api

import com.katic.api.model.Repository
import com.katic.api.model.User
import io.reactivex.Single

class ApiRepository(private val service: ApiService) {

    data class RepositoriesPaginator(
        val query: String = "",
        val limit: Int = 15,
        val sort: String = ApiService.RepositorySort.STARS,
        var totalCount: Int = 0,
        var loadedItems: List<Repository> = emptyList(),
        val allItems: MutableList<Repository> = ArrayList()
    )

    fun fetchRepositories(paginator: RepositoriesPaginator): Single<RepositoriesPaginator> =
        service.fetchRepositories(
            query = paginator.query,
            sort = paginator.sort,
            perPage = paginator.limit,
            page = paginator.allItems.size / paginator.limit + 1
        ).map {
            paginator.totalCount = it.totalCount
            paginator.loadedItems = it.items
            paginator
        }

    fun fetchRepository(user: String, repo: String): Single<Repository> =
        service.fetchRepository(user, repo)

    fun fetchUser(user: String): Single<User> =
        service.fetchUserDetails(user)

}