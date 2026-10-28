package mg.jkmm.baiboly.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.delay
import mg.jkmm.baiboly.R

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val TABS = listOf(
    Tab("home", "Fandraisana", AppIcons.Home),
    Tab("bible", "Baiboly", AppIcons.Bible),
    Tab("search", "Karohy", AppIcons.Search),
    Tab("settings", "Safidy", AppIcons.Tune),
)

@Composable
fun AppRoot(vm: BibleViewModel) {
    var showSplash by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(SPLASH_MILLIS)
        showSplash = false
    }
    if (showSplash) {
        SplashScreen()
        return
    }

    val nav = rememberNavController()
    val route = nav.currentBackStackEntryAsState().value?.destination?.route
    val cs = MaterialTheme.colorScheme

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (TABS.any { it.route == route }) {
                NavigationBar(containerColor = cs.surface) {
                    TABS.forEach { tab ->
                        NavigationBarItem(
                            selected = route == tab.route,
                            onClick = {
                                nav.navigate(tab.route) {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = cs.primary,
                                selectedTextColor = cs.primary,
                                indicatorColor = cs.tertiary.copy(alpha = 0.3f),
                                unselectedIconColor = cs.onSurfaceVariant,
                                unselectedTextColor = cs.onSurfaceVariant,
                            ),
                        )
                    }
                }
            }
        },
    ) { inner ->
        UpdateDialog(vm)
        NavHost(nav, startDestination = "home", modifier = Modifier.padding(inner)) {
            composable("home") { HomeScreen(vm) { b, c, f, t -> nav.navigate("reader/$b/$c?from=$f&to=$t") } }
            composable("bible") { BibleScreen(vm) { b, c, f, t -> nav.navigate("reader/$b/$c?from=$f&to=$t") } }
            composable("search") { SearchScreen(vm) { b, c, f, t -> nav.navigate("reader/$b/$c?from=$f&to=$t") } }
            composable("settings") { SettingsScreen(vm) }
            composable(
                "reader/{book}/{chapter}?from={from}&to={to}",
                arguments = listOf(
                    navArgument("book") { type = NavType.IntType },
                    navArgument("chapter") { type = NavType.IntType },
                    navArgument("from") { type = NavType.IntType; defaultValue = 0 },
                    navArgument("to") { type = NavType.IntType; defaultValue = 0 },
                ),
            ) { entry ->
                ReaderScreen(
                    vm,
                    entry.arguments!!.getInt("book"),
                    entry.arguments!!.getInt("chapter"),
                    entry.arguments!!.getInt("from"),
                    entry.arguments!!.getInt("to"),
                    onBack = { nav.popBackStack() },
                )
            }
        }
    }
}
