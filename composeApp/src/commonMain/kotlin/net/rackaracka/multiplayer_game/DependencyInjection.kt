package net.rackaracka.multiplayer_game

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.core.KoinApplication
import org.koin.dsl.module

fun KoinApplication.registerModules() {
    modules(repositories, usecases)
}

private val repositories = module {
    single<GameRepo> { GameRepoImpl(CoroutineScope(Dispatchers.Main)) }
    single<MediaPlayerController> { MediaPlayerControllerImpl() }
}

private val usecases = module {

}
