package com.snoopy.musex.domain

import java.net.URI

sealed interface YouTubeReference {
    data class Video(val id: String) : YouTubeReference
    data class Playlist(val id: String) : YouTubeReference
}

object YouTubeUrlParser {
    private val videoId = Regex("^[A-Za-z0-9_-]{11}$")
    private val playlistId = Regex("^[A-Za-z0-9_-]{10,80}$")
    private val urlCandidate = Regex("https?://[^\\s]+", RegexOption.IGNORE_CASE)
    private val acceptedHosts = setOf("youtube.com", "www.youtube.com", "m.youtube.com", "music.youtube.com", "youtu.be")

    fun parse(sharedText: String): YouTubeReference? {
        val candidate = urlCandidate.find(sharedText)?.value?.trimEnd('.', ',', ')', ']', '}') ?: sharedText.trim()
        val uri = runCatching { URI(candidate) }.getOrNull() ?: return null
        val host = uri.host?.lowercase() ?: return null
        if (host !in acceptedHosts) return null
        val query = parseQuery(uri.rawQuery.orEmpty())

        query["list"]?.takeIf { playlistId.matches(it) }?.let { return YouTubeReference.Playlist(it) }
        val rawVideo = when {
            host == "youtu.be" -> uri.path.trim('/').substringBefore('/')
            uri.path == "/watch" -> query["v"]
            uri.path.startsWith("/shorts/") -> uri.path.removePrefix("/shorts/").substringBefore('/')
            uri.path.startsWith("/embed/") -> uri.path.removePrefix("/embed/").substringBefore('/')
            else -> null
        }
        return rawVideo?.takeIf { videoId.matches(it) }?.let(YouTubeReference::Video)
    }

    private fun parseQuery(raw: String): Map<String, String> = raw.split('&')
        .mapNotNull { part ->
            val index = part.indexOf('=')
            if (index <= 0) null else part.substring(0, index) to part.substring(index + 1)
        }.toMap()
}

