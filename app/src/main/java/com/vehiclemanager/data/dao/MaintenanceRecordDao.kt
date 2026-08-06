package com.vehiclemanager.data.dao

import androidx.room.*
import com.vehiclemanager.data.entity.MaintenanceRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceRecordDao {
    @Query("SELECT * FROM maintenance_records WHERE vehicle_id = :vehicleId ORDER BY date DESC")
    fun getRecordsByVehicle(vehicleId: Long): Flow<List<MaintenanceRecord>>

    @Query("SELECT * FROM maintenance_records ORDER BY date DESC")
    fun getAllRecords(): Flow<List<MaintenanceRecord>>

    @Query("SELECT * FROM maintenance_records WHERE type = :type ORDER BY date DESC")
    fun getRecordsByType(type: String): Flow<List<MaintenanceRecord>>

    @Query("SELECT * FROM maintenance_records WHERE vehicle_id = :vehicleId AND type = :type ORDER BY date DESC")
    fun getRecordsByVehicleAndType(vehicleId: Long, type: String): Flow<List<MaintenanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: MaintenanceRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<MaintenanceRecord>)

    @Update
    suspend fun updateRecord(record: MaintenanceRecord)

    @Delete
    suspend fun deleteRecord(record: MaintenanceRecord)

    @Query("SELECT * FROM maintenance_records")
    suspend fun getAllRecordsList(): List<MaintenanceRecord>
}
