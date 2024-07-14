package net.rackaracka.multiplayer_game

import kotlinx.serialization.Serializable

@Serializable
data class PlayerPositionDTO(val x: Int, val y: Int)

fun List<PlayerPositionDTO>.mapToPlayer() = map { PlayerPosition(x = it.x, y = it.y) }
