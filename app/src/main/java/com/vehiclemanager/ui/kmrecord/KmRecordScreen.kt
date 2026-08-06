package com.vehiclemanager.ui.kmrecord

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vehiclemanager.data.entity.Vehicle
import com.vehiclemanager.ui.components.*
import com.vehiclemanager.ui.theme.*
import com.vehiclemanager.util.DateUtils
import com.vehiclemanager.viewmodel.KmRecordViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KmRecordScreen(
    viewModel: KmRecordViewModel = viewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showVehiclePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("公里数录入", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                actions = {
                    if (state.selectedVehicle != null) {
                        IconButton(onClick = { viewModel.startAdding() }) {
                            Icon(Icons.Default.Add, "录入公里数")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue700,
                    titleContentColor = TextOnPrimary,
                    navigationIconContentColor = TextOnPrimary,
                    actionIconContentColor = TextOnPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Vehicle selector
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                onClick = { showVehiclePicker = true },
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DirectionsCar, null, tint = Blue700, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            state.selectedVehicle?.plateNumber ?: "选择车辆",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (state.selectedVehicle != null) TextPrimary else TextSecondary
                        )
                        state.selectedVehicle?.let { v ->
                            Text(
                                "${v.assignedUser} · ${v.vehicleCode}",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = TextSecondary)
                }
            }

            // Reminder
            state.reminderMessage?.let { msg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = WarningOrange.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, null, tint = WarningOrange, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(msg, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = WarningOrange)
                    }
                }
            }

            // Add km form
            if (state.isAdding) {
                KmInputForm(
                    currentMonth = state.currentMonth,
                    kmInput = state.kmInput,
                    onKmChange = { viewModel.setKmInput(it) },
                    onSave = { viewModel.saveKmRecord() },
                    onCancel = { viewModel.cancelAdding() }
                )
            }

            // Records list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.selectedVehicle != null) {
                    item { SectionHeader("公里数记录历史") }
                    items(state.records, key = { it.id }) { record ->
                        KmRecordCard(record = record)
                    }
                    if (state.records.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("暂无公里数记录", color = TextSecondary)
                            }
                        }
                    }
                } else {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Speed, null,
                                    tint = TextSecondary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("请先选择车辆", color = TextSecondary, fontSize = 16.sp)
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        }
    }

    // Vehicle picker dialog
    if (showVehiclePicker) {
        AlertDialog(
            onDismissRequest = { showVehiclePicker = false },
            title = { Text("选择车辆") },
            text = {
                LazyColumn {
                    items(state.vehicles) { vehicle ->
                        TextButton(
                            onClick = {
                                viewModel.selectVehicle(vehicle)
                                showVehiclePicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.DirectionsCar, null, tint = Blue700, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(vehicle.plateNumber, fontWeight = FontWeight.Medium)
                                    Text(
                                        "${vehicle.assignedUser} · ${vehicle.vehicleCode}",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showVehiclePicker = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun KmInputForm(
    currentMonth: String,
    kmInput: String,
    onKmChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Blue50)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "录入 ${currentMonth} 公里数",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = kmInput,
                onValueChange = onKmChange,
                label = { Text("当前公里数") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Icon(Icons.Default.Speed, null) },
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text("取消")
                }
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    enabled = kmInput.toIntOrNull() != null
                ) {
                    Text("保存")
                }
            }
        }
    }
}

@Composable
fun KmRecordCard(record: com.vehiclemanager.data.entity.KmRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Speed, null, tint = SuccessGreen, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${record.km} km",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = SuccessGreen
                )
                Text(
                    "${record.month} · ${DateUtils.formatDate(record.recordDate)}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
