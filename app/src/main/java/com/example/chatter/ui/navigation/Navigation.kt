package com.example.chatter.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.chatter.ui.screens.ChatScreen
import com.example.chatter.ui.screens.HomeScreen
import com.example.chatter.ui.screens.LoginScreen
import com.example.chatter.ui.screens.SignupScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

sealed class Screens(val route: String) {
    object LoginScreen : Screens(route = "login_screen")
    object SignupScreen : Screens("signup_screen")
    object HomeScreen : Screens("home_screen")
    object ChatScreen : Screens("chat_screen/{channelId}/{channelName}") {
        fun createRoute(channelId: String, channelName: String) =
            "chat_screen/$channelId/$channelName"
    }

    object ProfileScreen : Screens("profile_screen")
}

@Composable
fun NavigationHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController,
        startDestination = if (Firebase.auth.currentUser == null) {
            Screens.LoginScreen.route
        } else {
            Screens.HomeScreen.route
        },
        modifier = modifier.fillMaxSize()
    ) {
        composable(
            route = Screens.LoginScreen.route
        ) {
            LoginScreen(
                onNavigateToHomeScreen = {
                    navController.navigate(route = Screens.HomeScreen.route) {
                        popUpTo(Screens.LoginScreen.route) {
                            inclusive = true
                        }
                    }
                },
                onNavigateToSignupScreen = {
                    navController.navigate(route = Screens.SignupScreen.route)
                }
            )
        }

        composable(
            route = Screens.SignupScreen.route
        ) {
            SignupScreen(
                onNavigateToHomeScreen = {
                    navController.navigate(route = Screens.HomeScreen.route) {
                        popUpTo(Screens.LoginScreen.route) {
                            inclusive = true
                        }
                    }
                },
                onNavigateToLoginScreen = {
                    navController.navigate(route = Screens.LoginScreen.route)

                }
            )
        }

        composable(
            route = Screens.HomeScreen.route
        ) {
            HomeScreen(
                onNavigateToChatScreen = { id, name ->
                    navController.navigate(route = Screens.ChatScreen.createRoute(id, name))
                }
            )
        }

        composable(
            route = Screens.ChatScreen.route, arguments = listOf(
                navArgument("channelId") {
                    type = NavType.StringType
                }, navArgument("channelName") {
                    type = NavType.StringType
                }
            )) {
            val channelId = it.arguments?.getString("channelId") ?: ""
            val channelName = it.arguments?.getString("channelName") ?: ""
            ChatScreen(channelId = channelId, channelName = channelName)
        }

    }
}

