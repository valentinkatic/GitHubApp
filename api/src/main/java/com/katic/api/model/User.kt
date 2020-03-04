package com.katic.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    @Json(name = "login") val login: String,
    @Json(name = "id") val id: Long,
    @Json(name = "avatar_url") val avatarUrl: String,
    @Json(name = "html_url") val url: String,
    @Json(name = "public_repos") val publicRepos: Int? = null,
    @Json(name = "followers") val followers: Int? = null,
    @Json(name = "following") val following: Int? = null,
    @Json(name = "created_at") val created: String? = null,
    @Json(name = "updated_at") val updated: String? = null
)