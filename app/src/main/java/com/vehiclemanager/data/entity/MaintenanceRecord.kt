package com.vehiclemanager.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "maintenance_records",
    foreignKeys = [ForeignKey(
        entity = Vehicle::class,
        parentColumns = ["id"],
        childColumns = ["vehicle_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("vehicle_id")]
)
data class MaintenanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "vehicle_id") val vehicleId: Long,
    val type: String,  // "保养" or "维修"
    val date: Long,
    val km: Int = 0,
    val items: String,
    val location: String = "",
    val price: Double = 0.0,
    val notes: String = ""
)
