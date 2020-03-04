package com.katic.api

import com.katic.api.model.RepositoriesResponse
import com.katic.api.model.Repository
import com.katic.api.model.User
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    /**
     * Valid values for sort field in [fetchRepositories]
     */
    object RepositorySort {
        const val STARS = "stars"
        const val FORKS = "forks"
        const val UPDATED = "updated"
    }

    @GET("/search/repositories")
    suspend fun fetchRepositories(
        @Query("q") query: String,
        @Query("sort") sort: String = RepositorySort.STARS,
        @Query("per_page") perPage: Int = 15,
        @Query("page") page: Int = 1
    ): RepositoriesResponse

    @GET("/repos/{user}/{repo}")
    suspend fun fetchRepository(
        @Path("user") user: String,
        @Path("repo") repo: String
    ): Repository

    @GET("/users/{user}")
    suspend fun fetchUserDetails(@Path("user") user: String): User

    @GET("/user")
    suspend fun fetchCurrentUser(): User

}