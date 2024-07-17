package net.rackaracka.multiplayer_game

import org.koin.core.KoinApplication
import org.koin.dsl.module

fun KoinApplication.registerModules() {
    modules(repositories, usecases)
}

private val repositories = module {
    single<GameRepo> { GameRepoImpl() }
    single<MediaPlayerController> { MediaPlayerControllerImpl() }
}

private val usecases = module {

}
