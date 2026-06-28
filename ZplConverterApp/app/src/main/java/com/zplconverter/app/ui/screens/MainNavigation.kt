package com.zplconverter.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNavigateToConverter = { navController.navigate("converter") },
                onNavigateToDeclaration = { navController.navigate("declaration") }
            )
        }
        composable("converter") {
            ZplConverterScreen(onBack = { navController.popBackStack() })
        }
        composable("declaration") {
            DeclarationScreen(onBack = { navController.popBackStack() })
        }
    }
}
