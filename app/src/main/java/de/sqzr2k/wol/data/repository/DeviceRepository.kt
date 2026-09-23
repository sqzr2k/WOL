package de.sqzr2k.wol.data.repository

import androidx.room.withTransaction
import de.sqzr2k.wol.data.local.DeviceEntity
import de.sqzr2k.wol.data.local.GroupEntity
import de.sqzr2k.wol.data.local.WolDatabase
import kotlinx.coroutines.flow.Flow

class DeviceRepository(private val database: WolDatabase) {
    val devices: Flow<List<DeviceEntity>> = database.deviceDao().observeAll()
    val groups: Flow<List<GroupEntity>> = database.groupDao().observeAll()

    suspend fun device(id: Long) = database.deviceDao().get(id)

    suspend fun saveDevice(device: DeviceEntity): Long = if (device.id == 0L) {
        database.deviceDao().insert(device)
    } else {
        database.deviceDao().update(device.copy(updatedAt = System.currentTimeMillis()))
        device.id
    }

    suspend fun deleteDevice(device: DeviceEntity) = database.deviceDao().delete(device)
    suspend fun addGroup(name: String) = database.groupDao().insert(GroupEntity(name = name.trim()))
    suspend fun updateGroup(group: GroupEntity) = database.groupDao().update(group)
    suspend fun deleteGroup(group: GroupEntity) = database.groupDao().delete(group)

    suspend fun snapshot(): Pair<List<DeviceEntity>, List<GroupEntity>> =
        database.deviceDao().getAllOnce() to database.groupDao().getAllOnce()

    suspend fun replaceAll(devices: List<DeviceEntity>, groups: List<GroupEntity>) {
        database.withTransaction {
            database.deviceDao().deleteAll()
            database.groupDao().deleteAll()
            val idMap = mutableMapOf<Long, Long>()
            groups.forEach { old -> idMap[old.id] = database.groupDao().insert(old.copy(id = 0)) }
            database.deviceDao().insertAll(
                devices.map { device ->
                    device.copy(id = 0, groupId = device.groupId?.let(idMap::get))
                },
            )
        }
    }
}
