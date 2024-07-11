package net.rackaracka.multiplayer_game

data class Player(val x: Int, val y: Int)

interface PlayerRepo {
    suspend fun playerSession(onPlayerChanged: (Player) -> Unit)
}