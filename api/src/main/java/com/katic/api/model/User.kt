package com.katic.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    val login: String,
    val id: Long,
    @Json(name = "avatar_url") val avatarUrl: String,
    @Json(name = "html_url") val url: String,
    @Json(name = "public_repos") val publicRepos: Int? = null,
    val followers: Int? = null,
    val following: Int? = null,
    @Json(name = "created_at") val created: String? = null,
    @Json(name = "updated_at") val updated: String? = null
)