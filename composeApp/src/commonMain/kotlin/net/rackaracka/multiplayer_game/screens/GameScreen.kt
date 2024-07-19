package net.rackaracka.multiplayer_game.screens

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import multiplayergame.composeapp.generated.resources.Res
import net.rackaracka.multiplayer_game.Board
import net.rackaracka.multiplayer_game.Dashboard
import net.rackaracka.multiplayer_game.DashboardItem
import net.rackaracka.multiplayer_game.Direction
import net.rackaracka.multiplayer_game.GameEvent
import net.rackaracka.multiplayer_game.GameRepo
import net.rackaracka.multiplayer_game.MediaPlayerController
import net.rackaracka.multiplayer_game.MediaPlayerListener
import net.rackaracka.multiplayer_game.Point
import net.rackaracka.multiplayer_game.Sector
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// Reworka pause så att det finns något som håller i vad anledningen är för pausen
// Detta gör så att man inte kan lägga minor och skanna med sonar samtidigt

// Och man borde inte röra sig om man dör

@OptIn(ExperimentalResourceApi::class)
class GameScreenModel : ViewModel(), KoinComponent {
    private val gameRepo by inject<GameRepo>()
    private val mediaPlayerController by inject<MediaPlayerController>()

    val boardSize = gameRepo.boardSize
    val sectorSize = gameRepo.sectorSize

    val gameEvent = gameRepo.gameEvent

    private val player = gameRepo.player
    val playerPosition = player.map { it.position }.eagerStateIn(null)

    private val _playerMines = player.map { it.mines }.eagerStateIn(setOf())

    val playerMines = _playerMines.map {
        it.mapIndexed { index, mine ->
            index to mine.position
        }
    }.eagerStateIn(emptyList())

    private val isGamePaused = gameRepo.isGamePaused.eagerStateIn(false)

    private val canReleaseMine = gameRepo.canReleaseMine
    private val canDetonateMine = gameRepo.canDetonateMine
    private val canSonar = gameRepo.canSonar

    private val _sonarScanResult = MutableStateFlow<Pair<Boolean, Sector>?>(null)
    val sonarScanResult = _sonarScanResult.asStateFlow()

    private val _detonatedMines = MutableStateFlow<List<Point>>(emptyList())
    val detonatedMines = _detonatedMines.asStateFlow()

    private val userWantsToDetonateMine = MutableStateFlow(false)
    private val userWantsToScanWithSonar = MutableStateFlow(false)

    val highlightSectorsWithNumbers = combine(
        userWantsToScanWithSonar,
        isGamePaused
    ) { scanWithSonar, paused ->
        if (scanWithSonar && paused) {
            val nrOfSectors = boardSize / sectorSize
            (0 until nrOfSectors).map { y ->
                (0 until nrOfSectors).map { x ->
                    Sector(
                        Point(x * sectorSize, y * sectorSize),
                        Point((x * sectorSize) + sectorSize, (y * sectorSize) + sectorSize)
                    )
                }
            }.flatten()
        } else emptyList()
    }.eagerStateIn(emptyList())

    val showMineDetonationNumbers =
        combine(userWantsToDetonateMine, isGamePaused) { showNumbers, isPaused ->
            isPaused && showNumbers
        }.eagerStateIn(false)

