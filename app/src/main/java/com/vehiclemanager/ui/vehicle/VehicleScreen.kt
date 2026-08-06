package com.vehiclemanager.ui.vehicle

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
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
import com.vehiclemanager.viewmodel.VehicleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleScreen(
    viewModel: VehicleViewModel = viewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("车辆档案", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.startEditing() }) {
                        Icon(Icons.Default.Add, "新增车辆")
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
            // Search bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("搜索车牌号/编号/使用人...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Blue700,
                    unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f)
                )
            )

            if (state.isEditing) {
                VehicleEditForm(
                    vehicle = state.selectedVehicle,
                    onSave = { viewModel.saveVehicle(it) },
                    onCancel = { viewModel.cancelEditing() }
                )
            } else if (state.selectedVehicle != null) {
                VehicleDetailView(
                    vehicle = state.selectedVehicle!!,
                    onEdit = { viewModel.startEditing(state.selectedVehicle) },
                    onDelete = {
                        viewModel.deleteVehicle(state.selectedVehicle!!)
                    },
                    onBack = { viewModel.selectVehicle(null) },
                    reminder = state.vehicleReminders[state.selectedVehicle!!.id]
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.vehicles, key = { it.id }) { vehicle ->
                        VehicleInfoCard(
                            plateNumber = vehicle.plateNumber,
                            vehicleCode = vehicle.vehicleCode,
                            assignedUser = vehicle.assignedUser,
                            brand = vehicle.brand,
                            status = vehicle.status,
                            onClick = { viewModel.selectVehicle(vehicle) }
                        )
                        val reminder = state.vehicleReminders[vehicle.id]
                        if (reminder != null) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(modifier = Modifier.padding(start = 60.dp)) {
                                ReminderChip(reminder.replace("\n", " · "))
                            }
                        }
                    }

                    if (state.vehicles.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.DirectionsCar,
                                        null,
                                        tint = TextSecondary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("暂无车辆", color = TextSecondary, fontSize = 16.sp)
                                    Text("点击右上角+号添加", color = TextSecondary, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleEditForm(
    vehicle: Vehicle?,
    onSave: (Vehicle) -> Unit,
    onCancel: () -> Unit
) {
    var plateNumber by remember { mutableStateOf(vehicle?.plateNumber ?: "") }
    var vehicleCode by remember { mutableStateOf(vehicle?.vehicleCode ?: "") }
    var assignedUser by remember { mutableStateOf(vehicle?.assignedUser ?: "") }
    var brand by remember { mutableStateOf(vehicle?.brand ?: "") }
    var maintenanceIntervalKm by remember {
        mutableStateOf((vehicle?.maintenanceIntervalKm ?: 5000).toString())
    }
    var maintenanceIntervalDays by remember {
        mutableStateOf((vehicle?.maintenanceIntervalDays ?: 180).toString())
    }
    var annualInspectionDateStr by remember {
        mutableStateOf(DateUtils.formatDate(vehicle?.annualInspectionDate ?: 0))
    }
    var status by remember { mutableStateOf(vehicle?.status ?: "正常") }
    var notes by remember { mutableStateOf(vehicle?.notes ?: "") }
    var statusExpanded by remember { mutableStateOf(false) }

    val statusOptions = listOf("正常", "维修中", "待报废")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader(if (vehicle == null) "新增车辆" else "编辑车辆") }

        item {
            OutlinedTextField(
                value = plateNumber,
                onValueChange = { plateNumber = it },
                label = { Text("车牌号 *") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.DirectionsCar, null) },
                shape = RoundedCornerShape(8.dp)
            )
        }

        item {
            OutlinedTextField(
                value = vehicleCode,
                onValueChange = { vehicleCode = it },
                label = { Text("车辆编号") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Numbers, null) },
                shape = RoundedCornerShape(8.dp)
            )
        }

        item {
            OutlinedTextField(
                value = assignedUser,
                onValueChange = { assignedUser = it },
                label = { Text("使用人") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Person, null) },
                shape = RoundedCornerShape(8.dp)
            )
        }

        item {
            OutlinedTextField(
                value = brand,
                onValueChange = { brand = it },
                label = { Text("品牌型号") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Info, null) },
                shape = RoundedCornerShape(8.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = maintenanceIntervalKm,
                    onValueChange = { maintenanceIntervalKm = it },
                    label = { Text("保养间隔(公里)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = maintenanceIntervalDays,
                    onValueChange = { maintenanceIntervalDays = it },
                    label = { Text("保养间隔(天)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        item {
            OutlinedTextField(
                value = annualInspectionDateStr,
                onValueChange = { annualInspectionDateStr = it },
                label = { Text("年审日期 (yyyy-MM-dd)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Gavel, null) },
                shape = RoundedCornerShape(8.dp)
            )
        }

        item {
            // Status dropdown
            ExposedDropdownMenuBox(
                expanded = statusExpanded,
                onExpandedChange = { statusExpanded = it }
            ) {
                OutlinedTextField(
                    value = status,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("状态") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                    shape = RoundedCornerShape(8.dp)
                )
                ExposedDropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false }
                ) {
                    statusOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                status = option
                                statusExpanded = false
                            }
                        )
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("备注") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                minLines = 2
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("取消")
                }
                Button(
                    onClick = {
                        if (plateNumber.isNotBlank()) {
                            val newVehicle = Vehicle(
                                id = vehicle?.id ?: 0,
                                plateNumber = plateNumber,
                                vehicleCode = vehicleCode,
                                assignedUser = assignedUser,
                                brand = brand,
                                maintenanceIntervalKm = maintenanceIntervalKm.toIntOrNull() ?: 5000,
                                maintenanceIntervalDays = maintenanceIntervalDays.toIntOrNull() ?: 180,
                                annualInspectionDate = parseDate(annualInspectionDateStr),
                                status = status,
                                notes = notes,
                                lastMaintenanceDate = vehicle?.lastMaintenanceDate ?: 0,
                                lastMaintenanceKm = vehicle?.lastMaintenanceKm ?: 0,
                                purchaseDate = vehicle?.purchaseDate ?: 0,
                                createdAt = vehicle?.createdAt ?: System.currentTimeMillis()
                            )
                            onSave(newVehicle)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = plateNumber.isNotBlank(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("保存")
                }
            }
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}

@Composable
fun VehicleDetailView(
    vehicle: Vehicle,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
    reminder: String?
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Blue700)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.DirectionsCar, null,
                            tint = TextOnPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                vehicle.plateNumber,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextOnPrimary
                            )
                            Text(
                                vehicle.vehicleCode,
                                fontSize = 14.sp,
                                color = TextOnPrimary.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    StatusBadge(vehicle.status)
                    if (reminder != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        ReminderChip(reminder.replace("\n", " "))
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader("基本信息")
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow("使用人", vehicle.assignedUser)
                    DetailRow("品牌型号", vehicle.brand.ifBlank { "未设置" })
                    DetailRow("年审日期", DateUtils.formatDate(vehicle.annualInspectionDate))
                    DetailRow("保养间隔", "${vehicle.maintenanceIntervalKm} 公里 / ${vehicle.maintenanceIntervalDays} 天")
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader("保养信息")
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow("最近保养日期", DateUtils.formatDate(vehicle.lastMaintenanceDate))
                    DetailRow("最近保养公里", "${vehicle.lastMaintenanceKm} km")
                    DetailRow("备注", vehicle.notes.ifBlank { "无" })
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue700),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("编辑")
                }
                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("删除")
                }
            }
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除车辆「${vehicle.plateNumber}」吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                    onBack()
                }) {
                    Text("删除", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

private fun parseDate(dateStr: String): Long {
    if (dateStr.isBlank()) return 0L
    return try {
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.CHINA)
            .parse(dateStr)?.time ?: 0L
    } catch (e: Exception) { 0L }
}
