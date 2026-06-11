package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Channel(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "alt_names") val altNames: List<String> = emptyList(),
    @Json(name = "network") val network: String?,
    @Json(name = "owners") val owners: List<String> = emptyList(),
    @Json(name = "country") val country: String?,
    @Json(name = "categories") val categories: List<String>? = emptyList(),
    @Json(name = "is_nsfw") val isNsfw: Boolean = false,
    @Json(name = "launched") val launched: String?,
    @Json(name = "closed") val closed: String?,
    @Json(name = "replaced_by") val replacedBy: String?,
    @Json(name = "website") val website: String?
)

@JsonClass(generateAdapter = true)
data class Stream(
    @Json(name = "channel") val channel: String?,
    @Json(name = "feed") val feed: String?,
    @Json(name = "title") val title: String?,
    @Json(name = "url") val url: String?,
    @Json(name = "referrer") val referrer: String?,
    @Json(name = "user_agent") val userAgent: String?,
    @Json(name = "quality") val quality: String?
)

@JsonClass(generateAdapter = true)
data class Logo(
    @Json(name = "channel") val channel: String?,
    @Json(name = "url") val url: String?
)

@JsonClass(generateAdapter = true)
data class Category(
    @Json(name = "id") val id: String?,
    @Json(name = "name") val name: String?
)

@JsonClass(generateAdapter = true)
data class Country(
    @Json(name = "name") val name: String?,
    @Json(name = "code") val code: String?,
    @Json(name = "flag") val flag: String?
)

data class ChannelItem(
    val channel: Channel,
    val stream: Stream?,
    val logo: Logo?,
    val categories: List<Category>,
    val country: Country?
)
