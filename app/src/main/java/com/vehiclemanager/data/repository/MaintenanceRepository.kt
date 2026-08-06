package com.vehiclemanager.data.repository

import com.vehiclemanager.data.dao.MaintenanceRecordDao
import com.vehiclemanager.data.entity.MaintenanceRecord
import kotlinx.coroutines.flow.Flow

class MaintenanceRepository(private val dao: MaintenanceRecordDao) {
    fun getRecordsByVehicle(vehicleId: Long): Flow<List<MaintenanceRecord>> =
        dao.getRecordsByVehicle(vehicleId)
    fun getAllRecords(): Flow<List<MaintenanceRecord>> = dao.getAllRecords()
    fun getRecordsByType(type: String): Flow<List<MaintenanceRecord>> = dao.getRecordsByType(type)
    suspend fun insertRecord(record: MaintenanceRecord): Long = dao.insertRecord(record)
    suspend fun updateRecord(record: MaintenanceRecord) = dao.updateRecord(record)
    suspend fun deleteRecord(record: MaintenanceRecord) = dao.deleteRecord(record)
    suspend fun getAllRecordsList(): List<MaintenanceRecord> = dao.getAllRecordsList()
}
