import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import net.rackaracka.multiplayer_game.App
import net.rackaracka.multiplayer_game.registerModules
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    startKoin {
        registerModules()
    }
    ComposeViewport(document.body!!) {
        App()
    }
}