package net.rackaracka.multiplayer_game.screens

import androidx.compose.animation.core.animateDpAsState
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.rackaracka.multiplayer_game.Player
import net.rackaracka.multiplayer_game.PlayerRepo
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WelcomeScreenViewModel : ViewModel(), KoinComponent {
    private val playerRepo by inject<PlayerRepo>()

    private val _playerFlow = MutableStateFlow<Player?>(null)
    val playerFlow = _playerFlow.asStateFlow()

    init {
        viewModelScope.launch {
            playerRepo.playerSession {
                _playerFlow.value = it
            }
        }
    }
}

@Composable
fun WelcomeScreen(viewModel: WelcomeScreenViewModel = viewModel { WelcomeScreenViewModel() }) {
    val player by viewModel.playerFlow.collectAsState()

    val playerX by animateIntAsState(targetValue = player?.x ?: 0)
    val playerY by animateIntAsState(targetValue = player?.y ?: 0)
    player?.let {
        Box(
            Modifier
                .size(100.dp)
                .offset { IntOffset(playerX, playerY) }
                .background(Color.Black)
        )
    }
}