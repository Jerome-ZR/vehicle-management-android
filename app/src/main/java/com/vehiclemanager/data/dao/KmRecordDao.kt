package com.vehiclemanager.data.dao

import androidx.room.*
import com.vehiclemanager.data.entity.KmRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface KmRecordDao {
    @Query("SELECT * FROM km_records WHERE vehicle_id = :vehicleId ORDER BY record_date DESC")
    fun getRecordsByVehicle(vehicleId: Long): Flow<List<KmRecord>>

    @Query("SELECT * FROM km_records ORDER BY record_date DESC")
    fun getAllRecords(): Flow<List<KmRecord>>

    @Query("SELECT * FROM km_records WHERE vehicle_id = :vehicleId AND month = :month LIMIT 1")
    suspend fun getRecordByVehicleAndMonth(vehicleId: Long, month: String): KmRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: KmRecord): Long

    @Update
    suspend fun updateRecord(record: KmRecord)

    @Delete
    suspend fun deleteRecord(record: KmRecord)

    @Query("SELECT * FROM km_records")
    suspend fun getAllRecordsList(): List<KmRecord>
}
