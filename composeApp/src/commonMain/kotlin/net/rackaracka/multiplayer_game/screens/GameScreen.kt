package net.rackaracka.multiplayer_game.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyDown
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import multiplayergame.composeapp.generated.resources.Res
import net.rackaracka.multiplayer_game.Board
import net.rackaracka.multiplayer_game.Dashboard
import net.rackaracka.multiplayer_game.Direction
import net.rackaracka.multiplayer_game.GameRepo
import net.rackaracka.multiplayer_game.MediaPlayerController
import net.rackaracka.multiplayer_game.MediaPlayerListener
import net.rackaracka.multiplayer_game.Point
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@OptIn(ExperimentalResourceApi::class)
class GameScreenModel : ViewModel(), KoinComponent {
    private val gameRepo by inject<GameRepo>()
    private val mediaPlayerController by inject<MediaPlayerController>()

    val playerPosition = gameRepo.playerPosition
    val playerMines = gameRepo.playerMines

    init {
        viewModelScope.launch {
            val bytes = Res.readBytes("files/ambience_music.mp3")
            mediaPlayerController.prepare(bytes, object : MediaPlayerListener {
                override fun onPrepared() {
                    viewModelScope.launch {
                        mediaPlayerController.start()
                    }
                }

                override fun onCompletion() {
                    viewModelScope.launch {
                        mediaPlayerController.start()
                    }
                }

                override fun onError(exception: Exception) {
                    println("MediaController error: $exception")
                }
            })
        }
    }

    fun onClickUp() {
        gameRepo.onMove(Direction.Up)
    }

    fun onClickDown() {
        gameRepo.onMove(Direction.Down)
    }

    fun onClickLeft() {
        gameRepo.onMove(Direction.Left)
    }

    fun onClickRight() {
        gameRepo.onMove(Direction.Right)
    }

    fun onDeployMine() {
        gameRepo.onDeployMine()
    }
}

@Composable
fun GameScreen(viewModel: GameScreenModel = viewModel { GameScreenModel() }) {
    val inputRequester = remember { FocusRequester() }

    val playerPosition by viewModel.playerPosition.collectAsState()
    val playerMines by viewModel.playerMines.collectAsState()

    LaunchedEffect(Unit) {
        inputRequester.requestFocus()
    }

    Spacer(
        Modifier.onKeyEvent { keyEvent ->
            val isUp = keyEvent.key == Key.W || keyEvent.key == Key.DirectionUp
            val isDown = keyEvent.key == Key.S || keyEvent.key == Key.DirectionDown
            val isLeft = keyEvent.key == Key.A || keyEvent.key == Key.DirectionLeft
            val isRight = keyEvent.key == Key.D || keyEvent.key == Key.DirectionRight

            val isDeployMine = keyEvent.key == Key.One

            if (isUp && keyEvent.type == KeyDown) {
                viewModel.onClickUp()
            }
            if (isDown && keyEvent.type == KeyDown) {
                viewModel.onClickDown()
            }
            if (isLeft && keyEvent.type == KeyDown) {
                viewModel.onClickLeft()
            }
            if (isRight && keyEvent.type == KeyDown) {
                viewModel.onClickRight()
            }
            if (isDeployMine && keyEvent.type == KeyDown) {
                viewModel.onDeployMine()
            }
            false
        }
            .focusRequester(inputRequester)
            .focusable())

    Row {
        Board(
            verticalTilesCount = 14, horizontalTilesCount = 14,
        ) {
            Submarine(Point(playerPosition.x, playerPosition.y))
            playerMines.forEach {
                Mine(it)
            }
        }
        Spacer(Modifier.width(20.dp))
        Dashboard()
    }
}