package com.vehiclemanager.ui.todo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vehiclemanager.data.entity.Todo
import com.vehiclemanager.ui.components.*
import com.vehiclemanager.ui.theme.*
import com.vehiclemanager.util.DateUtils
import com.vehiclemanager.viewmodel.TodoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(
    viewModel: TodoViewModel = viewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshTodos()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("待办事项", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue700,
                    titleContentColor = TextOnPrimary,
                    navigationIconContentColor = TextOnPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.filterType == null,
                    onClick = { viewModel.filterByType(null) },
                    label = { Text("全部") }
                )
                FilterChip(
                    selected = state.filterType == "保养",
                    onClick = { viewModel.filterByType("保养") },
                    label = { Text("保养") },
                    leadingIcon = {
                        Icon(Icons.Default.Build, null, modifier = Modifier.size(16.dp))
                    }
                )
                FilterChip(
                    selected = state.filterType == "年审",
                    onClick = { viewModel.filterByType("年审") },
                    label = { Text("年审") },
                    leadingIcon = {
                        Icon(Icons.Default.Gavel, null, modifier = Modifier.size(16.dp))
                    }
                )
                FilterChip(
                    selected = state.filterType == "维修",
                    onClick = { viewModel.filterByType("维修") },
                    label = { Text("维修") },
                    leadingIcon = {
                        Icon(Icons.Default.Construction, null, modifier = Modifier.size(16.dp))
                    }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Summary
                item {
                    val pending = state.todos.count { it.status == "待处理" }
                    val completed = state.todos.count { it.status == "已完成" }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "待处理",
                            value = "$pending",
                            icon = Icons.Default.PendingActions,
                            backgroundColor = WarningOrange,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "已完成",
                            value = "$completed",
                            icon = Icons.Default.CheckCircle,
                            backgroundColor = SuccessGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                items(state.todos, key = { it.id }) { todo ->
                    TodoCard(
                        todo = todo,
                        onComplete = { viewModel.completeTodo(todo) },
                        onDelete = { viewModel.deleteTodo(todo) }
                    )
                }

                if (state.todos.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.CheckCircle, null,
                                    tint = TextSecondary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("暂无待办事项", color = TextSecondary, fontSize = 16.sp)
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
fun TodoCard(
    todo: Todo,
    onComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val daysLeft = if (todo.dueDate > 0) {
        (todo.dueDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)
    } else 0L

    val accentColor = when (todo.type) {
        "年审" -> Blue700
        "保养" -> Orange700
        "维修" -> SuccessGreen
        else -> TextSecondary
    }

    val urgencyColor = when {
        todo.status == "已完成" -> SuccessGreen
        daysLeft < 0 -> ErrorRed
        daysLeft <= 7 -> WarningOrange
        else -> accentColor
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (todo.status == "待处理") 2.dp else 0.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (todo.status == "已完成")
                SurfaceLight.copy(alpha = 0.5f) else Background
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    when (todo.type) {
                        "年审" -> Icons.Default.Gavel
                        "保养" -> Icons.Default.Build
                        else -> Icons.Default.Construction
                    },
                    null,
                    tint = if (todo.status == "已完成") TextSecondary else accentColor,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        todo.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (todo.status == "已完成") TextSecondary else TextPrimary
                    )
                    if (todo.description.isNotBlank()) {
                        Text(
                            todo.description.lines().first(),
                            fontSize = 13.sp,
                            color = TextSecondary,
                            maxLines = 2
                        )
                    }
                }
                StatusBadge(todo.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (todo.dueDate > 0) {
                    val dayText = when {
                        daysLeft < 0 -> "已过期 ${-daysLeft} 天"
                        daysLeft == 0L -> "今天截止"
                        else -> "剩余 $daysLeft 天"
                    }
                    Text(
                        "📅 ${DateUtils.formatDate(todo.dueDate)} · $dayText",
                        fontSize = 12.sp,
                        color = if (todo.status == "已处理") TextSecondary else urgencyColor
                    )
                } else {
                    Text("📅 未设置日期", fontSize = 12.sp, color = TextSecondary)
                }

                if (todo.status == "待处理") {
                    Row {
                        TextButton(onClick = onComplete) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp), tint = SuccessGreen)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("完成", color = SuccessGreen, fontSize = 13.sp)
                        }
                        TextButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp), tint = ErrorRed)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("删除", color = ErrorRed, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
