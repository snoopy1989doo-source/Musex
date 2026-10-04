package com.snoopy.musex

import android.app.Application
import com.snoopy.musex.data.MusexDatabase
import com.snoopy.musex.data.MusexRepository

class MusexApplication : Application() {
    val database by lazy { MusexDatabase.create(this) }
    val repository by lazy { MusexRepository(database) }
}

