package com.vehiclemanager.util

import android.content.Context
import android.net.Uri
import com.vehiclemanager.data.entity.*
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook

data class ExportData(
    val vehicles: List<Vehicle>,
    val maintenanceRecords: List<MaintenanceRecord>,
    val kmRecords: List<KmRecord>,
    val todos: List<Todo>
)

object ExcelUtil {

    fun exportToExcel(context: Context, uri: Uri, data: ExportData) {
        val wb = XSSFWorkbook()
        val hs = headerStyle(wb); val cs = cellStyle(wb)

        // Sheet 1: 保养跟踪表 (matching user's actual Excel columns)
        val s1 = wb.createSheet("保养跟踪表")
        val h1 = arrayOf("车身编号","车牌号","车辆类型","当前公里数","应保养公里数","保养规则","上次保养时间","上次保养公里数","下次保养到期","备注","审车日期")
        row(s1, 0, h1, hs)
        data.vehicles.forEachIndexed { i, v ->
            val r = s1.createRow(i + 1)
            r.cell(0, v.vehicleCode, cs)             // 车身编号 (use vehicleCode for now)
            r.cell(1, v.plateNumber, cs)             // 车牌号
            r.cell(2, v.brand.ifBlank { "警用摩托车" }, cs) // 车辆类型 (use brand as type)
            r.cell(3, "${v.lastMaintenanceKm} km", cs)     // 当前公里数 (approximate)
            r.cell(4, "${v.lastMaintenanceKm + v.maintenanceIntervalKm} km", cs) // 应保养公里数
            r.cell(5, "1年或${v.maintenanceIntervalKm/1000}千公里", cs)           // 保养规则
            r.cell(6, if(v.lastMaintenanceDate>0) formatDate(v.lastMaintenanceDate) else "", cs)
            r.cell(7, "${v.lastMaintenanceKm} km", cs)
            r.cell(8, "", cs)
            r.cell(9, v.notes.ifBlank { "使用人: ${v.assignedUser}" }, cs)
            r.cell(10, if(v.annualInspectionDate>0) formatDate(v.annualInspectionDate) else "", cs)
        }
        for (c in h1.indices) s1.autoSizeColumn(c)

        // Sheet 2: 维修记录总表
        val s2 = wb.createSheet("维修记录总表")
        row(s2, 0, arrayOf("车牌号","时间","维修地点","维修项目","价格"), hs)
        data.maintenanceRecords.forEachIndexed { i, r ->
            val r2 = s2.createRow(i + 1)
            r2.cell(0, "", cs) // vehicleId as plate code placeholder
            r2.cell(1, formatDate(r.date), cs)
            r2.cell(2, r.location, cs)
            r2.cell(3, r.items, cs)
            r2.cell(4, r.price.toString(), cs)
        }
        for (c in 0..4) s2.autoSizeColumn(c)

        // Sheet 3: 公里数记录
        val s3 = wb.createSheet("公里数记录")
        row(s3, 0, arrayOf("车辆ID","公里数","记录日期","所属月份"), hs)
        data.kmRecords.forEachIndexed { i, r ->
            val r3 = s3.createRow(i + 1)
            r3.cell(0, r.vehicleId.toString(), cs); r3.cell(1, r.km.toString(), cs)
            r3.cell(2, formatDate(r.recordDate), cs); r3.cell(3, r.month, cs)
        }
        for (c in 0..3) s3.autoSizeColumn(c)

        context.contentResolver.openOutputStream(uri)?.use { wb.write(it) }
        wb.close()
    }

