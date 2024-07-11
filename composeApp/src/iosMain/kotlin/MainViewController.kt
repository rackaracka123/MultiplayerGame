import androidx.compose.ui.window.ComposeUIViewController
import net.rackaracka.multiplayer_game.App
import net.rackaracka.multiplayer_game.registerModules
import org.koin.core.context.startKoin
import platform.UIKit.UIViewController
fun MainViewController(): UIViewController {
    startKoin {
        registerModules()
    }
    return ComposeUIViewController {
        App()
    }
}