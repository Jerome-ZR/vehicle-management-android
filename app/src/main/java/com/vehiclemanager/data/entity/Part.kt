package com.vehiclemanager.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parts")
data class Part(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val partName: String = "",
    val shop: String = "",
    val qty: Int = 0,
    val unitPrice: Int = 0,
    val amount: Int = 0
)
