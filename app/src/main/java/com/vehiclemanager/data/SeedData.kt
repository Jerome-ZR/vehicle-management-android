package com.vehiclemanager.data

import com.vehiclemanager.data.dao.*
import com.vehiclemanager.data.entity.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

object SeedData {
    private val df = SimpleDateFormat("yyyy.M.d", Locale.CHINA)

    fun seedAll(db: com.vehiclemanager.data.database.AppDatabase) = runBlocking {
        if (db.vehicleDao().getAllVehiclesList().isEmpty()) {
            db.vehicleDao().insertAll(getVehicles())
            db.maintenanceRecordDao().insertAll(getRecords())
        }
    }

    fun getVehicles() = listOf(
        veh("豫J0298警", "警车", "193,040 km", 193040, "203,040 km", 203040, "每年保养一次（后续按1年或1万公里）", "2026.3.10", "2027.3.10", "2027年2月", ""),
        veh("豫J0308警", "警车", "76,238 km", 76238, "86,238 km", 86238, "每年保养一次（后续按1年或1万公里）", "2026.3.9", "2027.3.9", "", ""),
        veh("豫JQ157Q", "警车", "11,533 km", 11533, "21,533 km", 21533, "每年保养一次（后续按1年或1万公里）", "2026.3.25", "2027.3.25", "", ""),
        veh("豫J5001警", "警用摩托车", "9,336 km", 9336, "11,624 km", 11624, "1年或3000公里", "2026.4.24", "剩余 2,288 km", "", "冯鹏杰"),
        veh("豫J5002警", "警用摩托车", "9,559 km", 9559, "11,830 km", 11830, "1年或3000公里", "2026.4.24", "剩余 2,271 km", "", "刘景灿"),
        veh("豫J5003警", "警用摩托车", "未测量", 0, "10,500 km", 10500, "1年或3000公里", "2026.4.24", "待提供公里数", "", "张超业"),
        veh("豫J5017警", "警用摩托车", "未测量", 0, "13,500 km", 13500, "1年或3000公里", "2026.3.22", "待提供公里数", "", "刘明明"),
        veh("豫J5016警", "警用摩托车", "15,009 km", 15009, "18,009 km", 18009, "1年或3000公里", "2026.7.22", "剩余 3,000 km", "", "孙蒙"),
        veh("豫J1002警", "警用摩托车", "10,806 km", 10806, "12,500 km", 12500, "1年或3000公里", "2026.1.6", "剩余 1,694 km", "", "李世鹏"),
        veh("豫J6052警", "警用摩托车", "13,114 km", 13114, "15,041 km", 15041, "1年或3000公里", "2026.3.18", "剩余 1,927 km", "", "管李静"),
        veh("豫J5012警", "警用摩托车", "15,263 km", 15263, "17,800 km", 17800, "1年或3000公里", "2026.5.11", "剩余 2,537 km", "", "韩渊博"),
        veh("豫J6011警", "警用摩托车", "8,225 km", 8225, "10,000 km", 10000, "1年或3000公里", "2026.3.12", "剩余 1,775 km", "", "训练"),
        veh("豫J5067警", "警用摩托车", "13,076 km", 13076, "14,642 km", 14642, "1年或3000公里", "2026.3.15", "剩余 1,566 km", "", "董栋"),
        veh("豫J5060警", "警用摩托车", "未测量", 0, "13,467 km", 13467, "1年或3000公里", "2026.3.12", "待提供公里数", "", "训练"),
        veh("豫J5018警", "警用摩托车", "16,752 km", 16752, "18,986 km", 18986, "1年或3000公里", "2026.3.12", "剩余 2,234 km", "", "袁启飞"),
        veh("豫J5026警", "警用摩托车", "14,806 km", 14806, "14,852 km", 14852, "1年或3000公里", "2026.6.22", "剩余 46 km", "", "冯尚超"),
        veh("豫J5068警", "警用摩托车", "未测量", 0, "8,621 km", 8621, "1年或3000公里", "2026.4.9", "待提供公里数", "", "训练"),
        veh("豫J5007警", "警用摩托车", "14,350 km", 14350, "17,350 km", 17350, "1年或3000公里", "2026.8.4", "剩余 3,000 km", "", ""),
        veh("豫J6010警", "警用摩托车", "12,595 km", 12595, "14,881 km", 14881, "1年或3000公里", "2026.5.11", "剩余 2,286 km", "", "宋子昊"),
        veh("豫J5010警", "警用摩托车", "13,512 km", 13512, "13,025 km", 13025, "1年或3000公里", "2025.10.23", "已超过 487 km", "", "冯尚超"),
        veh("豫J5009警", "警用摩托车", "未测量", 0, "16,908 km", 16908, "1年或3000公里", "2026.6.22", "待提供公里数", "", "韩风"),
        veh("豫J6015警", "警用摩托车", "未测量", 0, "9,182 km", 9182, "1年或3000公里", "无记录", "待提供公里数", "", "训练"),
        veh("豫J5011警", "警用摩托车", "13,219 km", 13219, "15,361 km", 15361, "1年或3000公里", "2026.5.12", "剩余 2,142 km", "", "许辰阳"),
        veh("豫J6059警", "警用摩托车", "13,810 km", 13810, "16,000 km", 16000, "1年或3000公里", "2026.5.11", "剩余 2,190 km", "", "周鑫"),
        veh("豫J6016警", "警用摩托车", "13,381 km", 13381, "15,152 km", 15152, "1年或3000公里", "2026.4.7", "剩余 1,771 km", "", "张银隆"),
        veh("豫J5021警", "警用摩托车", "14,217 km", 14217, "17,217 km", 17217, "1年或3000公里", "2026.7.22", "剩余 3,000 km", "", "冯梦"),
        veh("豫J1005警", "警用摩托车", "15,297 km", 15297, "16,200 km", 16200, "1年或3000公里", "2026.5.12", "剩余 903 km", "", "高强强"),
        veh("豫J5015警", "警用摩托车", "14,770 km", 14770, "17,500 km", 17500, "1年或3000公里", "2026.6.15", "剩余 2,730 km", "", "韩进河"),
        veh("豫J6005警", "警用摩托车", "18,145 km", 18145, "19,453 km", 19453, "1年或3000公里", "2026.3.3", "剩余 1,308 km", "", "李世鹏"),
        veh("豫J6036警", "警用摩托车", "未测量", 0, "16,000 km", 16000, "1年或3000公里", "2026.3.6", "待提供公里数", "", "衡鑫"),
        veh("豫J6060警", "警用摩托车", "13,774 km", 13774, "15,100 km", 15100, "1年或3000公里", "2026.1.5", "剩余 1,326 km", "", "陶光林"),
        veh("豫J5008警", "警用摩托车", "13,824 km", 13824, "16,500 km", 16500, "1年或3000公里", "2026.5.11", "剩余 2,676 km", "", "张利弯"),
        veh("豫J6003警", "警用摩托车", "14,042 km", 14042, "15,531 km", 15531, "1年或3000公里", "2026.3.13", "剩余 1,489 km", "", "魏帅港")
    )

