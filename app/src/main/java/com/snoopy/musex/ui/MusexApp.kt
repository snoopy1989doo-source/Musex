package com.snoopy.musex.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.snoopy.musex.data.AddSongRequest
import com.snoopy.musex.data.MetadataState
import com.snoopy.musex.data.MusexRepository
import com.snoopy.musex.data.PlaylistSummary
import com.snoopy.musex.data.SongEntity
import com.snoopy.musex.domain.YouTubeReference
import com.snoopy.musex.domain.YouTubeUrlParser
import com.snoopy.musex.ui.theme.Blue400
import com.snoopy.musex.ui.theme.Navy800
import com.snoopy.musex.ui.theme.Navy900

private enum class Destination(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Default.Home),
    Library("Library", Icons.Default.LibraryMusic),
    Search("Search", Icons.Default.Search),
    Playlists("Playlists", Icons.AutoMirrored.Filled.PlaylistPlay),
    Settings("Settings", Icons.Default.Settings),
}

@Composable
fun MusexApp(
    repository: MusexRepository,
    incomingShare: String?,
    onShareConsumed: () -> Unit,
) {
    val model: MusexViewModel = viewModel(factory = MusexViewModel.Factory(repository))
    val songs by model.songs.collectAsState()
    val favorites by model.favorites.collectAsState()
    val recentlyPlayed by model.recentlyPlayed.collectAsState()
    val playlists by model.playlists.collectAsState()
    val searchResults by model.searchResults.collectAsState()
    val query by model.query.collectAsState()
    val message by model.message.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var destination by rememberSaveable { mutableStateOf(Destination.Home) }
    var showAddSong by rememberSaveable { mutableStateOf(false) }
    var showCreatePlaylist by rememberSaveable { mutableStateOf(false) }
    var showCarMode by rememberSaveable { mutableStateOf(false) }
    var sharedSeed by remember { mutableStateOf("") }

    LaunchedEffect(incomingShare) {
        incomingShare?.let {
            sharedSeed = it
            showAddSong = true
            onShareConsumed()
        }
    }
    LaunchedEffect(message) {
        message?.let { snackbar.showSnackbar(it); model.clearMessage() }
    }

    if (showCarMode) {
        CarModeScreen(
            favorites = favorites,
            recent = recentlyPlayed,
            playlists = playlists,
            onClose = { showCarMode = false },
            onPlayed = model::recordPlay,
        )
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            NavigationBar(containerColor = Navy900) {
                Destination.entries.forEach { item ->
                    NavigationBarItem(
                        selected = destination == item,
                        onClick = { destination = item },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                    )
                }
            }
        },
        floatingActionButton = {
            when (destination) {
                Destination.Library -> ExtendedFloatingActionButton(
                    onClick = { sharedSeed = ""; showAddSong = true },
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("Add song") },
                )
                Destination.Playlists -> FloatingActionButton(onClick = { showCreatePlaylist = true }) {
                    Icon(Icons.Default.Add, "New playlist")
                }
                else -> Unit
            }
        },
    ) { padding ->
        when (destination) {
            Destination.Home -> HomeScreen(songs, favorites, recentlyPlayed, playlists, { showCarMode = true }, model::recordPlay, padding)
            Destination.Library -> LibraryScreen(songs, model::toggleFavorite, model::recordPlay, padding)
            Destination.Search -> SearchScreen(query, { model.query.value = it }, searchResults, model::toggleFavorite, model::recordPlay, padding)
            Destination.Playlists -> PlaylistsScreen(playlists, padding)
            Destination.Settings -> SettingsScreen(padding)
        }
    }

    if (showAddSong) {
        AddSongDialog(
            initialText = sharedSeed,
            playlists = playlists,
            onDismiss = { showAddSong = false },
            onSave = { model.addSong(it) { showAddSong = false } },
        )
    }
    if (showCreatePlaylist) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylist = false },
            onCreate = { name, description -> model.createPlaylist(name, description) { showCreatePlaylist = false } },
        )
    }
}

