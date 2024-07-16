package net.rackaracka.multiplayer_game.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyDown
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import multiplayergame.composeapp.generated.resources.Res
import multiplayergame.composeapp.generated.resources.submarine
import net.rackaracka.multiplayer_game.Board
import net.rackaracka.multiplayer_game.Direction
import net.rackaracka.multiplayer_game.GameRepo
import net.rackaracka.multiplayer_game.Point
import org.jetbrains.compose.resources.painterResource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GameScreenModel : ViewModel(), KoinComponent {
    private val gameRepo by inject<GameRepo>()

    val playerPosition = gameRepo.playerPosition

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
}

@Composable
fun GameScreen(viewModel: GameScreenModel = viewModel { GameScreenModel() }) {
    val inputRequester = remember { FocusRequester() }

    val playerPosition by viewModel.playerPosition.collectAsState()

    LaunchedEffect(Unit) {
        inputRequester.requestFocus()
    }
    Spacer(
        Modifier.onKeyEvent { keyEvent ->
            val isUp = keyEvent.key == Key.W || keyEvent.key == Key.DirectionUp
            val isDown = keyEvent.key == Key.S || keyEvent.key == Key.DirectionDown
            val isLeft = keyEvent.key == Key.A || keyEvent.key == Key.DirectionLeft
            val isRight = keyEvent.key == Key.D || keyEvent.key == Key.DirectionRight

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
            true
        }
            .focusRequester(inputRequester)
            .focusable())

    Board(
        verticalTilesCount = 14, horizontalTilesCount = 14,
        listOf(
            Point(playerPosition.x, playerPosition.y) to {
                Image(
                    painter = painterResource(Res.drawable.submarine),
                    contentDescription = null
                )
            },
        )
    )
}
