package net.rackaracka.multiplayer_game

import kotlinx.coroutines.flow.StateFlow

interface GameRepo {
    val playerPosition: StateFlow<PlayerPosition>
    val playerMines: StateFlow<Set<Point>>
    fun onMove(direction: Direction)
    fun onDeployMine()
}