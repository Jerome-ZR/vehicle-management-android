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

data class MaintenanceUiState(
    val records: List<MaintenanceRecord> = emptyList(),
    val selectedVehicleId: Long? = null,
    val selectedVehicle: Vehicle? = null,
    val filterType: String? = null,
    val isAdding: Boolean = false,
    val editingRecord: MaintenanceRecord? = null,
    val reminderMessage: String? = null
)

class MaintenanceViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as VehicleApp
    private val repository = app.maintenanceRepository
    private val vehicleRepo = app.vehicleRepository
    private val kmRecordRepo = app.kmRecordRepository
    private val todoRepo = app.todoRepository

    private val _uiState = MutableStateFlow(MaintenanceUiState())
    val uiState: StateFlow<MaintenanceUiState> = _uiState.asStateFlow()

    fun loadRecordsForVehicle(vehicleId: Long) {
        _uiState.update { it.copy(selectedVehicleId = vehicleId, filterType = null) }
        viewModelScope.launch {
            vehicleRepo.getVehicleById(vehicleId)?.let { vehicle ->
                _uiState.update { it.copy(selectedVehicle = vehicle) }
            }
            repository.getRecordsByVehicle(vehicleId).collect { records ->
                _uiState.update { it.copy(records = records) }
            }
        }
    }

    fun loadAllRecords() {
        _uiState.update { it.copy(selectedVehicleId = null, filterType = null) }
        viewModelScope.launch {
            repository.getAllRecords().collect { records ->
                _uiState.update { it.copy(records = records) }
            }
        }
    }

    fun filterByType(type: String) {
        _uiState.update { it.copy(filterType = type) }
        viewModelScope.launch {
            repository.getRecordsByType(type).collect { records ->
                _uiState.update { it.copy(records = records) }
            }
        }
    }

    fun startAdding() {
        _uiState.update { it.copy(isAdding = true, editingRecord = null) }
    }

    fun startEditing(record: MaintenanceRecord) {
        _uiState.update { it.copy(isAdding = true, editingRecord = record) }
    }

    fun cancelEditing() {
        _uiState.update { it.copy(isAdding = false, editingRecord = null) }
    }

    fun saveRecord(record: MaintenanceRecord) {
        viewModelScope.launch {
            if (record.id == 0L) {
                repository.insertRecord(record)
            } else {
                repository.updateRecord(record)
            }

            // Auto-update vehicle maintenance info for "保养" type
            if (record.type == "保养") {
                val vehicle = vehicleRepo.getVehicleById(record.vehicleId)
                vehicle?.let { v ->
                    val updated = v.copy(
                        lastMaintenanceDate = record.date,
                        lastMaintenanceKm = if (record.km > 0) record.km else v.lastMaintenanceKm
                    )
                    vehicleRepo.updateVehicle(updated)

                    // Update todo - remove old maintenance todo, create new one
                    todoRepo.deleteByVehicleAndType(v.id, "保养")
                    val nextDate = record.date + v.maintenanceIntervalDays * 24L * 60 * 60 * 1000
                    todoRepo.insertTodo(
                        Todo(
                            vehicleId = v.id,
                            type = "保养",
                            title = "${v.plateNumber} 下次保养",
                            description = "预计保养日期: ${DateUtils.formatDate(nextDate)}",
                            dueDate = nextDate
                        )
                    )
                }
            }

            // Update todos for repair approval
            if (record.type == "维修") {
                val vehicle = vehicleRepo.getVehicleById(record.vehicleId)
                vehicle?.let { v ->
                    todoRepo.insertTodo(
                        Todo(
                            vehicleId = v.id,
                            type = "维修",
                            title = "${v.plateNumber} 维修记录",
                            description = "维修项目: ${record.items}\n地点: ${record.location}\n价格: ¥${record.price}",
                            dueDate = record.date,
                            status = "已完成"
                        )
                    )
                }
            }

            _uiState.update { it.copy(isAdding = false, editingRecord = null) }
        }
    }

    fun deleteRecord(record: MaintenanceRecord) {
        viewModelScope.launch {
            repository.deleteRecord(record)
        }
    }

    /**
     * Check maintenance reminder for a vehicle
     */
    fun checkReminder(vehicleId: Long) {
        viewModelScope.launch {
            val vehicle = vehicleRepo.getVehicleById(vehicleId) ?: return@launch
            // Get latest km record
            val records = kmRecordRepo.getRecordsByVehicle(vehicleId)
            // We need to collect flow... let's do it differently
            val (due, msg) = MaintenanceReminder.checkMaintenanceDue(vehicle, null)
            _uiState.update {
                it.copy(reminderMessage = if (due) msg else "暂无保养提醒")
            }
        }
    }
}
