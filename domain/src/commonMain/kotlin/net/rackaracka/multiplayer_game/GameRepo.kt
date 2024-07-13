package net.rackaracka.multiplayer_game

data class Player(val x: Int, val y: Int)

interface GameRepo {
    suspend fun gameSession(onGameUpdate: (List<Player>) -> Unit)
}