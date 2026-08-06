package com.vehiclemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vehiclemanager.backup.BackupScreen
import com.vehiclemanager.data.SeedData
import com.vehiclemanager.ui.home.HomeScreen
import com.vehiclemanager.ui.maintenance.MaintenanceScreen
import com.vehiclemanager.ui.parts.PartsScreen
import com.vehiclemanager.ui.theme.VehicleManagerTheme
import com.vehiclemanager.ui.todo.TodoScreen
import com.vehiclemanager.ui.vehicle.VehicleScreen
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = applicationContext as VehicleApp
        CoroutineScope(Dispatchers.IO).launch {
            SeedData.seedAll(app.database)
            if (app.database.partDao().getAllList().isEmpty()) {
                app.database.partDao().insertAll(SeedData.getParts())
            }
        }
        enableEdgeToEdge()
        setContent {
            VehicleManagerTheme {
                VehicleManagerApp()
            }
        }
    }
}

sealed class Screen(val route: String, val label: String) {
    object Home : Screen("home", "首页")
    object Vehicles : Screen("vehicles", "车辆")
    object Maintenance : Screen("maintenance", "维保")
    object Todos : Screen("todos", "提醒")
    object Parts : Screen("parts", "配件")
    object Backup : Screen("backup", "设置")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleManagerApp() {
    val nav = rememberNavController()
    val tabs = listOf(Screen.Home, Screen.Vehicles, Screen.Maintenance, Screen.Todos, Screen.Parts, Screen.Backup)
    val icons = listOf(Icons.Default.Home, Icons.Default.DirectionsCar, Icons.Default.Build, Icons.Default.Notifications, Icons.Default.ShoppingCart, Icons.Default.Settings)

    Scaffold(bottomBar = {
        NavigationBar {
            val route = nav.currentBackStackEntryAsState().value?.destination?.route
            tabs.forEach { tab ->
                NavigationBarItem(
                    icon = { Icon(icons[tabs.indexOf(tab)], tab.label) },
                    label = { Text(tab.label, fontWeight = FontWeight.Medium) },
                    selected = route == tab.route,
                    onClick = { nav.navigate(tab.route) { popUpTo(Screen.Home.route){saveState=true}; launchSingleTop=true; restoreState=true } }
                )
            }
        }
    }) { p ->
        NavHost(nav, Screen.Home.route, Modifier.padding(p)) {
            composable(Screen.Home.route) { HomeScreen(onNavigateToVehicles={nav.navigate(Screen.Vehicles.route)}, onNavigateToMaintenance={nav.navigate(Screen.Maintenance.route)}, onNavigateToKmRecord={nav.navigate(Screen.Parts.route)}, onNavigateToTodos={nav.navigate(Screen.Todos.route)}, onNavigateToBackup={nav.navigate(Screen.Backup.route)}) }
            composable(Screen.Vehicles.route) { VehicleScreen(onBack={nav.popBackStack()}) }
            composable(Screen.Maintenance.route) { MaintenanceScreen(vehicleId=null, onBack={nav.popBackStack()}) }
            composable(Screen.Todos.route) { TodoScreen(onBack={nav.popBackStack()}) }
            composable(Screen.Parts.route) { PartsScreen(onBack={nav.popBackStack()}) }
            composable(Screen.Backup.route) { BackupScreen(onBack={nav.popBackStack()}) }
        }
    }
}
