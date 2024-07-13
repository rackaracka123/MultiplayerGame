package net.rackaracka.multiplayer_game

interface GameCommand {
    fun move(x: Int, y: Int)
}