package com.snoopy.musex.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.snoopy.musex.data.AddSongRequest
import com.snoopy.musex.data.MusexRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class MusexViewModel(private val repository: MusexRepository) : ViewModel() {
    val songs = repository.songs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val favorites = repository.favorites.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val recentlyPlayed = repository.recentlyPlayed.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val playlists = repository.playlists.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val query = MutableStateFlow("")
    val searchResults = query.flatMapLatest(repository::search)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val message = MutableStateFlow<String?>(null)

    fun addSong(request: AddSongRequest, onDone: () -> Unit) = viewModelScope.launch {
        runCatching { repository.addSong(request) }
            .onSuccess {
                message.value = if (it.alreadyExisted) "Already in your library — organization updated" else "Saved for metadata lookup"
                onDone()
            }
            .onFailure { message.value = it.message ?: "Could not save this song" }
    }

    fun createPlaylist(name: String, description: String, onDone: () -> Unit) = viewModelScope.launch {
        runCatching { repository.createPlaylist(name, description) }
            .onSuccess { message.value = "Playlist created"; onDone() }
            .onFailure { message.value = it.message ?: "Could not create playlist" }
    }

    fun toggleFavorite(songId: String) = viewModelScope.launch { repository.toggleFavorite(songId) }
    fun recordPlay(songId: String) = viewModelScope.launch {
        repository.recordPlay(songId)
        message.value = "Play action recorded — not listening progress"
    }
    fun clearMessage() { message.value = null }

    fun playlistSongs(playlistId: String) = repository.playlistSongs(playlistId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun loadMemberships(songId: String, onLoaded: (Set<String>) -> Unit) = viewModelScope.launch {
        onLoaded(repository.memberships(songId))
    }

    fun updateSong(songId: String, title: String, artist: String, note: String, playlistIds: Set<String>, onDone: () -> Unit) = viewModelScope.launch {
        runCatching { repository.updateSong(songId, title, artist, note, playlistIds) }
            .onSuccess { message.value = "Song details updated"; onDone() }
            .onFailure { message.value = it.message ?: "Could not update song" }
    }

    fun removeFromPlaylist(playlistId: String, songId: String) = viewModelScope.launch {
        repository.removeFromPlaylist(playlistId, songId)
        message.value = "Removed from playlist — song kept in Library"
    }

    class Factory(private val repository: MusexRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = MusexViewModel(repository) as T
    }
}
