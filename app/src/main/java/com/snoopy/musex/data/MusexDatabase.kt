package com.snoopy.musex.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [SongEntity::class, PlaylistEntity::class, PlaylistSongEntity::class, TagEntity::class, SongTagEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(MusexTypeConverters::class)
abstract class MusexDatabase : RoomDatabase() {
    abstract fun dao(): MusexDao

    companion object {
        fun create(context: Context): MusexDatabase = Room.databaseBuilder(
            context.applicationContext,
            MusexDatabase::class.java,
            "musex.db",
        ).build()
    }
}

