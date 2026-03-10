package com.ravenguard.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ravenguard.app.ui.home.HomeScreen
import com.ravenguard.app.ui.settings.SettingsScreen

object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
}

@Composable
fun NavGraph(permissionGranted: Boolean, navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(permissionGranted = permissionGranted, onOpenSettings = {
                navController.navigate(Routes.SETTINGS)
            })
        }
        composable(
            Routes.SETTINGS,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            }
        ) {
            SettingsScreen()
        }
    }
}