    fun importFromExcel(context: Context, uri: Uri): ExportData {
        val vehicles = mutableListOf<Vehicle>()
        val records = mutableListOf<MaintenanceRecord>()
        val kmRecs = mutableListOf<KmRecord>()

        context.contentResolver.openInputStream(uri)?.use { stream ->
            val wb = WorkbookFactory.create(stream)

            // Sheet 1: 保养跟踪表
            val s1 = wb.getSheetAt(0)
            if (s1 != null) {
                for (i in 2..s1.lastRowNum) {
                    val row = s1.getRow(i) ?: continue
                    val plate = row.cellStr(1)
                    if (plate.isBlank()) continue
                    vehicles.add(Vehicle(
                        plateNumber = plate,
                        vehicleCode = row.cellStr(0),
                        assignedUser = extractUser(row.cellStr(9)),
                        brand = row.cellStr(2).ifBlank { "警用摩托车" },
                        lastMaintenanceDate = parseDate(row.cellStr(6)),
                        lastMaintenanceKm = parseKm(row.cellStr(7)),
                        maintenanceIntervalKm = parseInterval(row.cellStr(5), 3000),
                        maintenanceIntervalDays = parseDaysInterval(row.cellStr(5), 365),
                        annualInspectionDate = parseDate(row.cellStr(10)),
                        status = "正常",
                        notes = row.cellStr(9)
                    ))
                }
            }

            // Sheet 2: 维修记录总表
            if (wb.numberOfSheets > 1) {
                val s2 = wb.getSheetAt(1)
                if (s2 != null) {
                    for (i in 2..s2.lastRowNum) {
                        val row = s2.getRow(i) ?: continue
                        val project = row.cellStr(3)
                        if (project.isBlank()) continue
                        records.add(MaintenanceRecord(
                            vehicleId = 0, // will be matched by plate later
                            type = if (project.contains("保养")) "保养" else "维修",
                            date = parseDate(row.cellStr(1)),
                            items = project,
                            location = row.cellStr(2),
                            price = row.cellDbl(4)
                        ))
                    }
                }
            }

            wb.close()
        }
        return ExportData(vehicles, records, kmRecs, emptyList())
    }

    // === helpers ===
    private fun formatDate(ts: Long): String {
        if (ts == 0L) return ""
        val df = java.text.SimpleDateFormat("yyyy.M.d", java.util.Locale.CHINA)
        return df.format(java.util.Date(ts))
    }
    private fun parseDate(s: String): Long {
        if (s.isBlank()) return 0L
        return try { java.text.SimpleDateFormat("yyyy.M.d", java.util.Locale.CHINA).parse(s.trim())?.time ?: 0L } catch(e:Exception) { 0L }
    }
    private fun parseKm(s: String): Int {
        val m = Regex("([\\d,]+)").find(s) ?: return 0
        return m.value.replace(",", "").toIntOrNull() ?: 0
    }
    private fun parseInterval(rule: String, default: Int): Int {
        val m = Regex("(\\d+)\\s*千公里").find(rule) ?: Regex("(\\d+)公里").find(rule)
        return m?.groupValues?.get(1)?.toIntOrNull()?.times(1000) ?: default
    }
    private fun parseDaysInterval(rule: String, default: Int): Int {
        if (rule.contains("1年")) return 365
        return default
    }
    private fun extractUser(remark: String): String {
        val m = Regex("使用人[:：]\\s*(.+?)([;；，,]|$)").find(remark)
        return m?.groupValues?.getOrNull(1)?.trim() ?: ""
    }

    private fun Row.cellStr(idx: Int) = try { getCell(idx)?.stringCellValue ?: "" } catch (_: Exception) { "" }
    private fun Row.cellInt(idx: Int) = try { getCell(idx)?.numericCellValue?.toInt() ?: 0 } catch (_: Exception) { 0 }
    private fun Row.cellDbl(idx: Int) = try { getCell(idx)?.numericCellValue ?: 0.0 } catch (_: Exception) { 0.0 }

    private fun Row.cell(idx: Int, value: String, style: CellStyle) {
        createCell(idx).apply { setCellValue(value); cellStyle = style }
    }
    private fun row(sheet: Sheet, idx: Int, values: Array<String>, style: CellStyle) {
        val r = sheet.createRow(idx)
        values.forEachIndexed { i, v -> r.createCell(i).apply { setCellValue(v); cellStyle = style } }
    }
    private fun headerStyle(wb: Workbook) = wb.createCellStyle().apply {
        fillForegroundColor = IndexedColors.GREY_25_PERCENT.index; fillPattern = FillPatternType.SOLID_FOREGROUND
        setFont(wb.createFont().apply { bold = true })
    }
    private fun cellStyle(wb: Workbook) = wb.createCellStyle()
}
