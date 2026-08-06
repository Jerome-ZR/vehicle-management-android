package com.vehiclemanager.backup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vehiclemanager.VehicleApp
import com.vehiclemanager.ui.components.SectionHeader
import com.vehiclemanager.ui.theme.*
import com.vehiclemanager.util.BackupData
import com.vehiclemanager.util.BackupUtil
import com.vehiclemanager.util.DateUtils
import com.vehiclemanager.util.ExcelUtil
import com.vehiclemanager.util.ExportData
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as VehicleApp
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var backupFiles by remember { mutableStateOf<List<File>>(emptyList()) }

    // Export Excel launcher
    val exportExcelLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri ->
        uri?.let {
            scope.launch {
                isProcessing = true
                try {
                    val data = ExportData(
                        vehicles = app.vehicleRepository.getAllVehiclesList(),
                        maintenanceRecords = app.maintenanceRepository.getAllRecordsList(),
                        kmRecords = app.kmRecordRepository.getAllRecordsList(),
                        todos = app.todoRepository.getAllTodosList()
                    )
                    ExcelUtil.exportToExcel(context, it, data)
                    message = "✅ Excel 导出成功"
                } catch (e: Exception) {
                    message = "❌ 导出失败: ${e.message}"
                }
                isProcessing = false
            }
        }
    }

    // Import Excel launcher
    val importExcelLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch {
                isProcessing = true
                try {
                    val data = ExcelUtil.importFromExcel(context, it)
                    // Import vehicles
                    data.vehicles.forEach { app.vehicleRepository.insertVehicle(it) }
                    // Import maintenance records
                    data.maintenanceRecords.forEach { app.maintenanceRepository.insertRecord(it) }
                    // Import km records
                    data.kmRecords.forEach { app.kmRecordRepository.insertRecord(it) }
                    message = "✅ 导入成功: ${data.vehicles.size} 辆车, ${data.maintenanceRecords.size} 条维保记录, ${data.kmRecords.size} 条公里记录"
                } catch (e: Exception) {
                    message = "❌ 导入失败: ${e.message}"
                }
                isProcessing = false
            }
        }
    }

    // Export backup launcher
    val exportBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            scope.launch {
                isProcessing = true
                try {
                    val data = BackupData(
                        vehicles = app.vehicleRepository.getAllVehiclesList(),
                        maintenanceRecords = app.maintenanceRepository.getAllRecordsList(),
                        kmRecords = app.kmRecordRepository.getAllRecordsList(),
                        todos = app.todoRepository.getAllTodosList()
                    )
                    BackupUtil.exportBackup(context, it, data)
                    message = "✅ 备份导出成功"
                    backupFiles = BackupUtil.getBackupFiles(context)
                } catch (e: Exception) {
                    message = "❌ 备份失败: ${e.message}"
                }
                isProcessing = false
            }
        }
    }

    // Import backup launcher
    val importBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch {
                isProcessing = true
                try {
                    val backup = BackupUtil.importBackup(context, it)
                    if (backup != null) {
                        // Restore vehicles
                        backup.vehicles.forEach { app.vehicleRepository.insertVehicle(it) }
                        // Restore maintenance records
                        backup.maintenanceRecords.forEach { app.maintenanceRepository.insertRecord(it) }
                        // Restore km records
                        backup.kmRecords.forEach { app.kmRecordRepository.insertRecord(it) }
                        // Restore todos
                        backup.todos.forEach { app.todoRepository.insertTodo(it) }
                        message = "✅ 数据还原成功: ${backup.vehicles.size} 辆车"
                    } else {
                        message = "❌ 备份文件格式错误"
                    }
                    backupFiles = BackupUtil.getBackupFiles(context)
                } catch (e: Exception) {
                    message = "❌ 还原失败: ${e.message}"
                }
                isProcessing = false
            }
        }
    }

    LaunchedEffect(Unit) {
        backupFiles = BackupUtil.getBackupFiles(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("导入导出 · 备份还原", fontWeight = FontWeight.Bold) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Message
            message?.let { msg ->
                item {
                    val isError = msg.contains("❌")
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isError) ErrorRed.copy(alpha = 0.1f)
                            else SuccessGreen.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(modifier = Modifier.padding(12.dp)) {
                            Icon(
                                if (isError) Icons.Default.Error else Icons.Default.CheckCircle,
                                null,
                                tint = if (isError) ErrorRed else SuccessGreen
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(msg)
                        }
                    }
                }
            }

            // Loading indicator
            if (isProcessing) {
                item {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }

            // Excel section
            item { SectionHeader("📊 Excel 导入导出") }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionCard(
                        icon = Icons.Default.FileDownload,
                        title = "导出 Excel",
                        description = "导出全部数据为 Excel 文件",
                        color = SuccessGreen,
                        onClick = {
                            exportExcelLauncher.launch("车辆管理数据_${DateUtils.formatDate(System.currentTimeMillis())}.xlsx")
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ActionCard(
                        icon = Icons.Default.FileUpload,
                        title = "导入 Excel",
                        description = "从 Excel 文件导入数据",
                        color = Blue700,
                        onClick = {
                            importExcelLauncher.launch(arrayOf(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                "application/vnd.ms-excel"
                            ))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Backup section
            item { SectionHeader("💾 数据备份还原") }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionCard(
                        icon = Icons.Default.Backup,
                        title = "备份数据",
                        description = "导出 JSON 备份文件",
                        color = Orange700,
                        onClick = {
                            // Save internal first
                            scope.launch {
                                val data = BackupData(
                                    vehicles = app.vehicleRepository.getAllVehiclesList(),
                                    maintenanceRecords = app.maintenanceRepository.getAllRecordsList(),
                                    kmRecords = app.kmRecordRepository.getAllRecordsList(),
                                    todos = app.todoRepository.getAllTodosList()
                                )
                                BackupUtil.exportToInternalFile(context, data)
                                backupFiles = BackupUtil.getBackupFiles(context)
                            }
                            exportBackupLauncher.launch("车辆管理_备份_${DateUtils.formatDate(System.currentTimeMillis())}.json")
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ActionCard(
                        icon = Icons.Default.Restore,
                        title = "还原数据",
                        description = "从备份文件还原",
                        color = WarningOrange,
                        onClick = {
                            importBackupLauncher.launch(arrayOf("application/json"))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Internal backups
            if (backupFiles.isNotEmpty()) {
                item { SectionHeader("📁 本地备份列表") }
                items(backupFiles) { file ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Backup, null, tint = Orange700, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(file.name, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                Text(
                                    DateUtils.formatFull(file.lastModified()),
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Signature
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "🚗 车辆管理工作台",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Blue700
                        )
                        Text(
                            "车队管理 · 智能提醒 · 数据安全",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            "v1.0.0 | Made with ❤️",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun ActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                description,
                fontSize = 11.sp,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
