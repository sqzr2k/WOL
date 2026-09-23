package de.sqzr2k.wol.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "groups", indices = [Index(value = ["name"], unique = true)])
data class GroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sortOrder: Int = 0,
)

@Entity(
    tableName = "devices",
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("groupId")],
)
data class DeviceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val macAddress: String,
    val groupId: Long? = null,
    val color: Long = 0xFF315DA8,
    val hostname: String? = null,
    val deviceIp: String? = null,
    val broadcastAddress: String? = null,
    val port: Int = 9,
    val secureOnPassword: String? = null,
    val wifiSsid: String? = null,
    val pingCheckEnabled: Boolean = true,
    val netbiosCheckEnabled: Boolean = false,
    val manualPort: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
