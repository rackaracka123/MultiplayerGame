package net.rackaracka.multiplayer_game

import org.koin.core.KoinApplication
import org.koin.dsl.module

fun KoinApplication.registerModules() {
    modules(repositories, usecases)
}

private val repositories = module {
    single<Foo> { FooImpl() }
}

private val usecases = module {

}
