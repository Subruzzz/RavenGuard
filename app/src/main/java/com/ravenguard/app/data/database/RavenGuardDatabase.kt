package com.ravenguard.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ContactEntity::class], version = 1, exportSchema = true)
abstract class RavenGuardDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
}
