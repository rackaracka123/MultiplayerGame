package net.rackaracka.multiplayer_game

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class DirectionDTO {
    Up, Down, Left, Right, UpLeft, UpRight, DownLeft, DownRight
}

@Serializable
sealed class GameCommandDTO {
    @Serializable
    @SerialName("move")
    data class MoveDTO(val direction: DirectionDTO) : GameCommandDTO()
}

fun Direction.toMoveCommand(): GameCommandDTO = when (this) {
    Direction.Up -> GameCommandDTO.MoveDTO(DirectionDTO.Up)
    Direction.Down -> GameCommandDTO.MoveDTO(DirectionDTO.Down)
    Direction.Left -> GameCommandDTO.MoveDTO(DirectionDTO.Left)
    Direction.Right -> GameCommandDTO.MoveDTO(DirectionDTO.Right)
    Direction.UpLeft -> GameCommandDTO.MoveDTO(DirectionDTO.UpLeft)
    Direction.UpRight -> GameCommandDTO.MoveDTO(DirectionDTO.UpRight)
    Direction.DownLeft -> GameCommandDTO.MoveDTO(DirectionDTO.DownLeft)
    Direction.DownRight -> GameCommandDTO.MoveDTO(DirectionDTO.DownRight)
}