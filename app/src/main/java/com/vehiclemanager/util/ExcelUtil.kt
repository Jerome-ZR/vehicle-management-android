package com.vehiclemanager.util

import android.content.Context
import android.net.Uri
import com.vehiclemanager.data.entity.*
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

data class ExportData(
    val vehicles: List<Vehicle>,
    val maintenanceRecords: List<MaintenanceRecord>,
    val kmRecords: List<KmRecord>,
    val todos: List<Todo>
)

object ExcelUtil {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)

    fun exportToExcel(context: Context, uri: Uri, data: ExportData) {
        val workbook = XSSFWorkbook()
        val headerStyle = createHeaderStyle(workbook)
        val cellStyle = createCellStyle(workbook)

        // Sheet 1: 车辆档案
        createVehicleSheet(workbook, data.vehicles, headerStyle, cellStyle)
        // Sheet 2: 维修保养记录
        createMaintenanceSheet(workbook, data.maintenanceRecords, headerStyle, cellStyle)
        // Sheet 3: 公里数记录
        createKmRecordSheet(workbook, data.kmRecords, headerStyle, cellStyle)
        // Sheet 4: 待办事项
        createTodoSheet(workbook, data.todos, headerStyle, cellStyle)

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            workbook.write(outputStream)
        }
        workbook.close()
    }

    fun importFromExcel(context: Context, uri: Uri): ExportData {
        val vehicles = mutableListOf<Vehicle>()
        val maintenanceRecords = mutableListOf<MaintenanceRecord>()
        val kmRecords = mutableListOf<KmRecord>()
        val todos = mutableListOf<Todo>()

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val workbook = WorkbookFactory.create(inputStream)

            // Read vehicles
            workbook.getSheetAt(0)?.let { sheet ->
                for (i in 1..sheet.lastRowNum) {
                    val row = sheet.getRow(i) ?: continue
                    vehicles.add(
                        Vehicle(
                            plateNumber = row.getCellString(0),
                            vehicleCode = row.getCellString(1),
                            assignedUser = row.getCellString(2),
                            brand = row.getCellString(3),
                            purchaseDate = parseDateSafely(row.getCellString(4)),
                            lastMaintenanceDate = parseDateSafely(row.getCellString(5)),
                            lastMaintenanceKm = row.getCellInt(6),
                            maintenanceIntervalKm = row.getCellInt(7).coerceAtLeast(1000),
                            maintenanceIntervalDays = row.getCellInt(8).coerceAtLeast(30),
                            annualInspectionDate = parseDateSafely(row.getCellString(9)),
                            status = row.getCellString(10).ifBlank { "正常" },
                            notes = row.getCellString(11)
                        )
                    )
                }
            }

            // Read maintenance records
            if (workbook.numberOfSheets > 1) {
                workbook.getSheetAt(1)?.let { sheet ->
                    for (i in 1..sheet.lastRowNum) {
                        val row = sheet.getRow(i) ?: continue
                        maintenanceRecords.add(
                            MaintenanceRecord(
                                vehicleId = row.getCellLong(0),
                                type = row.getCellString(1),
                                date = parseDateSafely(row.getCellString(2)),
                                km = row.getCellInt(3),
                                items = row.getCellString(4),
                                location = row.getCellString(5),
                                price = row.getCellDouble(6),
                                notes = row.getCellString(7)
                            )
                        )
                    }
                }
            }

            // Read km records
            if (workbook.numberOfSheets > 2) {
                workbook.getSheetAt(2)?.let { sheet ->
                    for (i in 1..sheet.lastRowNum) {
                        val row = sheet.getRow(i) ?: continue
                        kmRecords.add(
                            KmRecord(
                                vehicleId = row.getCellLong(0),
                                km = row.getCellInt(1),
                                recordDate = parseDateSafely(row.getCellString(2)),
                                month = row.getCellString(3)
                            )
                        )
                    }
                }
            }

            workbook.close()
        }

        return ExportData(vehicles, maintenanceRecords, kmRecords, todos)
    }

    private fun createVehicleSheet(
        workbook: Workbook, vehicles: List<Vehicle>,
        headerStyle: CellStyle, cellStyle: CellStyle
    ) {
        val sheet = workbook.createSheet("车辆档案")
        val headers = arrayOf(
            "车牌号", "车辆编号", "使用人", "品牌型号", "购置日期",
            "最近保养日期", "最近保养公里数", "保养间隔公里数",
            "保养间隔天数", "年审日期", "状态", "备注"
        )
        createHeader(sheet, headers, headerStyle)

        vehicles.forEachIndexed { index, vehicle ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).apply { setCellValue(vehicle.plateNumber); cellStyle = cellStyle }
            row.createCell(1).apply { setCellValue(vehicle.vehicleCode); cellStyle = cellStyle }
            row.createCell(2).apply { setCellValue(vehicle.assignedUser); cellStyle = cellStyle }
            row.createCell(3).apply { setCellValue(vehicle.brand); cellStyle = cellStyle }
            row.createCell(4).apply { setCellValue(formatDate(vehicle.purchaseDate)); cellStyle = cellStyle }
            row.createCell(5).apply { setCellValue(formatDate(vehicle.lastMaintenanceDate)); cellStyle = cellStyle }
            row.createCell(6).apply { setCellValue(vehicle.lastMaintenanceKm.toDouble()); cellStyle = cellStyle }
            row.createCell(7).apply { setCellValue(vehicle.maintenanceIntervalKm.toDouble()); cellStyle = cellStyle }
            row.createCell(8).apply { setCellValue(vehicle.maintenanceIntervalDays.toDouble()); cellStyle = cellStyle }
            row.createCell(9).apply { setCellValue(formatDate(vehicle.annualInspectionDate)); cellStyle = cellStyle }
            row.createCell(10).apply { setCellValue(vehicle.status); cellStyle = cellStyle }
            row.createCell(11).apply { setCellValue(vehicle.notes); cellStyle = cellStyle }
        }

        // Auto-size columns
        for (i in headers.indices) sheet.autoSizeColumn(i)
    }

    private fun createMaintenanceSheet(
        workbook: Workbook, records: List<MaintenanceRecord>,
        headerStyle: CellStyle, cellStyle: CellStyle
    ) {
        val sheet = workbook.createSheet("维修保养记录")
        val headers = arrayOf(
            "车辆ID", "类型", "日期", "公里数", "项目", "地点", "价格", "备注"
        )
        createHeader(sheet, headers, headerStyle)

        records.forEachIndexed { index, record ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).apply { setCellValue(record.vehicleId.toDouble()); cellStyle = cellStyle }
            row.createCell(1).apply { setCellValue(record.type); cellStyle = cellStyle }
            row.createCell(2).apply { setCellValue(formatDate(record.date)); cellStyle = cellStyle }
            row.createCell(3).apply { setCellValue(record.km.toDouble()); cellStyle = cellStyle }
            row.createCell(4).apply { setCellValue(record.items); cellStyle = cellStyle }
            row.createCell(5).apply { setCellValue(record.location); cellStyle = cellStyle }
            row.createCell(6).apply { setCellValue(record.price); cellStyle = cellStyle }
            row.createCell(7).apply { setCellValue(record.notes); cellStyle = cellStyle }
        }

        for (i in headers.indices) sheet.autoSizeColumn(i)
    }

    private fun createKmRecordSheet(
        workbook: Workbook, records: List<KmRecord>,
        headerStyle: CellStyle, cellStyle: CellStyle
    ) {
        val sheet = workbook.createSheet("公里数记录")
        val headers = arrayOf("车辆ID", "公里数", "记录日期", "所属月份")
        createHeader(sheet, headers, headerStyle)

        records.forEachIndexed { index, record ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).apply { setCellValue(record.vehicleId.toDouble()); cellStyle = cellStyle }
            row.createCell(1).apply { setCellValue(record.km.toDouble()); cellStyle = cellStyle }
            row.createCell(2).apply { setCellValue(formatDate(record.recordDate)); cellStyle = cellStyle }
            row.createCell(3).apply { setCellValue(record.month); cellStyle = cellStyle }
        }

        for (i in headers.indices) sheet.autoSizeColumn(i)
    }

    private fun createTodoSheet(
        workbook: Workbook, todos: List<Todo>,
        headerStyle: CellStyle, cellStyle: CellStyle
    ) {
        val sheet = workbook.createSheet("待办事项")
        val headers = arrayOf("车辆ID", "类型", "标题", "描述", "截止日期", "状态")
        createHeader(sheet, headers, headerStyle)

        todos.forEachIndexed { index, todo ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).apply { setCellValue(todo.vehicleId.toDouble()); cellStyle = cellStyle }
            row.createCell(1).apply { setCellValue(todo.type); cellStyle = cellStyle }
            row.createCell(2).apply { setCellValue(todo.title); cellStyle = cellStyle }
            row.createCell(3).apply { setCellValue(todo.description); cellStyle = cellStyle }
            row.createCell(4).apply { setCellValue(formatDate(todo.dueDate)); cellStyle = cellStyle }
            row.createCell(5).apply { setCellValue(todo.status); cellStyle = cellStyle }
        }

        for (i in headers.indices) sheet.autoSizeColumn(i)
    }

    private fun createHeader(sheet: Sheet, headers: Array<String>, style: CellStyle) {
        val row = sheet.createRow(0)
        headers.forEachIndexed { index, header ->
            row.createCell(index).apply {
                setCellValue(header)
                cellStyle = style
            }
        }
    }

    private fun createHeaderStyle(workbook: Workbook): CellStyle {
        return workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            val font = workbook.createFont().apply { bold = true }
            setFont(font)
            setBorderBottom(BorderStyle.THIN)
            setBorderTop(BorderStyle.THIN)
            setBorderLeft(BorderStyle.THIN)
            setBorderRight(BorderStyle.THIN)
        }
    }

    private fun createCellStyle(workbook: Workbook): CellStyle {
        return workbook.createCellStyle().apply {
            setBorderBottom(BorderStyle.THIN)
            setBorderTop(BorderStyle.THIN)
            setBorderLeft(BorderStyle.THIN)
            setBorderRight(BorderStyle.THIN)
        }
    }

    private fun formatDate(timestamp: Long): String =
        if (timestamp == 0L) "" else dateFormat.format(Date(timestamp))

    private fun parseDateSafely(dateStr: String): Long {
        if (dateStr.isBlank()) return 0L
        return try {
            dateFormat.parse(dateStr)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    // Helper extensions for safe cell reading
    private fun Row.getCellString(index: Int): String {
        return try { getCell(index)?.stringCellValue ?: "" } catch (e: Exception) { "" }
    }

    private fun Row.getCellInt(index: Int): Int {
        return try { getCell(index)?.numericCellValue?.toInt() ?: 0 } catch (e: Exception) { 0 }
    }

    private fun Row.getCellDouble(index: Int): Double {
        return try { getCell(index)?.numericCellValue ?: 0.0 } catch (e: Exception) { 0.0 }
    }

    private fun Row.getCellLong(index: Int): Long {
        return try { getCell(index)?.numericCellValue?.toLong() ?: 0L } catch (e: Exception) { 0L }
    }
}
