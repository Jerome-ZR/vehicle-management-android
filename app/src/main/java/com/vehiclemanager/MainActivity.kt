package com.vehiclemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vehiclemanager.backup.BackupScreen
import com.vehiclemanager.data.SeedData
import com.vehiclemanager.ui.home.HomeScreen
import com.vehiclemanager.ui.kmrecord.KmRecordScreen
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

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Vehicles : Screen("vehicles")
    object Maintenance : Screen("maintenance?vehicleId={vehicleId}") {
        fun createRoute(vehicleId: Long? = null) =
            if (vehicleId != null) "maintenance?vehicleId=$vehicleId" else "maintenance"
    }
    object KmRecord : Screen("km_record")
    object Todos : Screen("todos")
    object Backup : Screen("backup")
    object Parts : Screen("parts")
}

@Composable
fun VehicleManagerApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToVehicles = { navController.navigate(Screen.Vehicles.route) },
                onNavigateToMaintenance = { navController.navigate(Screen.Maintenance.createRoute()) },
                onNavigateToKmRecord = { navController.navigate(Screen.KmRecord.route) },
                onNavigateToTodos = { navController.navigate(Screen.Todos.route) },
                onNavigateToBackup = { navController.navigate(Screen.Backup.route) }
            )
        }

        composable(Screen.Vehicles.route) {
            VehicleScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.Maintenance.route,
            arguments = listOf(
                navArgument("vehicleId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getLong("vehicleId") ?: -1L
            MaintenanceScreen(
                vehicleId = if (vehicleId > 0) vehicleId else null,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.KmRecord.route) {
            KmRecordScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Todos.route) {
            TodoScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Backup.route) {
            BackupScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Parts.route) {
            PartsScreen(onBack = { navController.popBackStack() })
        }
    }
}
