package com.vehiclemanager.ui.maintenance

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
import com.vehiclemanager.data.entity.MaintenanceRecord
import com.vehiclemanager.ui.components.*
import com.vehiclemanager.ui.theme.*
import com.vehiclemanager.util.DateUtils
import com.vehiclemanager.viewmodel.MaintenanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceScreen(
    viewModel: MaintenanceViewModel = viewModel(),
    vehicleId: Long? = null,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(vehicleId) {
        if (vehicleId != null) {
            viewModel.loadRecordsForVehicle(vehicleId)
        } else {
            viewModel.loadAllRecords()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("维保记录", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.startAdding() }) {
                        Icon(Icons.Default.Add, "添加记录")
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
            // Vehicle info header
            state.selectedVehicle?.let { vehicle ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Blue50)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DirectionsCar, null, tint = Blue700, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(vehicle.plateNumber, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(
                                "上次保养: ${DateUtils.formatDate(vehicle.lastMaintenanceDate)} · ${vehicle.lastMaintenanceKm}km",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.filterType == null,
                    onClick = { viewModel.loadAllRecords() },
                    label = { Text("全部") }
                )
                FilterChip(
                    selected = state.filterType == "保养",
                    onClick = { viewModel.filterByType("保养") },
                    label = { Text("保养") },
                    leadingIcon = { Icon(Icons.Default.Build, null, modifier = Modifier.size(16.dp)) }
                )
                FilterChip(
                    selected = state.filterType == "维修",
                    onClick = { viewModel.filterByType("维修") },
                    label = { Text("维修") },
                    leadingIcon = { Icon(Icons.Default.Construction, null, modifier = Modifier.size(16.dp)) }
                )
            }

            // Reminder
            state.reminderMessage?.let { msg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (msg.contains("暂无")) SuccessGreen.copy(alpha = 0.1f)
                        else WarningOrange.copy(alpha = 0.1f)
                    )
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = WarningOrange, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(msg, fontSize = 13.sp)
                    }
                }
            }

            if (state.isAdding) {
                MaintenanceEditForm(
                    record = state.editingRecord,
                    vehicleId = state.selectedVehicleId,
                    onSave = { viewModel.saveRecord(it) },
                    onCancel = { viewModel.cancelEditing() }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.records, key = { it.id }) { record ->
                        MaintenanceRecordCard(
                            record = record,
                            onEdit = { viewModel.startEditing(record) },
                            onDelete = { viewModel.deleteRecord(record) }
                        )
                    }

                    if (state.records.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.Build, null,
                                        tint = TextSecondary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("暂无维保记录", color = TextSecondary)
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

@Composable
fun MaintenanceRecordCard(
    record: MaintenanceRecord,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (record.type == "保养") Icons.Default.Build else Icons.Default.Construction,
                        null,
                        tint = if (record.type == "保养") Blue700 else Orange700,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            record.type,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (record.type == "保养") Blue700 else Orange700
                        )
                        Text(
                            DateUtils.formatDate(record.date),
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
                if (record.price > 0) {
                    Text(
                        "¥${String.format("%.2f", record.price)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ErrorRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    DetailRow("项目", record.items)
                    if (record.km > 0) DetailRow("公里数", "${record.km} km")
                }
                Column(modifier = Modifier.weight(1f)) {
                    if (record.location.isNotBlank()) DetailRow("地点", record.location)
                    if (record.notes.isNotBlank()) DetailRow("备注", record.notes)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("编辑")
                }
                TextButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp), tint = ErrorRed)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("删除", color = ErrorRed)
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除这条${record.type}记录吗？") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
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
fun MaintenanceEditForm(
    record: MaintenanceRecord?,
    vehicleId: Long?,
    onSave: (MaintenanceRecord) -> Unit,
    onCancel: () -> Unit
) {
    var type by remember { mutableStateOf(record?.type ?: "保养") }
    var dateStr by remember { mutableStateOf(DateUtils.formatDate(record?.date ?: System.currentTimeMillis())) }
    var km by remember { mutableStateOf(if (record?.km ?: 0 > 0) record!!.km.toString() else "") }
    var items by remember { mutableStateOf(record?.items ?: "") }
    var location by remember { mutableStateOf(record?.location ?: "") }
    var price by remember { mutableStateOf(if (record?.price ?: 0.0 > 0) record!!.price.toString() else "") }
    var notes by remember { mutableStateOf(record?.notes ?: "") }
    var typeExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(if (record == null) "添加记录" else "编辑记录")
        }

        // Type selector
        item {
            ExposedDropdownMenuBox(
                expanded = typeExpanded,
                onExpandedChange = { typeExpanded = it }
            ) {
                OutlinedTextField(
                    value = type,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("类型 *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    leadingIcon = {
                        Icon(
                            if (type == "保养") Icons.Default.Build else Icons.Default.Construction,
                            null
                        )
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                    shape = RoundedCornerShape(8.dp)
                )
                ExposedDropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("保养") },
                        onClick = { type = "保养"; typeExpanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("维修") },
                        onClick = { type = "维修"; typeExpanded = false }
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = dateStr,
                onValueChange = { dateStr = it },
                label = { Text("日期 (yyyy-MM-dd) *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )
        }

        if (type == "保养") {
            item {
                OutlinedTextField(
                    value = km,
                    onValueChange = { km = it },
                    label = { Text("保养时公里数") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = { Icon(Icons.Default.Speed, null) },
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        item {
            OutlinedTextField(
                value = items,
                onValueChange = { items = it },
                label = { Text("${type}项目 *") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("如: 更换机油、更换刹车片") },
                shape = RoundedCornerShape(8.dp)
            )
        }

        item {
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("${type}地点") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                shape = RoundedCornerShape(8.dp)
            )
        }

        item {
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("价格 (元)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Text("¥", fontSize = 18.sp, color = TextSecondary) },
                shape = RoundedCornerShape(8.dp)
            )
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
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text("取消")
                }
                Button(
                    onClick = {
                        if (type.isNotBlank() && items.isNotBlank()) {
                            onSave(
                                MaintenanceRecord(
                                    id = record?.id ?: 0,
                                    vehicleId = record?.vehicleId ?: vehicleId ?: 0,
                                    type = type,
                                    date = parseDate(dateStr),
                                    km = km.toIntOrNull() ?: 0,
                                    items = items,
                                    location = location,
                                    price = price.toDoubleOrNull() ?: 0.0,
                                    notes = notes
                                )
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = type.isNotBlank() && items.isNotBlank(),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("保存") }
            }
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}

private fun parseDate(dateStr: String): Long {
    if (dateStr.isBlank()) return System.currentTimeMillis()
    return try {
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.CHINA)
            .parse(dateStr)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) { System.currentTimeMillis() }
}
