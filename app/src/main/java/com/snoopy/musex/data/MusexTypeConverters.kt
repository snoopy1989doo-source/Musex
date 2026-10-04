package com.snoopy.musex.data

import androidx.room.TypeConverter

class MusexTypeConverters {
    @TypeConverter fun metadataState(value: String) = MetadataState.valueOf(value)
    @TypeConverter fun metadataState(value: MetadataState) = value.name
    @TypeConverter fun availability(value: String) = Availability.valueOf(value)
    @TypeConverter fun availability(value: Availability) = value.name
    @TypeConverter fun playlistType(value: String) = PlaylistType.valueOf(value)
    @TypeConverter fun playlistType(value: PlaylistType) = value.name
    @TypeConverter fun coverSource(value: String) = CoverSource.valueOf(value)
    @TypeConverter fun coverSource(value: CoverSource) = value.name
}

