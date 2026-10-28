package com.daymark.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.daymark.app.ui.calendar.CalendarScreen
import com.daymark.app.ui.calendar.CalendarViewModel
import com.daymark.app.ui.stats.StatsScreen
import com.daymark.app.ui.stats.StatsViewModel
import com.daymark.app.ui.theme.DayMarkTheme
import com.daymark.app.ui.theme.DarkBg
import kotlinx.coroutines.delay

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("calendar", "Calendar", Icons.Default.CalendarMonth),
    Tab("stats", "Stats", Icons.Default.BarChart),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        val app = application as DayMarkApplication
        setContent {
            DayMarkTheme {
                var splashDone by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(1_400)
                    splashDone = true
                }
                Crossfade(targetState = splashDone, label = "splash") { done ->
                    if (done) MainScaffold(app) else SplashScreen()
                }
            }
        }
    }
}

@Composable
private fun SplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(DarkBg),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.splash_logo),
            contentDescription = "DayMark",
            modifier = Modifier.fillMaxWidth(0.62f),
            contentScale = ContentScale.FillWidth,
        )
    }
}

@Composable
fun MainScaffold(app: DayMarkApplication) {
    val navController = rememberNavController()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "calendar",
            modifier = Modifier.padding(padding),
        ) {
            composable("calendar") {
                val vm: CalendarViewModel = viewModel(factory = viewModelFactory(app))
                CalendarScreen(viewModel = vm)
            }
            composable("stats") {
                val vm: StatsViewModel = viewModel(factory = viewModelFactory(app))
                StatsScreen(viewModel = vm)
            }
        }
    }
}

private fun viewModelFactory(app: DayMarkApplication) = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when (modelClass) {
        CalendarViewModel::class.java -> CalendarViewModel(app.repository)
        StatsViewModel::class.java -> StatsViewModel(app.repository)
        else -> throw IllegalArgumentException("Unknown ViewModel: $modelClass")
    } as T
}
