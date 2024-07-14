package net.rackaracka.multiplayer_game

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

private const val directionalMovement = 10

@JvmInline
value class PlayerID(val value: String)

class GameService {
    private val players = mutableListOf<Pair<PlayerID, PlayerPositionDTO>>()

    private val _playersFlow =
        MutableStateFlow<List<Pair<PlayerID, PlayerPositionDTO>>>(emptyList())
    val playersFlow = _playersFlow.asStateFlow()

    fun connectPlayer(playerID: PlayerID): PlayerPositionDTO {
        val player =
            PlayerPositionDTO((Random.nextInt() % 250) + 250, (Random.nextInt() % 250) + 250)
        players += playerID to player
        _playersFlow.value = players
        return player
    }

    fun disconnectPlayer(playerID: PlayerID) {
        players.removeIf { it.first == playerID }
        _playersFlow.value = players
    }

    fun move(player: PlayerID, direction: DirectionDTO) {
        val index = _playersFlow.value.indexOfFirst { it.first == player }
        if (index != -1) {
            _playersFlow.value =
                _playersFlow.value.let {
                    it.toMutableList()
                        .apply {
                            this[index] =
                                player to direction.toPlayerPosition(
                                    this[index].second.x,
                                    this[index].second.y
                                )
                        }
                }.toList()
        }
    }
}

private fun DirectionDTO.toPlayerPosition(x: Int, y: Int): PlayerPositionDTO {
    when (this) {
        DirectionDTO.Up -> return PlayerPositionDTO(x, y - directionalMovement)
        DirectionDTO.Down -> return PlayerPositionDTO(x, y + directionalMovement)
        DirectionDTO.Left -> return PlayerPositionDTO(x - directionalMovement, y)
        DirectionDTO.Right -> return PlayerPositionDTO(x + directionalMovement, y)
    }
}
