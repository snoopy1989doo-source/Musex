package com.snoopy.musex.data

import androidx.room.withTransaction
import com.snoopy.musex.domain.YouTubeReference
import com.snoopy.musex.domain.YouTubeUrlParser
import kotlinx.coroutines.flow.Flow
import java.util.UUID

data class AddSongRequest(
    val sharedText: String,
    val titleOverride: String = "",
    val artistOverride: String = "",
    val memoryNote: String = "",
    val playlistIds: Set<String> = emptySet(),
    val tags: Set<String> = emptySet(),
)

data class AddSongResult(val songId: String, val alreadyExisted: Boolean)

class MusexRepository(private val database: MusexDatabase) {
    private val dao = database.dao()
    val songs: Flow<List<SongEntity>> = dao.observeSongs()
    val favorites: Flow<List<SongEntity>> = dao.observeFavorites()
    val recentlyPlayed: Flow<List<SongEntity>> = dao.observeRecentlyPlayed()
    val playlists: Flow<List<PlaylistSummary>> = dao.observePlaylists()

    fun search(query: String) = dao.searchSongs(query.trim())

    suspend fun addSong(request: AddSongRequest): AddSongResult = database.withTransaction {
        val reference = YouTubeUrlParser.parse(request.sharedText)
        require(reference is YouTubeReference.Video) { "Please enter a supported YouTube video URL" }
        val now = System.currentTimeMillis()
        val existing = dao.songByVideoId(reference.id)
        val song = existing ?: SongEntity(
            id = UUID.randomUUID().toString(),
            youtubeVideoId = reference.id,
            canonicalUrl = "https://www.youtube.com/watch?v=${reference.id}",
            titleOverride = request.titleOverride.trim().ifBlank { null },
            artistOverride = request.artistOverride.trim().ifBlank { null },
            memoryNote = request.memoryNote.trim(),
            addedAt = now,
            updatedAt = now,
        ).also { dao.insertSong(it) }

        if (existing != null && (request.titleOverride.isNotBlank() || request.artistOverride.isNotBlank() || request.memoryNote.isNotBlank())) {
            dao.updateSong(existing.copy(
                titleOverride = request.titleOverride.trim().ifBlank { existing.titleOverride },
                artistOverride = request.artistOverride.trim().ifBlank { existing.artistOverride },
                memoryNote = request.memoryNote.trim().ifBlank { existing.memoryNote },
                updatedAt = now,
            ))
        }
        request.playlistIds.forEach { playlistId ->
            dao.insertMembership(PlaylistSongEntity(playlistId, song.id, dao.nextPosition(playlistId), now))
        }
        request.tags.map(String::trim).filter(String::isNotBlank).forEach { name ->
            val normalized = name.lowercase()
            val tag = dao.tagByNormalizedName(normalized) ?: TagEntity(UUID.randomUUID().toString(), name, normalized).also {
                dao.insertTag(it)
            }
            dao.insertSongTag(SongTagEntity(song.id, tag.id))
        }
        AddSongResult(song.id, existing != null)
    }

    suspend fun createPlaylist(name: String, description: String = ""): String {
        require(name.isNotBlank()) { "Playlist name is required" }
        val now = System.currentTimeMillis()
        return UUID.randomUUID().toString().also { id ->
            dao.insertPlaylist(PlaylistEntity(id, name.trim(), description.trim(), createdAt = now, updatedAt = now))
        }
    }

    suspend fun toggleFavorite(songId: String) = dao.toggleFavorite(songId, System.currentTimeMillis())
    suspend fun recordPlay(songId: String) = dao.recordPlay(songId, System.currentTimeMillis())
}
