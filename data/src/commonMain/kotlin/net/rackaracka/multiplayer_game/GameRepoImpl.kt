package net.rackaracka.multiplayer_game

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameRepoImpl : GameRepo {

    private val _playerPosition = MutableStateFlow(PlayerPosition(5, 5))
    override val playerPosition = _playerPosition.asStateFlow()

    private val _playerMines = MutableStateFlow(setOf<Point>())
    override val playerMines = _playerMines.asStateFlow()


    override fun onMove(direction: Direction) {
        val (dx, dy) = when (direction) {
            Direction.Up -> 0 to -1
            Direction.Down -> 0 to 1
            Direction.Left -> -1 to 0
            Direction.Right -> 1 to 0
        }
        val newPos = PlayerPosition(
            _playerPosition.value.x + dx,
            _playerPosition.value.y + dy
        )
        
        if (_playerMines.value.contains(Point(newPos.x, newPos.y))) {
            return
        }
        _playerPosition.value = newPos
    }

    override fun onDeployMine() {
        _playerMines.value += Point(_playerPosition.value.x, _playerPosition.value.y)
    }
}