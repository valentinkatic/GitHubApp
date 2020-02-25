package com.katic.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Repository(
    val id: Int,
    @Json(name = "node_id") val nodeId: String,
    val name: String,
    @Json(name = "full_name") val fullName: String,
    val owner: User,
    @Json(name = "watchers_count") val watchersCount: Int,
    @Json(name = "forks_count") val forksCount: Int,
    @Json(name = "open_issues_count") val openIssuesCount: Int,
    @Json(name = "created_at") val created: String,
    @Json(name = "updated_at") val updated: String,
    val language: String? = null,
    val description: String? = null,
    @Json(name = "html_url") val url: String
)