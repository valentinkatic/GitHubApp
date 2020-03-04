package com.katic.api

import com.katic.api.model.TokenResponse

class AuthRepository(private val service: AuthService) {

    suspend fun fetchToken(
        clientId: String,
        clientSecret: String,
        code: String
    ): TokenResponse =
        service.fetchToken(clientId, clientSecret, code)

}
