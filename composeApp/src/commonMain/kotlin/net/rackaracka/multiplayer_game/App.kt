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
    LaunchedEffect(Unit) {
        val h = MediaPlayerControllerImpl()
        h.apply {
            HttpClient().let {
                val soundBytes =
                    it.get("https://raw.githubusercontent.com/rafaelreis-hotmart/Audio-Sample-files/master/sample.mp3")
                        .readBytes()
                prepare(soundBytes, object : MediaPlayerListener {
                    override fun onPrepared() {
                        launch {
                            start()
                        }
                    }

                    override fun onCompletion() {
                        println("Completed")
                    }

                    override fun onError(exception: Exception) {
                        println("Error $exception")
                    }
                })
                // För att LaunchedEffekten inte ska dö direkt efter att man ha börjat spela ett ljud
                delay(100000)

                // TODO: Mig imorgon -> Implementera iOS natively för att KorGe är trash.
            }
        }
    }
    MaterialTheme {
        val controller = rememberNavController()
        NavHost(controller, startDestination = Screen.Welcome.route) {
            composable(Screen.Welcome.route) {
                GameScreen()
            }
        }
    }
}