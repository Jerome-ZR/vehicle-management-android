package com.vehiclemanager.ui.parts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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
import com.vehiclemanager.data.entity.Part
import com.vehiclemanager.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current; val app = ctx.applicationContext as VehicleApp; val sc = rememberCoroutineScope()
    var parts by remember { mutableStateOf<List<Part>>(emptyList()) }
    var shops by remember { mutableStateOf<List<String>>(emptyList()) }
    var selShop by remember { mutableStateOf("铁马维修") }
    LaunchedEffect(Unit) { sc.launch { parts = app.partDao.getAllList(); shops = app.partDao.getShops() } }
    val cur = parts.filter { it.shop == selShop }
    val other = shops.firstOrNull { it != selShop } ?: "洪亮价格"
    val oth = parts.filter { it.shop == other }

    Scaffold(
        topBar = { TopAppBar(title = { Text("配件价格表", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Blue700, titleContentColor = androidx.compose.ui.graphics.Color.White, navigationIconContentColor = androidx.compose.ui.graphics.Color.White)) }
    ) { p ->
        LazyColumn(Modifier.fillMaxSize().padding(p).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { shops.forEach { s -> FilterChip(s == selShop, { selShop = s }, label = { Text(s, fontSize = 12.sp) }) } } }
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Blue50)) {
                    Row(Modifier.padding(10.dp)) { Text("配件", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 13.sp); Text("数量", Modifier.width(40.dp), fontSize = 12.sp, color = androidx.compose.ui.graphics.Color(0xFF5f6368)); Text("单价", Modifier.width(60.dp), fontSize = 12.sp, color = androidx.compose.ui.graphics.Color(0xFF5f6368)); Text("金额", Modifier.width(60.dp), fontSize = 12.sp, color = androidx.compose.ui.graphics.Color(0xFF5f6368)) }
                }
            }
            items(cur) { p2 ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(6.dp), elevation = CardDefaults.cardElevation(1.dp)) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) { Text(p2.partName, Modifier.weight(1f), fontSize = 13.sp); Text("${p2.qty}", Modifier.width(40.dp), fontSize = 12.sp); Text("¥${p2.unitPrice}", Modifier.width(60.dp), fontSize = 12.sp, color = androidx.compose.ui.graphics.Color(0xFF5f6368)); Text("¥${p2.amount}", Modifier.width(60.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ErrorRed) }
                }
            }
            item { Text("对比 $other", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp), color = Orange700) }
            items(oth) { p2 ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(6.dp), colors = CardDefaults.cardColors(containerColor = Orange50)) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) { Text(p2.partName, Modifier.weight(1f), fontSize = 13.sp); Text("${p2.qty}", Modifier.width(40.dp), fontSize = 12.sp); Text("¥${p2.unitPrice}", Modifier.width(60.dp), fontSize = 12.sp, color = androidx.compose.ui.graphics.Color(0xFF5f6368)); Text("¥${p2.amount}", Modifier.width(60.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Orange700) }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}
