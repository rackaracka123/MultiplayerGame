package net.rackaracka.multiplayer_game

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readBytes
import io.ktor.utils.io.core.use
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.rackaracka.multiplayer_game.screens.GameScreen
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
                GameScreen()
            }
        }
    }
}