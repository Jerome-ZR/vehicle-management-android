package com.vehiclemanager.data.database

import android.content.Context
import androidx.room.*
import com.vehiclemanager.data.dao.*
import com.vehiclemanager.data.entity.*

@Database(
    entities = [Vehicle::class, MaintenanceRecord::class, KmRecord::class, Todo::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun maintenanceRecordDao(): MaintenanceRecordDao
    abstract fun kmRecordDao(): KmRecordDao
    abstract fun todoDao(): TodoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vehicle_manager_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
