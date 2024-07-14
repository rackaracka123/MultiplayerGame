package net.rackaracka.multiplayer_game.screens

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import net.rackaracka.multiplayer_game.Direction
import net.rackaracka.multiplayer_game.GameRepo
import net.rackaracka.multiplayer_game.GameEventListener
import net.rackaracka.multiplayer_game.PlayerPosition
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WelcomeScreenViewModel : ViewModel(), KoinComponent {
    private val gameRepo by inject<GameRepo>()

    private val _playersFlow = MutableStateFlow<List<PlayerPosition>>(emptyList())
    val playersFlow = _playersFlow.asStateFlow()

    private val movementFlow = MutableSharedFlow<Direction>()

    init {
        viewModelScope.launch {
            gameRepo.session(gameScope = {
                launch {
                    movementFlow.collect {
                        move(it)
                    }
                }
            }, gameEventListener = object : GameEventListener {
                override fun onPositionsChanged(playerPositions: List<PlayerPosition>) {
                    _playersFlow.value = playerPositions
                }
            })
        }
    }

    fun onPlayerMove(direction: Direction) {
        viewModelScope.launch {
            movementFlow.emit(direction)
        }
    }
}

@Composable
fun WelcomeScreen(viewModel: WelcomeScreenViewModel = viewModel { WelcomeScreenViewModel() }) {
    val players by viewModel.playersFlow.collectAsState()

    val requester = remember { FocusRequester() }
    Spacer(Modifier.onKeyEvent { keyEvent ->
        keyEvent.key.toMoveDirectionOrNull()?.let { direction ->
            println("Movement: $direction")
            viewModel.onPlayerMove(direction)
        }
        true
    }
        .focusRequester(requester)
        .focusable())

    LaunchedEffect(Unit) {
        requester.requestFocus()
    }
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

private fun Key.toMoveDirectionOrNull(): Direction? = when (this) {
    Key.DirectionUp, Key.W -> Direction.Up
    Key.DirectionDown, Key.S -> Direction.Down
    Key.DirectionLeft, Key.A -> Direction.Left
    Key.DirectionRight, Key.D -> Direction.Right
    else -> null
}