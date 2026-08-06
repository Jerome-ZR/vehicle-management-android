package com.vehiclemanager.data.dao

import androidx.room.*
import com.vehiclemanager.data.entity.Vehicle
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles ORDER BY created_at DESC")
    fun getAllVehicles(): Flow<List<Vehicle>>

    @Query("SELECT * FROM vehicles WHERE id = :id")
    suspend fun getVehicleById(id: Long): Vehicle?

    @Query("SELECT * FROM vehicles WHERE plate_number LIKE '%' || :query || '%' OR vehicle_code LIKE '%' || :query || '%' OR assigned_user LIKE '%' || :query || '%'")
    fun searchVehicles(query: String): Flow<List<Vehicle>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: Vehicle): Long

    @Update
    suspend fun updateVehicle(vehicle: Vehicle)

    @Delete
    suspend fun deleteVehicle(vehicle: Vehicle)

    @Query("DELETE FROM vehicles WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM vehicles")
    suspend fun getAllVehiclesList(): List<Vehicle>
}
