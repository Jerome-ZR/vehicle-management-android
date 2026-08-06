package com.vehiclemanager.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vehiclemanager.ui.components.*
import com.vehiclemanager.ui.theme.*
import com.vehiclemanager.util.DateUtils
import com.vehiclemanager.viewmodel.TodoViewModel
import com.vehiclemanager.viewmodel.VehicleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vehicleViewModel: VehicleViewModel = viewModel(),
    todoViewModel: TodoViewModel = viewModel(),
    onNavigateToVehicles: () -> Unit,
    onNavigateToMaintenance: () -> Unit,
    onNavigateToKmRecord: () -> Unit,
    onNavigateToTodos: () -> Unit,
    onNavigateToBackup: () -> Unit
) {
    val vehicleState by vehicleViewModel.uiState.collectAsState()
    val todoState by todoViewModel.uiState.collectAsState()

    // Calculate stats
    val totalVehicles = vehicleState.vehicles.size
    val pendingTodos = todoState.todos.count { it.status == "待处理" }
    val overdueItems = todoState.todos.count { todo ->
        todo.status == "待处理" && todo.dueDate > 0 && todo.dueDate < System.currentTimeMillis()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("🚗 车辆管理工作台", fontWeight = FontWeight.Bold)
                        Text(
                            "车队管理 · 智能提醒",
                            fontSize = 12.sp,
                            color = TextOnPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue700,
                    titleContentColor = TextOnPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToVehicles,
                containerColor = Orange700
            ) {
                Icon(Icons.Default.Add, "添加车辆", tint = TextOnPrimary)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stats cards
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "车辆总数",
                        value = "$totalVehicles",
                        icon = Icons.Default.DirectionsCar,
                        backgroundColor = Blue700,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "待办事项",
                        value = "$pendingTodos",
                        icon = Icons.Default.TaskAlt,
                        backgroundColor = Orange700,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "已过期",
                        value = "$overdueItems",
                        icon = Icons.Default.Warning,
                        backgroundColor = ErrorRed,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "今日日期",
                        value = DateUtils.formatDate(System.currentTimeMillis()),
                        icon = Icons.Default.CalendarMonth,
                        backgroundColor = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick actions
            item {
                SectionHeader("快捷操作")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionButton(
                        icon = Icons.Default.DirectionsCar,
                        label = "车辆档案",
                        color = Blue700,
                        onClick = onNavigateToVehicles,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        icon = Icons.Default.Build,
                        label = "维保记录",
                        color = Orange700,
                        onClick = onNavigateToMaintenance,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        icon = Icons.Default.Speed,
                        label = "公里录入",
                        color = SuccessGreen,
                        onClick = onNavigateToKmRecord,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        icon = Icons.Default.Assignment,
                        label = "待办事项",
                        color = WarningOrange,
                        onClick = onNavigateToTodos,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Pending reminders
            item {
                if (todoState.todos.isNotEmpty()) {
                    SectionHeader(
                        title = "⚠️ 到期提醒",
                        action = {
                            TextButton(onClick = onNavigateToTodos) {
                                Text("全部", color = Blue700)
                            }
                        }
                    )
                }
            }

            // Overdue items
            val pendingTodosList = todoState.todos
                .filter { it.status == "待处理" }
                .sortedBy { it.dueDate }
                .take(5)

            items(pendingTodosList) { todo ->
                TodoReminderCard(todo = todo, onClick = onNavigateToTodos)
            }

            // Recent vehicles
            item {
                if (vehicleState.vehicles.isNotEmpty()) {
                    SectionHeader(
                        title = "车辆列表",
                        action = {
                            TextButton(onClick = onNavigateToVehicles) {
                                Text("全部", color = Blue700)
                            }
                        }
                    )
                }
            }

            items(
                vehicleState.vehicles.take(5),
                key = { it.id }
            ) { vehicle ->
                val reminder = vehicleState.vehicleReminders[vehicle.id]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateToVehicles,
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DirectionsCar,
                            null,
                            tint = Blue700,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    vehicle.plateNumber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                StatusBadge(vehicle.status)
                            }
                            Text(
                                "${vehicle.assignedUser} · ${vehicle.vehicleCode}",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                            if (reminder != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                ReminderChip(reminder.replace("\n", " "))
                            }
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            null,
                            tint = TextSecondary
                        )
                    }
                }
            }

            // Bottom spacer
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 11.sp, color = color, fontWeight = FontWeight.Medium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoReminderCard(todo: com.vehiclemanager.data.entity.Todo, onClick: () -> Unit) {
    val daysLeft = if (todo.dueDate > 0) {
        (todo.dueDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)
    } else 0L

    val bgColor = when {
        daysLeft < 0 -> ErrorRed.copy(alpha = 0.08f)
        daysLeft <= 7 -> WarningOrange.copy(alpha = 0.08f)
        else -> Blue50
    }
    val borderColor = when {
        daysLeft < 0 -> ErrorRed
        daysLeft <= 7 -> WarningOrange
        else -> Blue700
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when (todo.type) {
                    "年审" -> Icons.Default.Gavel
                    "保养" -> Icons.Default.Build
                    else -> Icons.Default.Warning
                },
                null,
                tint = borderColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(todo.title, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Text(
                    if (todo.dueDate > 0) "截止: ${DateUtils.formatDate(todo.dueDate)}" else "未设置日期",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            val dayText = when {
                daysLeft < 0 -> "已过期${-daysLeft}天"
                daysLeft == 0L -> "今天"
                else -> "${daysLeft}天后"
            }
            Text(dayText, fontSize = 12.sp, color = borderColor, fontWeight = FontWeight.Bold)
        }
    }
}
