package com.vehiclemanager.data.repository

import com.vehiclemanager.data.dao.TodoDao
import com.vehiclemanager.data.entity.Todo
import kotlinx.coroutines.flow.Flow

class TodoRepository(private val dao: TodoDao) {
    fun getAllTodos(): Flow<List<Todo>> = dao.getAllTodos()
    fun getTodosByStatus(status: String): Flow<List<Todo>> = dao.getTodosByStatus(status)
    fun getTodosByType(type: String): Flow<List<Todo>> = dao.getTodosByType(type)
    fun getTodosByVehicle(vehicleId: Long): Flow<List<Todo>> = dao.getTodosByVehicle(vehicleId)
    suspend fun insertTodo(todo: Todo): Long = dao.insertTodo(todo)
    suspend fun updateTodo(todo: Todo) = dao.updateTodo(todo)
    suspend fun deleteTodo(todo: Todo) = dao.deleteTodo(todo)
    suspend fun deleteByVehicleAndType(vehicleId: Long, type: String) =
        dao.deleteByVehicleAndType(vehicleId, type)
    suspend fun getAllTodosList(): List<Todo> = dao.getAllTodosList()
}