    val dashboardItems = combine(
        showMineDetonationNumbers,
        highlightSectorsWithNumbers,
        canReleaseMine,
        canDetonateMine,
        canSonar,
    ) { showMineNumbers, showSectorNumbers, releaseMine, detonateMine, sonar ->
        if (showMineNumbers || showSectorNumbers.isNotEmpty()) {
            listOf(
                DashboardItem.Cancel,
            )
        } else {
            listOfNotNull(
                if (releaseMine) DashboardItem.ReleaseMine else null,
                if (detonateMine) DashboardItem.DetonateMine else null,
                if (sonar) DashboardItem.Sonar else null,
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
        if (userWantsToDetonateMine.value) {
            gameRepo.onResumeGame()
            userWantsToDetonateMine.value = false
        }
        if (userWantsToScanWithSonar.value) {
            gameRepo.onResumeGame()
            userWantsToScanWithSonar.value = false
        }
    }

    fun onDeployMine() {
        if (!userWantsToDetonateMine.value) {
            gameRepo.onDeployMine()
        }
    }

    fun onClickOpenDetonateView() {
        gameRepo.onPauseGame()
        userWantsToDetonateMine.value = true
    }

    private fun onClickDetonateMine(mineID: Int) {
        val mineIndex = playerMines.value.indexOfFirst { it.first == mineID }
        val mine = _playerMines.value.toList()[mineIndex]
        if (gameRepo.onDetonateMine(mine.id)) {
            _detonatedMines.value += mine.position
        }
        viewModelScope.launch {
            delay(200)
            gameRepo.onResumeGame()
            userWantsToDetonateMine.value = false
        }
    }

    private fun resetSonarScanner() = viewModelScope.launch {
        delay(2000)
        gameRepo.onResumeGame()
        _sonarScanResult.value = null
    }


    private fun onClickStartSonarScan(index: Int) {
        val scan = gameRepo.onClickSonar(index)
        resetSonarScanner()
        if (scan != null) {
            _sonarScanResult.value = true to scan
        }
    }

    fun onClickNumber(number: Long) {
        if (userWantsToDetonateMine.value) {
            if (number < playerMines.value.size && number >= 0) {
                onClickDetonateMine(playerMines.value[number.toInt()].first)
            }
        }
        if (userWantsToScanWithSonar.value) {
            if (number in 1..9) {
                onClickStartSonarScan(number.toInt() - 1)
            }
        }
    }

    fun onClickSonar() {
        gameRepo.onPauseGame()
        userWantsToScanWithSonar.value = true
    }

    private fun <T> Flow<T>.eagerStateIn(
        initialValue: T
    ) = stateIn(viewModelScope, SharingStarted.Eagerly, initialValue)
}

@Composable
fun GameScreen(viewModel: GameScreenModel = viewModel { GameScreenModel() }) {
    val inputRequester = remember { FocusRequester() }

    val boardSize = viewModel.boardSize

    val playerPosition by viewModel.playerPosition.collectAsState()
    val playerMines by viewModel.playerMines.collectAsState()
    val showMineDetonationNumbers by viewModel.showMineDetonationNumbers.collectAsState()
    val detonatedMines by viewModel.detonatedMines.collectAsState()
    val dashboardItems by viewModel.dashboardItems.collectAsState()
    val gameEvent by viewModel.gameEvent.collectAsState()

    val sonarScanResult by viewModel.sonarScanResult.collectAsState()
    val highlightSectorsWithNumbers by viewModel.highlightSectorsWithNumbers.collectAsState()

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
            val isSonar = keyEvent.key == Key.Two

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
                if (isSonar) {
                    viewModel.onClickSonar()
                }
            }
            false
        }
            .focusRequester(inputRequester)
            .focusable())
    Column {
        when (val event = gameEvent) {
            null -> Unit
            is GameEvent.PlayerDied -> {
                Text("Spelare ${event.player.id} har dött")
            }
        }
        Row {
            Board(
                verticalTilesCount = boardSize, horizontalTilesCount = boardSize,
                sectorContent = {
                    sonarScanResult?.let {
                        HighlightSector(it.second, if (it.first) Color.Green else Color.Gray)
                    }
                    highlightSectorsWithNumbers.forEachIndexed { index, sector ->
                        HighlightSector(sector, Color.Black) {
                            Text("(${index + 1})")
                        }
                    }
                },
                tileContent = {
                    playerPosition?.let {
                        Submarine(Point(it.x, it.y))
                    }
                    playerMines.forEach {
                        if (showMineDetonationNumbers) {
                            NumberedMine(
                                point = it.second,
                                mineIndex = it.first,
                            )
                        } else {
                            Mine(it.second)
                        }
                    }
                    detonatedMines.forEach {
                        DetonatedMine(it)
                    }
                })
            Spacer(Modifier.width(20.dp))
            Dashboard(dashboardItems)
        }
    }
}

private operator fun Key.compareTo(key: Key): Int = (keyCode - key.keyCode).toInt()