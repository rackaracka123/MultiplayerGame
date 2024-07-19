package net.rackaracka.multiplayer_game

import kotlinx.coroutines.flow.Flow

interface GameRepo {
    val boardSize: Int
    val sectorSize: Int
    val player: Flow<Player>
    val canReleaseMine: Flow<Boolean>
    val canDetonateMine: Flow<Boolean>
    val canSonar: Flow<Boolean>
    val isGamePaused: Flow<Boolean>
    fun onMove(direction: Direction)
    fun onDeployMine()
    fun onDetonateMine(mineID: MineID): Boolean
    fun onPauseGame()
    fun onResumeGame()
    fun onClickSonar(index: Int): Sector?
}