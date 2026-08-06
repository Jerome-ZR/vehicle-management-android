package com.vehiclemanager.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "todos",
    foreignKeys = [ForeignKey(
        entity = Vehicle::class,
        parentColumns = ["id"],
        childColumns = ["vehicle_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("vehicle_id")]
)
data class Todo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "vehicle_id") val vehicleId: Long,
    val type: String,  // "年审", "保养", "维修"
    val title: String,
    val description: String = "",
    @ColumnInfo(name = "due_date") val dueDate: Long = 0,
    val status: String = "待处理",
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)
