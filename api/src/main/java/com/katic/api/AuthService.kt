package com.katic.api

import com.katic.api.model.TokenResponse
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthService {

    @POST("/login/oauth/access_token")
    @Headers("No-Authentication: true", "Accept: application/json")
    suspend fun fetchToken(
        @Query("client_id") clientId: String,
        @Query("client_secret") clientSecret: String,
        @Query("code") code: String
    ): TokenResponse

}