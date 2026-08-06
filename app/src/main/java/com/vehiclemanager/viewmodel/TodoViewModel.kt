package com.vehiclemanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vehiclemanager.VehicleApp
import com.vehiclemanager.data.entity.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TodoUiState(
    val todos: List<Todo> = emptyList(),
    val filterType: String? = null,
    val filterStatus: String = "待处理",
    val vehicles: List<Vehicle> = emptyList(),
    val selectedVehicle: Vehicle? = null
)

class TodoViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as VehicleApp
    private val repository = app.todoRepository
    private val vehicleRepo = app.vehicleRepository

    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()

    init {
        loadTodos()
        viewModelScope.launch {
            vehicleRepo.getAllVehicles().collect { vehicles ->
                _uiState.update { it.copy(vehicles = vehicles) }
            }
        }
    }

    fun loadTodos() {
        viewModelScope.launch {
            repository.getTodosByStatus("待处理").collect { todos ->
                _uiState.update { it.copy(todos = todos) }
            }
        }
    }

    fun filterByType(type: String?) {
        _uiState.update { it.copy(filterType = type) }
        viewModelScope.launch {
            val flow = if (type != null) {
                repository.getTodosByType(type)
            } else {
                repository.getAllTodos()
            }
            flow.collect { todos ->
                _uiState.update { it.copy(todos = todos) }
            }
        }
    }

    fun completeTodo(todo: Todo) {
        viewModelScope.launch {
            repository.updateTodo(todo.copy(status = "已完成"))
        }
    }

    fun deleteTodo(todo: Todo) {
        viewModelScope.launch {
            repository.deleteTodo(todo)
        }
    }

    suspend fun refreshTodos() {
        // Sync todos with vehicle data
        val vehicles = vehicleRepo.getAllVehiclesList()
        val existingTodos = repository.getAllTodosList()

        for (vehicle in vehicles) {
            // Check annual inspection
            if (vehicle.annualInspectionDate > 0) {
                val hasInspectionTodo = existingTodos.any {
                    it.vehicleId == vehicle.id && it.type == "年审"
                }
                if (!hasInspectionTodo) {
                    repository.insertTodo(
                        Todo(
                            vehicleId = vehicle.id,
                            type = "年审",
                            title = "${vehicle.plateNumber} 年审",
                            description = "年审日期: ${vehicle.annualInspectionDate}",
                            dueDate = vehicle.annualInspectionDate
                        )
                    )
                }
            }
        }
    }
}
