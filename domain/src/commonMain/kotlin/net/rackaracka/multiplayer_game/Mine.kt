package net.rackaracka.multiplayer_game

import kotlin.jvm.JvmInline

@JvmInline
value class MineID(val value: String)

data class Mine(val id: MineID, val position: Point)