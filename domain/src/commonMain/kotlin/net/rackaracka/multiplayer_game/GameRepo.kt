package net.rackaracka.multiplayer_game

import kotlinx.coroutines.flow.StateFlow

interface GameRepo {
    val playerPosition: StateFlow<PlayerPosition>
    fun onMove(direction: Direction)
}