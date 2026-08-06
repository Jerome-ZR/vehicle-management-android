package com.vehiclemanager

import android.app.Application
import com.vehiclemanager.data.database.AppDatabase
import com.vehiclemanager.data.repository.*

class VehicleApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val vehicleRepository by lazy { VehicleRepository(database.vehicleDao()) }
    val maintenanceRepository by lazy { MaintenanceRepository(database.maintenanceRecordDao()) }
    val kmRecordRepository by lazy { KmRecordRepository(database.kmRecordDao()) }
    val todoRepository by lazy { TodoRepository(database.todoDao()) }
    val partDao by lazy { database.partDao() }
}
