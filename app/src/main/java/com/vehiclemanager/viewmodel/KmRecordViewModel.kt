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

data class KmRecordUiState(
    val records: List<KmRecord> = emptyList(),
    val selectedVehicleId: Long? = null,
    val selectedVehicle: Vehicle? = null,
    val vehicles: List<Vehicle> = emptyList(),
    val currentMonth: String = DateUtils.currentMonth(),
    val isAdding: Boolean = false,
    val reminderMessage: String? = null,
    val kmInput: String = ""
)

class KmRecordViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as VehicleApp
    private val repository = app.kmRecordRepository
    private val vehicleRepo = app.vehicleRepository
    private val todoRepo = app.todoRepository

    private val _uiState = MutableStateFlow(KmRecordUiState())
    val uiState: StateFlow<KmRecordUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            vehicleRepo.getAllVehicles().collect { vehicles ->
                _uiState.update { it.copy(vehicles = vehicles) }
            }
        }
    }

    fun selectVehicle(vehicle: Vehicle) {
        _uiState.update { it.copy(selectedVehicleId = vehicle.id, selectedVehicle = vehicle) }
        viewModelScope.launch {
            repository.getRecordsByVehicle(vehicle.id).collect { records ->
                _uiState.update { it.copy(records = records) }
            }
        }
    }

    fun setKmInput(km: String) {
        _uiState.update { it.copy(kmInput = km) }
    }

    fun startAdding() {
        _uiState.update { it.copy(isAdding = true, reminderMessage = null) }
    }

    fun cancelAdding() {
        _uiState.update { it.copy(isAdding = false, kmInput = "", reminderMessage = null) }
    }

    fun saveKmRecord() {
        val vehicleId = _uiState.value.selectedVehicleId ?: return
        val kmStr = _uiState.value.kmInput
        val km = kmStr.toIntOrNull() ?: return
        val month = _uiState.value.currentMonth
        val vehicle = _uiState.value.selectedVehicle

        viewModelScope.launch {
            // Check if record for this month already exists
            val existing = repository.getRecordByVehicleAndMonth(vehicleId, month)
            if (existing != null) {
                repository.updateRecord(existing.copy(km = km))
            } else {
                repository.insertRecord(
                    KmRecord(vehicleId = vehicleId, km = km, month = month)
                )
            }

            // Check maintenance reminder
            vehicle?.let { v ->
                val currentV = vehicleRepo.getVehicleById(v.id) ?: v
                val (due, msg) = MaintenanceReminder.checkMaintenanceDue(currentV, km)
                if (due) {
                    _uiState.update { it.copy(reminderMessage = "⚠️ $msg") }

                    // Create todo if not exists
                    todoRepo.deleteByVehicleAndType(v.id, "保养")
                    val nextKm = currentV.lastMaintenanceKm +
                            currentV.maintenanceIntervalKm
                    val nextDate = DateUtils.addDays(
                        System.currentTimeMillis(),
                        currentV.maintenanceIntervalDays
                    )
                    todoRepo.insertTodo(
                        Todo(
                            vehicleId = v.id,
                            type = "保养",
                            title = "${v.plateNumber} 需要保养",
                            description = "当前公里数: $km\n应保公里数: $nextKm\n$msg",
                            dueDate = nextDate
                        )
                    )
                }
            }

            _uiState.update { it.copy(isAdding = false, kmInput = "") }
        }
    }
}
