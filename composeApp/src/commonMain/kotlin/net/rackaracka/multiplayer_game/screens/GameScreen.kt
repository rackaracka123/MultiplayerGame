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
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyDown
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.rackaracka.multiplayer_game.Direction
import net.rackaracka.multiplayer_game.GameRepo
import net.rackaracka.multiplayer_game.GameEventListener
import net.rackaracka.multiplayer_game.PlayerPosition
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GameScreenModel : ViewModel(), KoinComponent {
    private val gameRepo by inject<GameRepo>()

    private val _playersFlow = MutableStateFlow<List<PlayerPosition>>(emptyList())
    val playersFlow = _playersFlow.asStateFlow()

    private val isUpPressed = MutableStateFlow(false)
    private val isDownPressed = MutableStateFlow(false)
    private val isLeftPressed = MutableStateFlow(false)
    private val isRightPressed = MutableStateFlow(false)

    private val stepsTicker = flow {
        while (viewModelScope.isActive) {
            emit(Unit)
            delay(100)
        }
    }

    private val directionSteps = combine(
        isUpPressed,
        isDownPressed,
        isLeftPressed,
        isRightPressed,
        stepsTicker
    ) { isUp, isDown, isLeft, isRight, _ ->
        if (isUp && isLeft) {
            Direction.UpLeft
        } else if (isUp && isRight) {
            Direction.UpRight
        } else if (isUp) {
            Direction.Up
        } else if (isDown && isLeft) {
            Direction.DownLeft
        } else if (isDown && isRight) {
            Direction.DownRight
        } else if (isDown) {
            Direction.Down
        } else if (isLeft) {
            Direction.Left
        } else if (isRight) {
            Direction.Right
        } else null
    }

    init {
        viewModelScope.launch {
            gameRepo.session(gameScope = {
                launch {
                    directionSteps.collect { movement ->
                        movement?.let {
                            move(it)
                        }
                    }
                }
            }, gameEventListener = object : GameEventListener {
                override fun onPositionsChanged(playerPositions: List<PlayerPosition>) {
                    _playersFlow.value = playerPositions
                }
            })
        }
    }

    fun onPressUpChanged(isPressed: Boolean) {
        if (isUpPressed.value != isPressed) {
            isUpPressed.value = isPressed
        }
    }

    fun onPressDownChanged(isPressed: Boolean) {
        if (isDownPressed.value != isPressed) {
            isDownPressed.value = isPressed
        }
    }

    fun onPressLeftChanged(isPressed: Boolean) {
        if (isLeftPressed.value != isPressed) {
            isLeftPressed.value = isPressed
        }
    }

    fun onPressRightChanged(isPressed: Boolean) {
        if (isRightPressed.value != isPressed) {
            isRightPressed.value = isPressed
        }
    }
}

@Composable
fun GameScreen(viewModel: GameScreenModel = viewModel { GameScreenModel() }) {
    val players by viewModel.playersFlow.collectAsState()

    val requester = remember { FocusRequester() }
    Spacer(
        Modifier.onKeyEvent { keyEvent ->
            val isUp = keyEvent.key == Key.W || keyEvent.key == Key.DirectionUp
            val isDown = keyEvent.key == Key.S || keyEvent.key == Key.DirectionDown
            val isLeft = keyEvent.key == Key.A || keyEvent.key == Key.DirectionLeft
            val isRight = keyEvent.key == Key.D || keyEvent.key == Key.DirectionRight

            if (isUp) {
                println("Up ${keyEvent.type == KeyDown}")
                viewModel.onPressUpChanged(keyEvent.type == KeyDown)
            }
            if (isDown) {
                println("Down ${keyEvent.type == KeyDown}")
                viewModel.onPressDownChanged(keyEvent.type == KeyDown)
            }
            if (isLeft) {
                println("Left ${keyEvent.type == KeyDown}")
                viewModel.onPressLeftChanged(keyEvent.type == KeyDown)
            }
            if (isRight) {
                println("Right ${keyEvent.type == KeyDown}")
                viewModel.onPressRightChanged(keyEvent.type == KeyDown)
            }
            true
        }
            .focusRequester(requester)
            .focusable())

    LaunchedEffect(Unit) {
        requester.requestFocus()
    }
    players.forEach { player ->
        Box(
            Modifier
                .size(100.dp)
                .offset { IntOffset(player.x, player.y) }
                .background(Color.Black)
        )
    }
}
