package com.vehiclemanager.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "plate_number") val plateNumber: String,
    @ColumnInfo(name = "vehicle_code") val vehicleCode: String,
    @ColumnInfo(name = "assigned_user") val assignedUser: String,
    val brand: String = "",
    @ColumnInfo(name = "purchase_date") val purchaseDate: Long = 0,
    @ColumnInfo(name = "last_maintenance_date") var lastMaintenanceDate: Long = 0,
    @ColumnInfo(name = "last_maintenance_km") var lastMaintenanceKm: Int = 0,
    @ColumnInfo(name = "maintenance_interval_km") val maintenanceIntervalKm: Int = 5000,
    @ColumnInfo(name = "maintenance_interval_days") val maintenanceIntervalDays: Int = 180,
    @ColumnInfo(name = "annual_inspection_date") var annualInspectionDate: Long = 0,
    val status: String = "正常",
    val notes: String = "",
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)
