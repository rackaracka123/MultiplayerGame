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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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
import net.rackaracka.multiplayer_game.Point
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// Flytta över showMineDetonationNumbers till data lagret, man ska inte kunna flytta sig när det är aktivt.
@OptIn(ExperimentalResourceApi::class)
class GameScreenModel : ViewModel(), KoinComponent {
    private val gameRepo by inject<GameRepo>()
    private val mediaPlayerController by inject<MediaPlayerController>()

    val playerPosition = gameRepo.playerPosition
    val playerMines = gameRepo.playerMines

    private val canReleaseMine = gameRepo.canReleaseMine
    private val canDetonateMine = gameRepo.canDetonateMine

    private val _detonatedMines = MutableStateFlow<List<Point>>(emptyList())
    val detonatedMines = _detonatedMines.asStateFlow()

    private val _showMineDetonationNumbers = MutableStateFlow(false)
    val showMineDetonationNumbers = _showMineDetonationNumbers.asStateFlow()

    val dashboardItems = combine(
        _showMineDetonationNumbers,
        canReleaseMine,
        canDetonateMine
    ) { openMenu, releaseMine, detonateMine ->
        if (openMenu) {
            listOf(
                DashboardItem.Cancel,
            )
        } else {
            listOfNotNull(
                if (releaseMine) DashboardItem.ReleaseMine else null,
                if (detonateMine) DashboardItem.DetonateMine else null
            )
        }
    }.eagerStateIn(emptyList())

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
        if (_showMineDetonationNumbers.value) {
            _showMineDetonationNumbers.value = false
        }
    }

    fun onDeployMine() {
        if (!_showMineDetonationNumbers.value) {
            gameRepo.onDeployMine()
        }
    }

    fun onClickOpenDetonateView() {
        _showMineDetonationNumbers.value = true
    }

    private fun onClickDetonateMine(mineID: MineID) {
        val mine = playerMines.value.firstOrNull { it.first == mineID }
        if (mine != null && gameRepo.onDetonateMine(mineID)) {
            _detonatedMines.value += mine.second
        }
    }

    fun onClickNumber(number: Long) {
        if (_showMineDetonationNumbers.value) {
            onClickDetonateMine(MineID(number.toInt()))
        }
    }

    private fun <T> Flow<T>.eagerStateIn(
        initialValue: T
    ) = stateIn(viewModelScope, SharingStarted.Eagerly, initialValue)
}

@Composable
fun GameScreen(viewModel: GameScreenModel = viewModel { GameScreenModel() }) {
    val inputRequester = remember { FocusRequester() }

    val playerPosition by viewModel.playerPosition.collectAsState()
    val playerMines by viewModel.playerMines.collectAsState()
    val showMineDetonationNumbers by viewModel.showMineDetonationNumbers.collectAsState()
    val detonatedMines by viewModel.detonatedMines.collectAsState()
    val dashboardItems by viewModel.dashboardItems.collectAsState()

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

    Row {
        Board(
            verticalTilesCount = 14, horizontalTilesCount = 14,
        ) {
            Submarine(Point(playerPosition.x, playerPosition.y))
            playerMines.forEach {
                if (showMineDetonationNumbers) {
                    NumberedMine(
                        point = it.second,
                        mineID = it.first,
                    )
                } else {
                    Mine(it.second)
                }
            }
            detonatedMines.forEach {
                DetonatedMine(it)
            }
        }
        Spacer(Modifier.width(20.dp))
        Dashboard(dashboardItems)
    }
}

private operator fun Key.compareTo(key: Key): Int = (keyCode - key.keyCode).toInt()