    fun getRecords() = listOf(
        rec("5001", "2025.3.21", "保养：更换机油、机滤、空滤，清洗链条", 1340, "保养"),
        rec("5002", "2025.3.21", "保养：更换机油、机滤、空滤，清洗链条", 1220, "保养"),
        rec("5060", "2025.3.21", "更换电瓶", 580, "维修"),
        rec("1005", "2025.3.31", "保养：更换机油、机滤、空滤，清洗链条", 530, "保养", "洪亮"),
        rec("5001", "2025.6.13", "更换后刹车片，刹车油1桶，防冻液1桶", 480, "维修", "铁马"),
        rec("5001", "2026.4.9", "保养：更换机油、机滤、空滤", 960, "保养", "铁马"),
        rec("5001", "2026.4.24", "更换左前警灯支架，后轮补胎", 260, "维修", "洪亮"),
        rec("5067", "2025.4.14", "更换水箱，加注防冻液", 550, "维修"),
        rec("1005", "2025.4.17", "保养 9891km", 770, "保养", "铁马"),
        rec("1005", "2025.5.29", "更换排气管，更换后刹车总成", 405, "维修", "现奔宝"),
        rec("5007", "2025.8.5", "保养 14350km", 960, "保养")
    )

    fun getParts() = listOf(
        Part(partName="机油", shop="铁马维修", qty=2, unitPrice=130, amount=260),
        Part(partName="机油", shop="洪亮价格", qty=2, unitPrice=120, amount=240),
        Part(partName="机滤", shop="铁马维修", qty=1, unitPrice=60, amount=60),
        Part(partName="机滤", shop="洪亮价格", qty=1, unitPrice=40, amount=40),
        Part(partName="空滤", shop="铁马维修", qty=1, unitPrice=90, amount=90),
        Part(partName="空滤", shop="洪亮价格", qty=1, unitPrice=80, amount=80),
        Part(partName="链条保养", shop="铁马维修", qty=1, unitPrice=190, amount=190),
        Part(partName="前刹车片", shop="铁马维修", qty=2, unitPrice=280, amount=560),
        Part(partName="前刹车片", shop="洪亮价格", qty=2, unitPrice=150, amount=300),
        Part(partName="后刹车片", shop="铁马维修", qty=1, unitPrice=280, amount=280),
        Part(partName="后刹车片", shop="洪亮价格", qty=1, unitPrice=150, amount=150),
        Part(partName="防冻液", shop="铁马维修", qty=1, unitPrice=80, amount=80),
        Part(partName="防冻液", shop="洪亮价格", qty=1, unitPrice=50, amount=50)
    )

    private fun veh(plate: String, type: String, curKm: String, curKmNum: Int, tgtKm: String, tgtKmNum: Int,
                     rule: String, lastDate: String, nextMnt: String, inspDate: String, user: String) =
        Vehicle(plateNumber = plate, vehicleCode = "", brand = type, assignedUser = user,
            lastMaintenanceDate = parseDate(lastDate), lastMaintenanceKm = curKmNum,
            maintenanceIntervalKm = tgtKmNum - curKmNum, maintenanceIntervalDays = if(rule.contains("1年"))365 else 365,
            annualInspectionDate = parseDate(inspDate),
            notes = "规则:$rule 下次:$nextMnt" + if(user.isNotBlank()) " 使用人:$user" else "")

    private fun rec(plateShort: String, date: String, items: String, price: Int, type: String = "保养", loc: String = "") =
        MaintenanceRecord(vehicleId = 0, type = type, date = parseDate(date), items = items, location = loc, price = price.toDouble())

    private fun parseDate(s: String): Long {
        if (s.isBlank()) return 0L
        return try { df.parse(s.trim())?.time ?: 0L } catch (_: Exception) { 0L }
    }
}
