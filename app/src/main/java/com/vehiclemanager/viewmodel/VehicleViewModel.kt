package com.vehiclemanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vehiclemanager.VehicleApp
import com.vehiclemanager.data.entity.*
import com.vehiclemanager.util.DateUtils
import com.vehiclemanager.util.MaintenanceReminder
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class VehicleUiState(
    val vehicles: List<Vehicle> = emptyList(),
    val searchQuery: String = "",
    val selectedVehicle: Vehicle? = null,
    val isEditing: Boolean = false,
    val vehicleReminders: Map<Long, String> = emptyMap()
)

class VehicleViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as VehicleApp
    private val vehicleRepo = app.vehicleRepository
    private val kmRecordRepo = app.kmRecordRepository

    private val _uiState = MutableStateFlow(VehicleUiState())
    val uiState: StateFlow<VehicleUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            vehicleRepo.getAllVehicles().collect { vehicles ->
                val reminders = mutableMapOf<Long, String>()
                vehicles.forEach { v ->
                    val kmRecords = kmRecordRepo.getRecordsByVehicle(v.id)
                    // We need to get the latest km record
                    // Use suspend function to get latest km
                    val (due, msg) = MaintenanceReminder.checkMaintenanceDue(v, null)
                    if (due) reminders[v.id] = msg
                    val (annualDue, annualMsg) = MaintenanceReminder.checkAnnualInspectionDue(v)
                    if (annualDue) reminders[v.id] = (reminders[v.id] ?: "") + "\n" + annualMsg
                }
                _uiState.update { it.copy(vehicles = vehicles, vehicleReminders = reminders) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isBlank()) return
        viewModelScope.launch {
            vehicleRepo.searchVehicles(query).collect { vehicles ->
                _uiState.update { it.copy(vehicles = vehicles) }
            }
        }
    }

    fun selectVehicle(vehicle: Vehicle?) {
        _uiState.update { it.copy(selectedVehicle = vehicle, isEditing = false) }
    }

    fun startEditing(vehicle: Vehicle? = null) {
        _uiState.update { it.copy(selectedVehicle = vehicle, isEditing = true) }
    }

    fun cancelEditing() {
        _uiState.update { it.copy(isEditing = false, selectedVehicle = null) }
    }

    fun saveVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            if (vehicle.id == 0L) {
                vehicleRepo.insertVehicle(vehicle)
            } else {
                vehicleRepo.updateVehicle(vehicle)
            }
            _uiState.update { it.copy(isEditing = false, selectedVehicle = null) }
        }
    }

    fun deleteVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            vehicleRepo.deleteVehicle(vehicle)
            _uiState.update { it.copy(selectedVehicle = null) }
        }
    }
}
