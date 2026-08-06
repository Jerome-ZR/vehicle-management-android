package com.vehiclemanager.util

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vehiclemanager.data.entity.*
import java.io.*

data class BackupData(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val vehicles: List<Vehicle>,
    val maintenanceRecords: List<MaintenanceRecord>,
    val kmRecords: List<KmRecord>,
    val todos: List<Todo>
)

object BackupUtil {
    private val gson = Gson()

    fun exportBackup(context: Context, uri: Uri, data: BackupData): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                val json = gson.toJson(data)
                outputStream.write(json.toByteArray(Charsets.UTF_8))
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun importBackup(context: Context, uri: Uri): BackupData? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val json = inputStream.bufferedReader(Charsets.UTF_8).readText()
                gson.fromJson(json, BackupData::class.java)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportToInternalFile(context: Context, data: BackupData): File {
        val backupDir = File(context.filesDir, "backups")
        if (!backupDir.exists()) backupDir.mkdirs()

        val file = File(backupDir, "backup_${data.timestamp}.json")
        val json = gson.toJson(data)
        file.writeText(json, Charsets.UTF_8)
        return file
    }

    fun getBackupFiles(context: Context): List<File> {
        val backupDir = File(context.filesDir, "backups")
        if (!backupDir.exists()) return emptyList()
        return backupDir.listFiles()?.filter { it.extension == "json" }?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }
}
