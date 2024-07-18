package net.rackaracka.multiplayer_game

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.random.Random

class GameRepoImpl : GameRepo {

    private val _playerPosition = MutableStateFlow(Point(5, 5))
    override val playerPosition = _playerPosition.asStateFlow()

    private val _playerMines = MutableStateFlow(setOf<Pair<MineID, Point>>())
    override val playerMines = _playerMines.asStateFlow()
    override val canReleaseMine = playerMines.map { it.size < 4 }
    override val canDetonateMine = playerMines.map { it.isNotEmpty() }

    private val _isGamePaused = MutableStateFlow(false)
    override val isGamePaused = _isGamePaused.asStateFlow()


    override fun onMove(direction: Direction) {
        if (_isGamePaused.value) return
        val (dx, dy) = when (direction) {
            Direction.Up -> 0 to -1
            Direction.Down -> 0 to 1
            Direction.Left -> -1 to 0
            Direction.Right -> 1 to 0
        }
        val newPos = Point(
            _playerPosition.value.x + dx,
            _playerPosition.value.y + dy
        )

        if (_playerMines.value.any { it.second == newPos }) {
            return
        }
        _playerPosition.value = newPos
    }

    override fun onDeployMine() {
        if (_isGamePaused.value) return
        _playerMines.value += MineID(Random.nextDouble().toString()) to Point(
            _playerPosition.value.x,
            _playerPosition.value.y
        )
    }

    override fun onDetonateMine(mineID: MineID): Boolean {
        if (!_isGamePaused.value) return false
        val newPlayerMines =
            _playerMines.value.toMutableSet().apply { removeAll { it.first == mineID } }.toSet()
        return if (newPlayerMines.size != _playerMines.value.size) {
            _playerMines.value = newPlayerMines
            true
        } else {
            false
        }
    }

    override fun onPauseGame() {
        _isGamePaused.value = true
    }

    override fun onResumeGame() {
        _isGamePaused.value = false
    }
}