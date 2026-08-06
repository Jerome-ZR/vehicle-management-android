package com.vehiclemanager.util

import android.content.Context
import android.net.Uri
import com.vehiclemanager.data.entity.*
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.text.SimpleDateFormat
import java.util.*

data class ExportData(
    val vehicles: List<Vehicle>,
    val maintenanceRecords: List<MaintenanceRecord>,
    val kmRecords: List<KmRecord>,
    val todos: List<Todo>
)

object ExcelUtil {
    private val df = SimpleDateFormat("yyyy.M.d", Locale.CHINA)

    fun exportToExcel(ctx: Context, uri: Uri, data: ExportData) {
        val wb = XSSFWorkbook(); val hs = headerStyle(wb); val cs = cellStyle(wb)

        // Sheet 1: 保养跟踪表
        val s1 = wb.createSheet("保养跟踪表")
        row(s1, 0, arrayOf("车身编号","车牌号","车辆类型","当前公里数","应保养公里数","保养规则","上次保养时间","上次保养公里数","下次保养到期","备注","审车日期"), hs)
        data.vehicles.forEachIndexed { i, v ->
            val r = s1.createRow(i + 1)
            val ext = extractRule(v.notes)
            r.cell(0, v.vehicleCode, cs)
            r.cell(1, v.plateNumber, cs)
            r.cell(2, v.brand.ifBlank { "警用摩托车" }, cs)
            r.cell(3, if(v.lastMaintenanceKm > 0) "${v.lastMaintenanceKm} km" else "未测量", cs)
            r.cell(4, if(v.maintenanceIntervalKm > 0) "${v.lastMaintenanceKm + v.maintenanceIntervalKm} km" else "${v.maintenanceIntervalKm} km", cs)
            r.cell(5, ext.first, cs)
            r.cell(6, if(v.lastMaintenanceDate > 0) df.format(Date(v.lastMaintenanceDate)) else "", cs)
            r.cell(7, if(v.lastMaintenanceKm > 0) "${v.lastMaintenanceKm} km" else "", cs)
            r.cell(8, ext.second, cs)
            r.cell(9, v.notes, cs)
            r.cell(10, if(v.annualInspectionDate > 0) df.format(Date(v.annualInspectionDate)) else "", cs)
        }
        (0..10).forEach { s1.autoSizeColumn(it) }

        // Sheet 2: 维修记录总表
        val s2 = wb.createSheet("维修记录总表")
        row(s2, 0, arrayOf("车牌号","时间","维修地点","维修项目","价格"), hs)
        data.maintenanceRecords.forEachIndexed { i, r ->
            val r2 = s2.createRow(i + 1)
            r2.cell(0, "${r.vehicleId}", cs)
            r2.cell(1, if(r.date > 0) df.format(Date(r.date)) else "", cs)
            r2.cell(2, r.location, cs)
            r2.cell(3, r.items, cs)
            r2.cell(4, "${r.price.toInt()}", cs)
        }
        (0..4).forEach { s2.autoSizeColumn(it) }

        // Sheet 3: 公里数记录
        val s3 = wb.createSheet("公里数记录")
        row(s3, 0, arrayOf("车辆ID","公里数","日期","月份"), hs)
        data.kmRecords.forEachIndexed { i, r ->
            val r3 = s3.createRow(i + 1)
            r3.cell(0, "${r.vehicleId}", cs); r3.cell(1, "${r.km}", cs)
            r3.cell(2, if(r.recordDate > 0) df.format(Date(r.recordDate)) else "", cs); r3.cell(3, r.month, cs)
        }
        (0..3).forEach { s3.autoSizeColumn(it) }

        ctx.contentResolver.openOutputStream(uri)?.use { wb.write(it) }
        wb.close()
    }

    fun importFromExcel(ctx: Context, uri: Uri): ExportData {
        val vehicles = mutableListOf<Vehicle>()
        val records = mutableListOf<MaintenanceRecord>()
        val kmRecs = mutableListOf<KmRecord>()
        ctx.contentResolver.openInputStream(uri)?.use { s ->
            val wb = WorkbookFactory.create(s)
            val s1 = wb.getSheetAt(0)
            if (s1 != null) for (i in 2..s1.lastRowNum) {
                val row = s1.getRow(i) ?: continue; val plate = row.cs(1); if(plate.isBlank()) continue
                vehicles.add(Vehicle(plateNumber=plate, vehicleCode=row.cs(0), brand=row.cs(2).ifBlank{"警用摩托车"},
                    assignedUser=extractUser(row.cs(9)), lastMaintenanceDate=parseDate(row.cs(6)),
                    lastMaintenanceKm=parseKm(row.cs(7)), maintenanceIntervalKm=parseInterval(row.cs(5)),
                    maintenanceIntervalDays=if(row.cs(5).contains("1年"))365 else 365,
                    annualInspectionDate=parseDate(row.cs(10)), notes=row.cs(9)))
            }
            if (wb.numberOfSheets > 1) { val s2 = wb.getSheetAt(1)
                if (s2 != null) for (i in 2..s2.lastRowNum) {
                    val row = s2.getRow(i) ?: continue; val pj = row.cs(3); if(pj.isBlank()) continue
                    records.add(MaintenanceRecord(vehicleId=0, type=if(pj.contains("保养"))"保养" else "维修",
                        date=parseDate(row.cs(1)), items=pj, location=row.cs(2), price=row.cd(4)))
                }
            }
            wb.close()
        }
        return ExportData(vehicles, records, kmRecs, emptyList())
    }

    // helpers
    private fun extractRule(notes: String): Pair<String, String> {
        val rule = Regex("规则:([^ ]*)").find(notes)?.groupValues?.get(1) ?: ""
        val next = Regex("下次:([^ ]*)").find(notes)?.groupValues?.get(1) ?: ""
        return rule to next
    }
    private fun extractUser(notes: String) = Regex("使用人[:：]\\s*(\\S+)").find(notes)?.groupValues?.getOrNull(1) ?: ""
    private fun parseDate(s: String): Long { if(s.isBlank()) return 0L; return try{df.parse(s.trim())?.time?:0L} catch(_:Exception){0L} }
    private fun parseKm(s: String) = Regex("([\\d,]+)").find(s)?.value?.replace(",","")?.toIntOrNull() ?: 0
    private fun parseInterval(rule: String): Int { val m=Regex("(\\d+)\\s*千公里").find(rule)?:Regex("(\\d+)\\s*公里").find(rule); return m?.groupValues?.get(1)?.toIntOrNull()?.times(1000)?:5000 }
    private fun Row.cs(i:Int) = try{getCell(i)?.stringCellValue?:""}catch(_:Exception){""}
    private fun Row.cd(i:Int) = try{getCell(i)?.numericCellValue?:0.0} catch(_:Exception){0.0}
    private fun Row.cell(i:Int, v:String, s:CellStyle) { createCell(i).apply { setCellValue(v); cellStyle = s } }
    private fun row(sheet:Sheet, i:Int, vals:Array<String>, s:CellStyle) { val r=sheet.createRow(i); vals.forEachIndexed{j,v->r.createCell(j).apply{setCellValue(v);cellStyle=s}} }
    private fun headerStyle(wb:Workbook) = wb.createCellStyle().apply {
        fillForegroundColor = IndexedColors.GREY_25_PERCENT.index; fillPattern = FillPatternType.SOLID_FOREGROUND
        setFont(wb.createFont().apply { bold = true })
    }
    private fun cellStyle(wb:Workbook) = wb.createCellStyle()
}
