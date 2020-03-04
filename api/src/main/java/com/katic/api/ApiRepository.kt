package com.katic.api

import com.katic.api.model.Repository
import com.katic.api.model.User

class ApiRepository(private val service: ApiService) {

    data class RepositoriesPaginator(
        val query: String = "",
        val limit: Int = 15,
        val sort: String = ApiService.RepositorySort.STARS,
        var totalCount: Int = 0,
        var loadedItems: List<Repository> = emptyList(),
        val allItems: MutableList<Repository> = ArrayList()
    )

    suspend fun fetchRepositories(paginator: RepositoriesPaginator): RepositoriesPaginator {
        val repositoriesResponse = service.fetchRepositories(
            query = paginator.query,
            sort = paginator.sort,
            perPage = paginator.limit,
            page = paginator.allItems.size / paginator.limit + 1
        )
        paginator.totalCount = repositoriesResponse.totalCount
        paginator.loadedItems = repositoriesResponse.items
        return paginator
    }

    suspend fun fetchRepository(user: String, repo: String): Repository =
        service.fetchRepository(user, repo)

    suspend fun fetchUser(user: String): User =
        service.fetchUserDetails(user)

    suspend fun fetchCurrentUser(): User =
        service.fetchCurrentUser()
}