@Composable
private fun ScreenFrame(title: String, subtitle: String, padding: PaddingValues, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp)) {
        Spacer(Modifier.height(20.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(18.dp))
        content()
    }
}

@Composable
private fun HomeScreen(
    songs: List<SongEntity>,
    favorites: List<SongEntity>,
    recent: List<SongEntity>,
    playlists: List<PlaylistSummary>,
    onCarMode: () -> Unit,
    onPlayed: (String) -> Unit,
    padding: PaddingValues,
) = ScreenFrame("Musex", "Your memory-first music library", padding) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(bottom = 100.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF12395C)), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(22.dp)) {
                    Text("Remember the feeling, not the title.", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("เก็บเพลงพร้อมโน้ตสั้น ๆ แล้วกลับมาหาได้อีกครั้ง", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(onClick = onCarMode) { Icon(Icons.Default.DirectionsCar, null); Text("  Car Mode") }
                }
            }
        }
        item { Shelf("Continue Listening", recent, onPlayed) }
        item { Shelf("Recently Added", songs.take(12), onPlayed) }
        item { Shelf("Favorites", favorites, onPlayed) }
        item {
            SectionTitle("My Playlists")
            if (playlists.isEmpty()) EmptyLine("Create a playlist to organize songs without moving or duplicating them.")
            else playlists.take(6).forEach { PlaylistRow(it) }
        }
    }
}

@Composable
private fun LibraryScreen(
    songs: List<SongEntity>,
    onFavorite: (String) -> Unit,
    onPlayed: (String) -> Unit,
    padding: PaddingValues,
) = ScreenFrame("Library", "All songs · local and available offline", padding) {
    if (songs.isEmpty()) EmptyState("Your library is empty", "Add a YouTube or YouTube Music video link. It will be saved immediately, even before metadata is available.")
    else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 100.dp)) {
        items(songs, key = { it.id }) { SongRow(it, onFavorite, onPlayed) }
    }
}

@Composable
private fun SearchScreen(
    query: String,
    onQuery: (String) -> Unit,
    results: List<SongEntity>,
    onFavorite: (String) -> Unit,
    onPlayed: (String) -> Unit,
    padding: PaddingValues,
) = ScreenFrame("Search", "Songs · Artists · Playlists · Tags · Memory Notes", padding) {
    OutlinedTextField(value = query, onValueChange = onQuery, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Search, null) }, label = { Text("Search your library") }, singleLine = true)
    Spacer(Modifier.height(14.dp))
    Text("Can't Remember?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Text("ลองค้นจากคำใน Memory Note, ชื่อศิลปิน หรือ Tag ที่คุณเคยใส่", color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(14.dp))
    if (results.isEmpty()) EmptyLine(if (query.isBlank()) "Start typing to browse your local library." else "No literal Thai/English matches found.")
    else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 90.dp)) {
        items(results, key = { it.id }) { SongRow(it, onFavorite, onPlayed) }
    }
}

@Composable
private fun PlaylistsScreen(playlists: List<PlaylistSummary>, padding: PaddingValues) =
    ScreenFrame("Playlists", "Manual and smart collections", padding) {
        if (playlists.isEmpty()) EmptyState("No playlists yet", "Use + to create a manual playlist. Smart rules and YouTube sync arrive in later phases.")
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 90.dp)) {
            items(playlists, key = { it.id }) { PlaylistRow(it) }
        }
    }

