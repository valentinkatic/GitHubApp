package com.katic.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TokenResponse(
    @Json(name = "access_token") val token: String,
    @Json(name = "scope") val scope: String,
    @Json(name = "token_type") val tokenType: String
)