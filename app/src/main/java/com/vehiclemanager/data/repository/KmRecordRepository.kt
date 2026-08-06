package com.vehiclemanager.data.repository

import com.vehiclemanager.data.dao.KmRecordDao
import com.vehiclemanager.data.entity.KmRecord
import kotlinx.coroutines.flow.Flow

class KmRecordRepository(private val dao: KmRecordDao) {
    fun getRecordsByVehicle(vehicleId: Long): Flow<List<KmRecord>> =
        dao.getRecordsByVehicle(vehicleId)
    fun getAllRecords(): Flow<List<KmRecord>> = dao.getAllRecords()
    suspend fun getRecordByVehicleAndMonth(vehicleId: Long, month: String): KmRecord? =
        dao.getRecordByVehicleAndMonth(vehicleId, month)
    suspend fun insertRecord(record: KmRecord): Long = dao.insertRecord(record)
    suspend fun updateRecord(record: KmRecord) = dao.updateRecord(record)
    suspend fun deleteRecord(record: KmRecord) = dao.deleteRecord(record)
    suspend fun getAllRecordsList(): List<KmRecord> = dao.getAllRecordsList()
}
