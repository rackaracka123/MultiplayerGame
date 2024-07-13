package net.rackaracka.multiplayer_game.screens

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.rackaracka.multiplayer_game.GameRepo
import net.rackaracka.multiplayer_game.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WelcomeScreenViewModel : ViewModel(), KoinComponent {
    private val gameRepo by inject<GameRepo>()

    private val _playersFlow = MutableStateFlow<List<Player>>(emptyList())
    val playersFlow = _playersFlow.asStateFlow()

    init {
        viewModelScope.launch {
            gameRepo.session(command = {
                delay(1000)
                move(800, 800)
            }, onPlayerUpdate = {
                _playersFlow.value = it
            })
        }
    }
}

@Composable
fun WelcomeScreen(viewModel: WelcomeScreenViewModel = viewModel { WelcomeScreenViewModel() }) {
    val players by viewModel.playersFlow.collectAsState()

    players.forEach { player ->
        val playerX by animateIntAsState(player.x)
        val playerY by animateIntAsState(player.y)
        Box(
            Modifier
                .size(100.dp)
                .offset { IntOffset(playerX, playerY) }
                .background(Color.Black)
        )
    }
}