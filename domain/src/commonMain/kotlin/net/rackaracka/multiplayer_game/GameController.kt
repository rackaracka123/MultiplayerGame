package net.rackaracka.multiplayer_game

enum class Direction {
    Up, Down, Left, Right, UpLeft, UpRight, DownLeft, DownRight
}

interface GameController {
    fun move(direction: Direction)
}