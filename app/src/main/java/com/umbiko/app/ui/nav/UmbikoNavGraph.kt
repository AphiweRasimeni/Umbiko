package com.umbiko.app.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.umbiko.app.ui.screens.auth.LoginScreen
import com.umbiko.app.ui.screens.auth.RegisterScreen
import com.umbiko.app.ui.screens.create.CreatePostScreen
import com.umbiko.app.ui.screens.home.HomeScreen
import com.umbiko.app.ui.screens.notifications.NotificationsScreen
import com.umbiko.app.ui.screens.post.PostDetailScreen
import com.umbiko.app.ui.screens.settings.SettingsScreen

@Composable
fun UmbikoNavGraph(
    startDestination: String = Routes.HOME,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoggedIn = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                },
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegistered = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToCreatePost = {
                    navController.navigate(Routes.CREATE_POST)
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                },
                onNavigateToNotifications = {
                    navController.navigate(Routes.NOTIFICATIONS)
                },
                onNavigateToPostDetail = { postId ->
                    navController.navigate(Routes.postDetail(postId))
                },
            )
        }
        composable(Routes.CREATE_POST) {
            CreatePostScreen(
                onPublished = {
                    navController.popBackStack()
                },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onLoggedOut = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = "post/{postId}",
            arguments = listOf(navArgument("postId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            PostDetailScreen(
                postId = postId,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
