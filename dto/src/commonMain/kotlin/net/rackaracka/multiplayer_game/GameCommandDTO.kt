package net.rackaracka.multiplayer_game

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class GameCommandDTO {
    @Serializable
    @SerialName("move")
    data class MoveDTO(val x: Int, val y: Int): GameCommandDTO()
}