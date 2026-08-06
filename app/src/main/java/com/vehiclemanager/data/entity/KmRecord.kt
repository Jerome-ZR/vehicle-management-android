package com.vehiclemanager.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "km_records",
    foreignKeys = [ForeignKey(
        entity = Vehicle::class,
        parentColumns = ["id"],
        childColumns = ["vehicle_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("vehicle_id")]
)
data class KmRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "vehicle_id") val vehicleId: Long,
    val km: Int,
    @ColumnInfo(name = "record_date") val recordDate: Long = System.currentTimeMillis(),
    val month: String  // "YYYY-MM"
)
