package net.rackaracka.multiplayer_game

import kotlin.jvm.JvmInline

@JvmInline
value class PlayerID(val int: Int)

data class Player(val id: PlayerID, val position: Point, val mines: Set<Mine>, val health: Int)