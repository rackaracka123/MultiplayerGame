package net.rackaracka.multiplayer_game

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameService {
    private val players = mutableListOf<PlayerDTO>()

    private val _playersFlow = MutableStateFlow<List<PlayerDTO>>(emptyList())
    val playersFlow = _playersFlow.asStateFlow()

    fun connectPlayer(): PlayerDTO {
        val player = PlayerDTO((Random.nextInt() % 250) + 250, (Random.nextInt() % 250) + 250)
        players += player
        _playersFlow.value = players
        return player
    }

    fun disconnectPlayer(player: PlayerDTO) {
        players -= player
        _playersFlow.value = players
    }

    fun move(player: PlayerDTO, x: Int, y: Int) {
        val index = _playersFlow.value.indexOf(player)
        if (index != -1) {
            _playersFlow.value =
                _playersFlow.value.let {
                    it.toMutableList().apply { this[index] = PlayerDTO(x, y) }
                }.toList()
        }
    }
}