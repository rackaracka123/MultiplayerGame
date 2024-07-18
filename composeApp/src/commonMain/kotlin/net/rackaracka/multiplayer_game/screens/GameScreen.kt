package net.rackaracka.multiplayer_game.screens

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import multiplayergame.composeapp.generated.resources.Res
import net.rackaracka.multiplayer_game.Board
import net.rackaracka.multiplayer_game.Dashboard
import net.rackaracka.multiplayer_game.DashboardItem
import net.rackaracka.multiplayer_game.Direction
import net.rackaracka.multiplayer_game.GameRepo
import net.rackaracka.multiplayer_game.MediaPlayerController
import net.rackaracka.multiplayer_game.MediaPlayerListener
import net.rackaracka.multiplayer_game.MineID
import net.rackaracka.multiplayer_game.PlayerPosition
import net.rackaracka.multiplayer_game.Point
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

sealed class GameState {
    data object Initial : GameState()
    data class Moving(
        val playerPosition: PlayerPosition,
        val playerMines: Set<Point>,
        val dashboardItems: List<DashboardItem>
    ) : GameState()

    data class DetonateMine(
        val playerPosition: PlayerPosition,
        val playerMines: Set<Pair<MineID, Point>>,
        val dashboardItems: List<DashboardItem>
    ) : GameState()
}

@OptIn(ExperimentalResourceApi::class)
class GameScreenModel : ViewModel(), KoinComponent {
    private val gameRepo by inject<GameRepo>()
    private val mediaPlayerController by inject<MediaPlayerController>()

    private val playerPosition = gameRepo.playerPosition
    private val playerMines = gameRepo.playerMines

    private val canReleaseMine = gameRepo.canReleaseMine
    private val canDetonateMine = gameRepo.canDetonateMine

    private val showMineDetonationNumbers = MutableStateFlow(false)

    val gameState = combine(
        playerPosition,
        playerMines,
        showMineDetonationNumbers,
        canReleaseMine,
        canDetonateMine,
    ) { playerPosition, playerMines, openDetonationMenu, canReleaseMine, canDetonateMine ->
        if (openDetonationMenu) {
            GameState.DetonateMine(
                playerPosition = playerPosition,
                playerMines = playerMines.map { mine ->
                    mine.first to mine.second
                }.toSet(),
                dashboardItems = listOf(
                    DashboardItem.Cancel,
                )
            )
        } else {
            GameState.Moving(
                playerPosition = playerPosition,
                playerMines = playerMines.map { it.second }.toSet(),
                dashboardItems = listOfNotNull(
                    if (canReleaseMine) DashboardItem.ReleaseMine else null,
                    if (canDetonateMine) DashboardItem.DetonateMine else null
                )
            )
        }
    }

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

    fun onCancel() {
        if (showMineDetonationNumbers.value) {
            showMineDetonationNumbers.value = false
        }
    }

    fun onDeployMine() {
        if (!showMineDetonationNumbers.value) {
            gameRepo.onDeployMine()
        }
    }

    fun onClickOpenDetonateView() {
        showMineDetonationNumbers.value = true
    }

    private fun onClickDetonateMine(mineID: MineID) {
        gameRepo.onDetonateMine(mineID)
    }

    fun onClickNumber(number: Long) {
        if (showMineDetonationNumbers.value) {
            onClickDetonateMine(MineID(number.toInt()))
        }
    }
}

@Composable
fun GameScreen(viewModel: GameScreenModel = viewModel { GameScreenModel() }) {
    val inputRequester = remember { FocusRequester() }

    val gameState by viewModel.gameState.collectAsState(GameState.Initial)

    LaunchedEffect(Unit) {
        inputRequester.requestFocus()
    }

    Spacer(
        Modifier.onKeyEvent { keyEvent ->
            val isUp = keyEvent.key == Key.W || keyEvent.key == Key.DirectionUp
            val isDown = keyEvent.key == Key.S || keyEvent.key == Key.DirectionDown
            val isLeft = keyEvent.key == Key.A || keyEvent.key == Key.DirectionLeft
            val isRight = keyEvent.key == Key.D || keyEvent.key == Key.DirectionRight

            val isCancel = keyEvent.key == Key.Escape
            val isDeployMine = keyEvent.key == Key.One
            val isDetonate = keyEvent.key == Key.F
            val isNumber =
                keyEvent.key >= Key.One && keyEvent.key <= Key.Nine || keyEvent.key == Key.Zero

            if (keyEvent.type == KeyDown) {
                if (isUp) {
                    viewModel.onClickUp()
                }
                if (isDown) {
                    viewModel.onClickDown()
                }
                if (isLeft) {
                    viewModel.onClickLeft()
                }
                if (isRight) {
                    viewModel.onClickRight()
                }
                if (isCancel) {
                    viewModel.onCancel()
                }
                if (isNumber) {
                    viewModel.onClickNumber(if (keyEvent.key == Key.Zero) 0 else keyEvent.key.keyCode - (Key.One.keyCode - 1))
                }
                if (isDeployMine) {
                    viewModel.onDeployMine()
                }
                if (isDetonate) {
                    viewModel.onClickOpenDetonateView()
                }
            }
            false
        }
            .focusRequester(inputRequester)
            .focusable())

    when (val state = gameState) {
        is GameState.Initial -> Unit
        is GameState.Moving -> {
            Row {
                Board(
                    verticalTilesCount = 14, horizontalTilesCount = 14,
                ) {
                    Submarine(Point(state.playerPosition.x, state.playerPosition.y))
                    state.playerMines.forEach {
                        Mine(it)
                    }
                }
                Spacer(Modifier.width(20.dp))
                Dashboard(state.dashboardItems)
            }
        }

        is GameState.DetonateMine -> {
            Row {
                Board(
                    verticalTilesCount = 14, horizontalTilesCount = 14,
                ) {
                    Submarine(Point(state.playerPosition.x, state.playerPosition.y))
                    state.playerMines.forEach {
                        NumberedMine(
                            point = it.second,
                            mineID = it.first,
                        )
                    }
                }
                Spacer(Modifier.width(20.dp))
                Dashboard(state.dashboardItems)
            }
        }
    }

}

private operator fun Key.compareTo(key: Key): Int = (keyCode - key.keyCode).toInt()