package com.katic.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Repository(
    @Json(name = "id") val id: Int,
    @Json(name = "node_id") val nodeId: String,
    @Json(name = "name") val name: String,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "owner") val owner: User,
    @Json(name = "watchers_count") val watchersCount: Int,
    @Json(name = "forks_count") val forksCount: Int,
    @Json(name = "open_issues_count") val openIssuesCount: Int,
    @Json(name = "created_at") val created: String,
    @Json(name = "updated_at") val updated: String,
    @Json(name = "language") val language: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "html_url") val url: String
)