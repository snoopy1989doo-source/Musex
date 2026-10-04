package com.snoopy.musex.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class YouTubeUrlParserTest {
    private val id = "dQw4w9WgXcQ"

    @Test fun `watch and short links resolve to the same video id`() {
        assertEquals(YouTubeReference.Video(id), YouTubeUrlParser.parse("https://www.youtube.com/watch?v=$id&utm_source=x"))
        assertEquals(YouTubeReference.Video(id), YouTubeUrlParser.parse("listen https://youtu.be/$id?t=14"))
        assertEquals(YouTubeReference.Video(id), YouTubeUrlParser.parse("https://music.youtube.com/watch?v=$id&feature=share"))
        assertEquals(YouTubeReference.Video(id), YouTubeUrlParser.parse("https://youtube.com/shorts/$id"))
    }

    @Test fun `playlist is identified without treating its video as a song`() {
        assertEquals(YouTubeReference.Playlist("PL1234567890"), YouTubeUrlParser.parse("https://youtube.com/playlist?list=PL1234567890"))
        assertEquals(YouTubeReference.Playlist("PL1234567890"), YouTubeUrlParser.parse("https://youtube.com/watch?v=$id&list=PL1234567890"))
    }

    @Test fun `untrusted hosts and malformed ids are rejected`() {
        assertNull(YouTubeUrlParser.parse("https://example.com/watch?v=$id"))
        assertNull(YouTubeUrlParser.parse("https://youtube.com/watch?v=short"))
        assertNull(YouTubeUrlParser.parse("hello world"))
    }
}

