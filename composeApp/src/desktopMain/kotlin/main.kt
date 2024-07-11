import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import net.rackaracka.multiplayer_game.App
import net.rackaracka.multiplayer_game.registerModules
import org.koin.core.context.startKoin

fun main() = application {
    startKoin {
        registerModules()
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = "MultiplayerGame",
    ) {
        App()
    }
}