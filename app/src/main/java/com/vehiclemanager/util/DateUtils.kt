package com.vehiclemanager.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.CHINA)
    private val fullFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)

    fun formatDate(timestamp: Long): String {
        if (timestamp == 0L) return "未设置"
        return dateFormat.format(Date(timestamp))
    }

    fun formatMonth(timestamp: Long): String = monthFormat.format(Date(timestamp))
    fun formatFull(timestamp: Long): String = fullFormat.format(Date(timestamp))

    fun currentMonth(): String = monthFormat.format(Date())
    fun todayStart(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun daysBetween(from: Long, to: Long): Int {
        val diff = to - from
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }

    fun daysFromNow(timestamp: Long): Int = daysBetween(todayStart(), timestamp)

    fun addDays(timestamp: Long, days: Int): Long = timestamp + days * 24 * 60 * 60 * 1000L
}
