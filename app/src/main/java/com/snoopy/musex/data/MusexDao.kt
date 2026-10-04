package com.snoopy.musex.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MusexDao {
    @Query("SELECT * FROM songs ORDER BY addedAt DESC")
    fun observeSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE favorite = 1 ORDER BY updatedAt DESC")
    fun observeFavorites(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE lastPlayedAt IS NOT NULL ORDER BY lastPlayedAt DESC LIMIT :limit")
    fun observeRecentlyPlayed(limit: Int = 12): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE youtubeVideoId = :videoId LIMIT 1")
    suspend fun songByVideoId(videoId: String): SongEntity?

    @Query("SELECT * FROM songs WHERE id = :id LIMIT 1")
    suspend fun songById(id: String): SongEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSong(song: SongEntity)

    @Update suspend fun updateSong(song: SongEntity)

    @Query("UPDATE songs SET favorite = NOT favorite, updatedAt = :now WHERE id = :id")
    suspend fun toggleFavorite(id: String, now: Long)

    @Query("UPDATE songs SET playCount = playCount + 1, lastPlayedAt = :now, updatedAt = :now WHERE id = :id")
    suspend fun recordPlay(id: String, now: Long)

    @Query("""
        SELECT DISTINCT s.* FROM songs s
        LEFT JOIN song_tags st ON st.songId = s.id
        LEFT JOIN tags t ON t.id = st.tagId
        WHERE :query = ''
           OR lower(COALESCE(s.titleOverride, s.providerTitle, '')) LIKE '%' || lower(:query) || '%'
           OR lower(COALESCE(s.artistOverride, s.inferredArtist, s.providerChannel, '')) LIKE '%' || lower(:query) || '%'
           OR lower(s.memoryNote) LIKE '%' || lower(:query) || '%'
           OR lower(COALESCE(t.name, '')) LIKE '%' || lower(:query) || '%'
        ORDER BY s.addedAt DESC
    """)
    fun searchSongs(query: String): Flow<List<SongEntity>>

    @Query("""
        SELECT p.id, p.name, p.description, p.type, COUNT(ps.songId) AS songCount
        FROM playlists p LEFT JOIN playlist_songs ps ON p.id = ps.playlistId
        GROUP BY p.id ORDER BY p.updatedAt DESC
    """)
    fun observePlaylists(): Flow<List<PlaylistSummary>>

    @Query("SELECT * FROM playlists WHERE id = :id LIMIT 1")
    suspend fun playlistById(id: String): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Query("SELECT COALESCE(MAX(customPosition), -1) + 1 FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun nextPosition(playlistId: String): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMembership(item: PlaylistSongEntity)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeMembership(playlistId: String, songId: String)

    @Query("SELECT * FROM tags WHERE normalizedName = :normalized LIMIT 1")
    suspend fun tagByNormalizedName(normalized: String): TagEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: TagEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSongTag(join: SongTagEntity)

    @Query("SELECT t.name FROM tags t INNER JOIN song_tags st ON st.tagId = t.id WHERE st.songId = :songId ORDER BY t.name")
    fun observeTagNames(songId: String): Flow<List<String>>

    @Transaction
    suspend fun moveMembership(songId: String, fromPlaylistId: String, toPlaylistId: String, now: Long) {
        insertMembership(PlaylistSongEntity(toPlaylistId, songId, nextPosition(toPlaylistId), now))
        removeMembership(fromPlaylistId, songId)
    }
}

