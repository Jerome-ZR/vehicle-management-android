package com.vehiclemanager.util

import com.vehiclemanager.data.entity.Vehicle

object MaintenanceReminder {
    /**
     * Check if a vehicle is due for maintenance (by km or by days).
     * Returns a pair: (isDue, description)
     */
    fun checkMaintenanceDue(vehicle: Vehicle, currentKm: Int? = null): Pair<Boolean, String> {
        val now = System.currentTimeMillis()

        // Check by days
        if (vehicle.lastMaintenanceDate > 0) {
            val nextDate = vehicle.lastMaintenanceDate +
                    vehicle.maintenanceIntervalDays * 24L * 60 * 60 * 1000
            val daysLeft = (nextDate - now) / (24 * 60 * 60 * 1000)

            if (daysLeft <= 0) {
                return Pair(true, "保养已过期 ${-daysLeft} 天")
            }
            if (daysLeft <= 30) {
                return Pair(true, "距保养日期还有 $daysLeft 天")
            }
        }

        // Check by km
        val km = currentKm ?: return Pair(false, "")
        if (vehicle.lastMaintenanceKm > 0) {
            val nextKm = vehicle.lastMaintenanceKm + vehicle.maintenanceIntervalKm
            val kmLeft = nextKm - km

            if (kmLeft <= 0) {
                return Pair(true, "保养公里数已超 ${-kmLeft} 公里")
            }
            if (kmLeft <= 500) {
                return Pair(true, "距保养公里数还有 $kmLeft 公里")
            }
        }

        return Pair(false, "")
    }

    /**
     * Check annual inspection due for a vehicle.
     */
    fun checkAnnualInspectionDue(vehicle: Vehicle): Pair<Boolean, String> {
        if (vehicle.annualInspectionDate == 0L) return Pair(false, "")

        val now = System.currentTimeMillis()
        val daysLeft = (vehicle.annualInspectionDate - now) / (24 * 60 * 60 * 1000)

        return when {
            daysLeft < 0 -> Pair(true, "年审已过期 ${-daysLeft} 天")
            daysLeft <= 30 -> Pair(true, "距离年审还有 $daysLeft 天")
            else -> Pair(false, "")
        }
    }
}
