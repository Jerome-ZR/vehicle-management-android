package com.vehiclemanager.data.repository

import com.vehiclemanager.data.dao.VehicleDao
import com.vehiclemanager.data.entity.Vehicle
import kotlinx.coroutines.flow.Flow

class VehicleRepository(private val dao: VehicleDao) {
    fun getAllVehicles(): Flow<List<Vehicle>> = dao.getAllVehicles()
    suspend fun getVehicleById(id: Long): Vehicle? = dao.getVehicleById(id)
    fun searchVehicles(query: String): Flow<List<Vehicle>> = dao.searchVehicles(query)
    suspend fun insertVehicle(vehicle: Vehicle): Long = dao.insertVehicle(vehicle)
    suspend fun updateVehicle(vehicle: Vehicle) = dao.updateVehicle(vehicle)
    suspend fun deleteVehicle(vehicle: Vehicle) = dao.deleteVehicle(vehicle)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
    suspend fun getAllVehiclesList(): List<Vehicle> = dao.getAllVehiclesList()
}
