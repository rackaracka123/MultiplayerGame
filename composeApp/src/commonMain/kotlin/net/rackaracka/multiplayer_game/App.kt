package net.rackaracka.multiplayer_game

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import net.rackaracka.multiplayer_game.screens.WelcomeScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

sealed class Screen(val route: String) {
    data object Welcome : Screen("welcome")
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        val controller = rememberNavController()
        NavHost(controller, startDestination = Screen.Welcome.route) {
            composable(Screen.Welcome.route) {
                WelcomeScreen()
            }
        }
    }
}