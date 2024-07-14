package net.rackaracka.multiplayer_game

enum class Direction {
    Up, Down, Left, Right
}

interface GameController {
    fun move(direction: Direction)
}