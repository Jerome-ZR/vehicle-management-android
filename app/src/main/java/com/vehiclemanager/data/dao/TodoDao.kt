package com.vehiclemanager.data.dao

import androidx.room.*
import com.vehiclemanager.data.entity.Todo
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY due_date ASC")
    fun getAllTodos(): Flow<List<Todo>>

    @Query("SELECT * FROM todos WHERE status = :status ORDER BY due_date ASC")
    fun getTodosByStatus(status: String): Flow<List<Todo>>

    @Query("SELECT * FROM todos WHERE type = :type ORDER BY due_date ASC")
    fun getTodosByType(type: String): Flow<List<Todo>>

    @Query("SELECT * FROM todos WHERE vehicle_id = :vehicleId ORDER BY due_date ASC")
    fun getTodosByVehicle(vehicleId: Long): Flow<List<Todo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: Todo): Long

    @Update
    suspend fun updateTodo(todo: Todo)

    @Delete
    suspend fun deleteTodo(todo: Todo)

    @Query("DELETE FROM todos WHERE vehicle_id = :vehicleId AND type = :type")
    suspend fun deleteByVehicleAndType(vehicleId: Long, type: String)

    @Query("SELECT * FROM todos")
    suspend fun getAllTodosList(): List<Todo>
}
