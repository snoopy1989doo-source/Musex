package com.snoopy.musex.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class MetadataState { PENDING, READY, FAILED }
enum class Availability { UNKNOWN, AVAILABLE, UNAVAILABLE }
enum class PlaylistType { MANUAL, SMART }
enum class CoverSource { NONE, SONG, UPLOADED }

@Entity(
    tableName = "songs",
    indices = [Index(value = ["youtubeVideoId"], unique = true), Index("addedAt"), Index("lastPlayedAt")]
)
data class SongEntity(
    @PrimaryKey val id: String,
    val youtubeVideoId: String,
    val canonicalUrl: String,
    val providerTitle: String? = null,
    val providerChannel: String? = null,
    val inferredArtist: String? = null,
    val titleOverride: String? = null,
    val artistOverride: String? = null,
    val thumbnailUrl: String? = null,
    val providerFetchedAt: Long? = null,
    val metadataState: MetadataState = MetadataState.PENDING,
    val availability: Availability = Availability.UNKNOWN,
    val memoryNote: String = "",
    val favorite: Boolean = false,
    val addedAt: Long,
    val updatedAt: Long,
    val lastPlayedAt: Long? = null,
    val playCount: Int = 0,
) {
    val displayTitle: String get() = titleOverride?.takeIf(String::isNotBlank)
        ?: providerTitle?.takeIf(String::isNotBlank) ?: "Waiting for metadata"
    val displayArtist: String get() = artistOverride?.takeIf(String::isNotBlank)
        ?: inferredArtist?.takeIf(String::isNotBlank)
        ?: providerChannel?.takeIf(String::isNotBlank) ?: "Artist unknown"
}

@Entity(tableName = "playlists", indices = [Index("name")])
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String = "",
    val type: PlaylistType = PlaylistType.MANUAL,
    val coverSource: CoverSource = CoverSource.NONE,
    val coverAssetId: String? = null,
    val coverSongId: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val lastOpenedAt: Long? = null,
    val openCount: Int = 0,
)

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"],
    foreignKeys = [
        ForeignKey(entity = PlaylistEntity::class, parentColumns = ["id"], childColumns = ["playlistId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = SongEntity::class, parentColumns = ["id"], childColumns = ["songId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("playlistId"), Index("songId")]
)
data class PlaylistSongEntity(
    val playlistId: String,
    val songId: String,
    val customPosition: Long,
    val addedAt: Long,
)

@Entity(tableName = "tags", indices = [Index(value = ["normalizedName"], unique = true)])
data class TagEntity(
    @PrimaryKey val id: String,
    val name: String,
    val normalizedName: String,
)

@Entity(
    tableName = "song_tags",
    primaryKeys = ["songId", "tagId"],
    foreignKeys = [
        ForeignKey(entity = SongEntity::class, parentColumns = ["id"], childColumns = ["songId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TagEntity::class, parentColumns = ["id"], childColumns = ["tagId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("songId"), Index("tagId")]
)
data class SongTagEntity(val songId: String, val tagId: String)

data class PlaylistSummary(
    val id: String,
    val name: String,
    val description: String,
    val type: PlaylistType,
    val songCount: Int,
)

