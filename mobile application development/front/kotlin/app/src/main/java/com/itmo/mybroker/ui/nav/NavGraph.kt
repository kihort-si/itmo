package com.itmo.mybroker.ui.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.itmo.mybroker.data.TradeSide
import com.itmo.mybroker.ui.components.BottomNavBar
import com.itmo.mybroker.ui.components.HomeTab
import com.itmo.mybroker.ui.components.TweaksPanel
import com.itmo.mybroker.ui.screens.AuthScreen
import com.itmo.mybroker.ui.screens.ExchangeScreen
import com.itmo.mybroker.ui.screens.HistoryScreen
import com.itmo.mybroker.ui.screens.PortfolioScreen
import com.itmo.mybroker.ui.screens.ProfileScreen
import com.itmo.mybroker.ui.screens.StockScreen
import com.itmo.mybroker.ui.screens.TradeScreen
import com.itmo.mybroker.ui.theme.LocalPalette
import com.itmo.mybroker.core.SessionStore
import kotlinx.coroutines.launch

@Composable
fun MyBrokerNavHost() {
    val palette = LocalPalette.current
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val jwt by SessionStore.jwt.collectAsState(initial = null)
    val isAuthed = jwt != null
    var tweaksOpen by remember { mutableStateOf(false) }
    var favorites by remember { mutableStateOf(setOf("SBER", "AAPL", "GAZP", "NVDA")) }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val homeTab = HomeTab.fromRoute(currentRoute) ?: HomeTab.Exchange
    val showBottomBar = currentRoute in setOf("exchange", "portfolio", "profile")

    Column(modifier = Modifier.fillMaxSize().background(palette.bg)) {
        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
            NavHost(navController = navController, startDestination = "exchange") {
                composable("exchange") {
                    ExchangeScreen(
                        favorites = favorites,
                        onToggleFavorite = { sym ->
                            favorites = if (favorites.contains(sym)) favorites - sym else favorites + sym
                        },
                        onOpenStock = { sym -> navController.navigate("stock/$sym") },
                        onOpenTweaks = { tweaksOpen = true },
                    )
                }
                composable("portfolio") {
                    PortfolioScreen(
                        isAuthed = isAuthed,
                        onOpenStock = { sym -> navController.navigate("stock/$sym") },
                        onOpenHistory = { navController.navigate("history") },
                        onSignIn = { navController.navigate("auth") },
                        onOpenTweaks = { tweaksOpen = true },
                    )
                }
                composable("profile") {
                    ProfileScreen(
                        isAuthed = isAuthed,
                        onSignIn = { navController.navigate("auth") },
                        onSignOut = {
                            scope.launch { SessionStore.logout() }
                        },
                        onOpenTweaks = { tweaksOpen = true },
                    )
                }
                composable(
                    route = "stock/{sym}",
                    arguments = listOf(navArgument("sym") { type = NavType.StringType }),
                ) { backStack ->
                    val sym = backStack.arguments?.getString("sym") ?: "SBER"
                    StockScreen(
                        sym = sym,
                        onBack = { navController.popBackStack() },
                        onTrade = { s, side -> navController.navigate("trade/$s/${side.name}") },
                        favorites = favorites,
                        onToggleFavorite = { ss ->
                            favorites = if (favorites.contains(ss)) favorites - ss else favorites + ss
                        },
                    )
                }
                composable(
                    route = "trade/{sym}/{side}",
                    arguments = listOf(
                        navArgument("sym") { type = NavType.StringType },
                        navArgument("side") { type = NavType.StringType },
                    ),
                ) { backStack ->
                    val sym = backStack.arguments?.getString("sym") ?: "SBER"
                    val side = TradeSide.values().firstOrNull { it.name == backStack.arguments?.getString("side") } ?: TradeSide.Buy
                    TradeScreen(
                        sym = sym,
                        initialSide = side,
                        isAuthed = isAuthed,
                        onBack = { navController.popBackStack() },
                        onClose = {
                            navController.popBackStack(route = "exchange", inclusive = false)
                        },
                        onPlaced = { navController.popBackStack() },
                        onSignIn = { navController.navigate("auth") },
                    )
                }
                composable("history") {
                    HistoryScreen(onBack = { navController.popBackStack() })
                }
                composable("auth") {
                    AuthScreen(
                        onClose = { navController.popBackStack() },
                        onAuthed = { navController.popBackStack() },
                    )
                }
            }
        }
        if (showBottomBar) {
            BottomNavBar(
                current = homeTab,
                onSelect = { tab ->
                    navController.navigate(tab.route) {
                        launchSingleTop = true
                        popUpTo("exchange") { inclusive = false }
                    }
                },
            )
        }
    }

    if (tweaksOpen) {
        TweaksPanel(onDismiss = { tweaksOpen = false })
    }
}
