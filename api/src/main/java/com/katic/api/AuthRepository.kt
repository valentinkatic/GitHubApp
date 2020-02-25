package com.katic.api

import com.katic.api.model.TokenResponse
import io.reactivex.Single

class AuthRepository(private val service: AuthService) {

    fun fetchToken(
        clientId: String,
        clientSecret: String,
        code: String
    ): Single<TokenResponse> =
        service.fetchToken(clientId, clientSecret, code)

}
