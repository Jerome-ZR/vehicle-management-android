package com.vehiclemanager.data.dao

import androidx.room.*
import com.vehiclemanager.data.entity.Part

@Dao
interface PartDao {
    @Query("SELECT * FROM parts ORDER BY id ASC")
    suspend fun getAllList(): List<Part>

    @Query("SELECT DISTINCT shop FROM parts")
    suspend fun getShops(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<Part>)
}
