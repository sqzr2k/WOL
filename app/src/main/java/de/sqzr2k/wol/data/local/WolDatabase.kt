package de.sqzr2k.wol.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DeviceEntity::class, GroupEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class WolDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun groupDao(): GroupDao
}