@Composable
private fun SettingsScreen(padding: PaddingValues) = ScreenFrame("Settings", "Connections, backup and local data", padding) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SettingsCard("YouTube connection", "Not configured", "Local library remains fully usable. OAuth will never be simulated.", Icons.Default.SyncProblem) }
        item { SettingsCard("Backup & restore", "Planned · .musex", "Versioned JSON plus uploaded covers; credentials and caches excluded.", Icons.Default.LibraryMusic) }
        item { SettingsCard("Backup reminder", "Every 60 days", "A dismissal will not be recorded as a successful backup.", Icons.Default.Settings) }
        item {
            Text("Privacy & integration", fontWeight = FontWeight.Bold)
            Text("Musex stores your organization locally. YouTube playback requires an official visible player or opens the YouTube app; Musex does not download audio.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SongRow(song: SongEntity, onFavorite: (String) -> Unit, onPlayed: (String) -> Unit) {
    val context = LocalContext.current
    Card(colors = CardDefaults.cardColors(containerColor = Navy900), shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            CoverPlaceholder(song.displayTitle, Modifier.size(66.dp))
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(song.displayTitle, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(song.displayArtist, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (song.metadataState == MetadataState.PENDING) Text("Pending", color = Blue400, style = MaterialTheme.typography.labelSmall)
                    if (song.memoryNote.isNotBlank()) Text("Memory Note", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelSmall)
                }
            }
            IconButton(onClick = { onFavorite(song.id) }) { Icon(if (song.favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, "Favorite", tint = if (song.favorite) Blue400 else MaterialTheme.colorScheme.onSurfaceVariant) }
            IconButton(onClick = { openSong(context, song, onPlayed) }) { Icon(Icons.Default.PlayArrow, "Open in YouTube") }
        }
    }
}

@Composable
private fun Shelf(title: String, songs: List<SongEntity>, onPlayed: (String) -> Unit) {
    val context = LocalContext.current
    SectionTitle(title)
    if (songs.isEmpty()) EmptyLine("Nothing here yet.")
    else LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(songs, key = { it.id }) { song ->
            Card(Modifier.size(width = 156.dp, height = 205.dp).clickable { openSong(context, song, onPlayed) }, colors = CardDefaults.cardColors(containerColor = Navy900), shape = RoundedCornerShape(18.dp)) {
                CoverPlaceholder(song.displayTitle, Modifier.fillMaxWidth().height(150.dp))
                Column(Modifier.padding(10.dp)) {
                    Text(song.displayTitle, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(song.displayArtist, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun CoverPlaceholder(seed: String, modifier: Modifier = Modifier) {
    val tone = remember(seed) { listOf(Color(0xFF24598B), Color(0xFF3F4C91), Color(0xFF176B75))[seed.hashCode().and(Int.MAX_VALUE) % 3] }
    Box(modifier.background(tone, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
        Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(34.dp), tint = Color.White)
    }
}

@Composable private fun SectionTitle(text: String) = Text(text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
@Composable private fun EmptyLine(text: String) = Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)

@Composable
private fun EmptyState(title: String, description: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(28.dp)) {
            Icon(Icons.Default.Headphones, null, Modifier.size(58.dp), tint = Blue400)
            Spacer(Modifier.height(14.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PlaylistRow(playlist: PlaylistSummary) {
    Card(colors = CardDefaults.cardColors(containerColor = Navy900), shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            CoverPlaceholder(playlist.name, Modifier.size(58.dp))
            Column(Modifier.padding(start = 14.dp).weight(1f)) {
                Text(playlist.name, fontWeight = FontWeight.Bold)
                Text("${playlist.songCount} songs · ${playlist.type.name.lowercase().replaceFirstChar(Char::uppercase)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (playlist.description.isNotBlank()) Text(playlist.description, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text("Not linked", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun SettingsCard(title: String, status: String, detail: String, icon: ImageVector) {
    Card(colors = CardDefaults.cardColors(containerColor = Navy900), shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp)) {
            Icon(icon, null, tint = Blue400)
            Column(Modifier.padding(start = 14.dp)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(status, color = Blue400)
                Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun AddSongDialog(initialText: String, playlists: List<PlaylistSummary>, onDismiss: () -> Unit, onSave: (AddSongRequest) -> Unit) {
    var url by remember(initialText) { mutableStateOf(initialText) }
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(emptySet<String>()) }
    val parsed = YouTubeUrlParser.parse(url)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quick Add") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { OutlinedTextField(url, { url = it }, Modifier.fillMaxWidth(), label = { Text("YouTube link") }) }
                item {
                    Text(when (parsed) {
                        is YouTubeReference.Video -> "Video ${parsed.id} · metadata will load when connected"
                        is YouTubeReference.Playlist -> "This is a playlist link. Use Import in a later connected phase."
                        null -> "Paste a supported youtube.com, music.youtube.com or youtu.be video link."
                    }, color = if (parsed is YouTubeReference.Video) Blue400 else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (playlists.isNotEmpty()) {
                    item { Text("Add to playlists", fontWeight = FontWeight.Bold) }
                    items(playlists, key = { it.id }) { playlist ->
                        Row(Modifier.fillMaxWidth().clickable { selected = if (playlist.id in selected) selected - playlist.id else selected + playlist.id }, verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(playlist.id in selected, { checked -> selected = if (checked) selected + playlist.id else selected - playlist.id })
                            Text(playlist.name)
                        }
                    }
                }
                item { TextButton(onClick = { expanded = !expanded }) { Text(if (expanded) "Less details" else "Add note, tags or local names") } }
                if (expanded) {
                    item { OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text("Local title (optional)") }) }
                    item { OutlinedTextField(artist, { artist = it }, Modifier.fillMaxWidth(), label = { Text("Local artist (optional)") }) }
                    item { OutlinedTextField(tags, { tags = it }, Modifier.fillMaxWidth(), label = { Text("Tags, comma separated") }) }
                    item { OutlinedTextField(note, { note = it }, Modifier.fillMaxWidth(), label = { Text("Memory Note") }, supportingText = { Text("เช่น เพลงเปิดขับรถกลางคืน เสียงผู้หญิง") }) }
                }
            }
        },
        confirmButton = {
            Button(enabled = parsed is YouTubeReference.Video, onClick = {
                onSave(AddSongRequest(url, title, artist, note, selected, tags.split(',').map(String::trim).filter(String::isNotBlank).toSet()))
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun CreatePlaylistDialog(onDismiss: () -> Unit, onCreate: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New playlist") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Name") })
            OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), label = { Text("Description") })
        } },
        confirmButton = { Button(enabled = name.isNotBlank(), onClick = { onCreate(name, description) }) { Text("Create") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun CarModeScreen(
    favorites: List<SongEntity>,
    recent: List<SongEntity>,
    playlists: List<PlaylistSummary>,
    onClose: () -> Unit,
    onPlayed: (String) -> Unit,
) {
    val context = LocalContext.current
    Scaffold(topBar = {
        Row(Modifier.fillMaxWidth().background(Navy900).padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.DirectionsCar, null, tint = Blue400)
            Text("  Car Mode", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            OutlinedButton(onClick = onClose) { Text("Exit") }
        }
    }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { Text("Large controls · no editing while driving", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            item { SectionTitle("Favorites") }
            if (favorites.isEmpty()) item { EmptyLine("No favorites yet.") }
            items(favorites.take(8), key = { it.id }) { song ->
                Button(onClick = { openSong(context, song, onPlayed) }, modifier = Modifier.fillMaxWidth().height(72.dp), shape = RoundedCornerShape(18.dp)) {
                    Icon(Icons.Default.PlayArrow, null)
                    Text("  ${song.displayTitle}", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            item { SectionTitle("Recently Played") }
            items(recent.take(8), key = { "recent-${it.id}" }) { song ->
                OutlinedButton(onClick = { openSong(context, song, onPlayed) }, modifier = Modifier.fillMaxWidth().height(68.dp)) { Text(song.displayTitle, maxLines = 1) }
            }
            item { SectionTitle("Playlists") }
            items(playlists.take(8), key = { "playlist-${it.id}" }) { playlist ->
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Navy800)) {
                    Text("${playlist.name} · ${playlist.songCount} songs", Modifier.padding(20.dp), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

private fun openSong(context: Context, song: SongEntity, onOpened: (String) -> Unit) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(song.canonicalUrl))
    try {
        context.startActivity(intent)
        onOpened(song.id)
    } catch (_: ActivityNotFoundException) {
        // A failed launch is deliberately not counted as a play/open action.
    }
}
