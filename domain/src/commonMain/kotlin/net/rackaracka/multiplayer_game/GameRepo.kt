package net.rackaracka.multiplayer_game

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlin.jvm.JvmInline

@JvmInline
value class MineID(val value: String)

interface GameRepo {
    val playerPosition: StateFlow<Point>
    val playerMines: StateFlow<Set<Pair<MineID, Point>>>
    val canReleaseMine: Flow<Boolean>
    val canDetonateMine: Flow<Boolean>
    val isGamePaused: Flow<Boolean>
    fun onMove(direction: Direction)
    fun onDeployMine()
    fun onDetonateMine(mineID: MineID): Boolean
    fun onPauseGame()
    fun onResumeGame()
}