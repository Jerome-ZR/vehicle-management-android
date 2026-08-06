package com.vehiclemanager.ui.todo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vehiclemanager.VehicleApp
import com.vehiclemanager.data.entity.Vehicle
import com.vehiclemanager.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current; val app = ctx.applicationContext as VehicleApp; val sc = rememberCoroutineScope()
    var vehicles by remember { mutableStateOf<List<Vehicle>>(emptyList()) }
    LaunchedEffect(Unit) { sc.launch { vehicles = app.database.vehicleDao().getAllVehiclesList() } }

    val overdue = vehicles.filter { isOverdue(it) }
    val near = vehicles.filter { !isOverdue(it) && remainingKm(it) in 1..70 }
    val insp = vehicles.filter { it.annualInspectionDate > 0 && monthsUntil(it.annualInspectionDate) in 1..3 }

    Scaffold(topBar = { TopAppBar(title={Text("提醒中心",fontWeight=FontWeight.Bold)},navigationIcon={IconButton(onClick=onBack){Icon(Icons.Default.ArrowBack,"")}},colors=TopAppBarDefaults.topAppBarColors(containerColor=Blue700,titleContentColor=Color.White,navigationIconContentColor=Color.White))}) { p ->
        LazyColumn(Modifier.fillMaxSize().padding(p).padding(12.dp), verticalArrangement=Arrangement.spacedBy(8.dp)) {
            if (overdue.isNotEmpty()) { item{Text("🔴 逾期保养 (${overdue.size})",fontWeight=FontWeight.Bold,fontSize=16.sp,color=ErrorRed)}; items(overdue){RemCard(it,"urgent")} }
            if (near.isNotEmpty()) { item{Text("🟡 即将保养 (${near.size})",fontWeight=FontWeight.Bold,fontSize=16.sp,color=Orange700,modifier=Modifier.padding(top=8.dp))}; items(near){RemCard(it,"near")} }
            if (insp.isNotEmpty()) { item{Text("🔵 年审提醒 (${insp.size})",fontWeight=FontWeight.Bold,fontSize=16.sp,color=Blue700,modifier=Modifier.padding(top=8.dp))}
                items(insp){v->Card(modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(8.dp),colors=CardDefaults.cardColors(containerColor=Blue50)){
                    Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically){
                        Surface(shape=RoundedCornerShape(50.dp),color=Blue700.copy(alpha=0.1f),modifier=Modifier.size(40.dp)){Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Icon(Icons.Default.Gavel,null,tint=Blue700,modifier=Modifier.size(20.dp))}}
                        Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(v.plateNumber,fontWeight=FontWeight.Bold,fontSize=14.sp);Text("审车: ${fmtDate(v.annualInspectionDate)} · 剩${monthsUntil(v.annualInspectionDate)}个月",fontSize=12.sp,color=Color(0xFF5f6368))}}}}}
            }
            if(overdue.isEmpty()&&near.isEmpty()&&insp.isEmpty()){item{Box(Modifier.fillMaxWidth().padding(48.dp),contentAlignment=Alignment.Center){Text("✅ 一切正常",color=Color(0xFF34a853),fontSize=16.sp)}}}
            item{Spacer(Modifier.height(16.dp))}
        }
    }
}

@Composable
fun RemCard(v: Vehicle, level: String) {
    val bg = if(level=="urgent") ErrorRed.copy(alpha=0.05f) else Orange50
    val bd = if(level=="urgent") ErrorRed.copy(alpha=0.3f) else Orange700.copy(alpha=0.3f)
    Card(modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(8.dp),colors=CardDefaults.cardColors(containerColor=bg),border=BorderStroke(1.dp,bd)){
        Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically){
            Surface(shape=RoundedCornerShape(50.dp),color=if(level=="urgent")ErrorRed.copy(alpha=0.1f) else Orange700.copy(alpha=0.1f),modifier=Modifier.size(40.dp)){Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Icon(if(level=="urgent")Icons.Default.Error else Icons.Default.Warning,null,tint=if(level=="urgent")ErrorRed else Orange700,modifier=Modifier.size(20.dp))}}
            Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(v.plateNumber,fontWeight=FontWeight.Bold,fontSize=14.sp);Text("${v.brand} · ${v.assignedUser}",fontSize=12.sp,color=Color(0xFF5f6368));Text(if(isOverdue(v))"保养已逾期！需立即保养" else "还剩 ${remainingKm(v)} km 需保养",fontSize=12.sp,color=if(isOverdue(v))ErrorRed else Color(0xFF5f6368))}
            if(isOverdue(v)){Surface(shape=RoundedCornerShape(8.dp),color=ErrorRed.copy(alpha=0.1f)){Text("立即保养",Modifier.padding(horizontal=8.dp,vertical=4.dp),fontSize=11.sp,color=ErrorRed,fontWeight=FontWeight.Bold)}}
        }
    }
}

// helpers: parse notes field for rule/next info
private fun parseNext(notes: String): Int {
    val next = Regex("下次:([^ ]*)").find(notes)?.groupValues?.get(1) ?: ""
    return Regex("([\\d,]+)").find(next)?.value?.replace(",","")?.toIntOrNull() ?: 0
}
private fun parseRuleKm(notes: String): Int {
    val rule = Regex("规则:([^ ]*)").find(notes)?.groupValues?.get(1) ?: ""
    val m = Regex("(\\d+)\\s*千公里").find(rule) ?: Regex("(\\d+)公里").find(rule)
    return m?.groupValues?.get(1)?.toIntOrNull()?.times(1000) ?: 3000
}
private fun remainingKm(v: Vehicle): Int {
    val cur = v.lastMaintenanceKm
    val nxt = parseNext(v.notes)
    return if(cur > 0 && nxt > 0) nxt - cur else 0
}
private fun isOverdue(v: Vehicle): Boolean {
    val rem = remainingKm(v)
    return v.lastMaintenanceKm > 0 && rem < 0
}
private fun monthsUntil(ts: Long): Int {
    if(ts == 0L) return 999
    val now = System.currentTimeMillis()
    return ((ts - now) / (30L * 24 * 3600 * 1000)).toInt()
}
private fun fmtDate(ts: Long): String {
    if(ts == 0L) return ""
    return SimpleDateFormat("yyyy年M月", Locale.CHINA).format(Date(ts))
}
