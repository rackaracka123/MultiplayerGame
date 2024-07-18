package net.rackaracka.multiplayer_game

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlin.math.sqrt
import kotlin.random.Random


// Detta borde flyttas över till en server sen
// Vi vill nog skicka in player som parameter och sen får något autentiserings repo hantera vilken player som ska vart.

class GameRepoImpl(
    opponentCoroutineScope: CoroutineScope
) : GameRepo {

    private val players = MutableStateFlow(
        listOf(
            Player(HumanPlayerID, Point(0, 0), setOf(), 4), // Human
            Player(PlayerID(1), Point(5, 2), setOf(), 4) // AI
        )
    )
    private val sonarScanSize = 4
    override val boardSize: Int = 12

    override val player = players.map { it.first { it.id == HumanPlayerID } }

    override val canReleaseMine = player.map { it.canReleaseMine() }
    override val canDetonateMine = player.map { it.mines.isNotEmpty() }
    override val canSonar = player.map { true }

    private val _isGamePaused = MutableStateFlow(false)
    override val isGamePaused = _isGamePaused.asStateFlow()

    fun onMove(playerID: PlayerID, direction: Direction) {
        if (_isGamePaused.value) return
        val (dx, dy) = when (direction) {
            Direction.Up -> 0 to -1
            Direction.Down -> 0 to 1
            Direction.Left -> -1 to 0
            Direction.Right -> 1 to 0
        }
        modifyPlayerByID(playerID) { player ->
            val position = player.position
            val newPos = Point(
                position.x + dx,
                position.y + dy
            )

            if (player.mines.any { it.position == newPos }) {
                null
            } else {
                player.copy(position = newPos)
            }
        }
    }

    override fun onMove(direction: Direction) =
        onMove(HumanPlayerID, direction)

    fun onDeployMine(playerID: PlayerID) {
        if (_isGamePaused.value) return

        modifyPlayerByID(playerID) { player ->
            if (!player.canReleaseMine()) return@modifyPlayerByID null

            val playerPos = player.position
            val newMines = player.mines + Mine(
                MineID(Random.nextDouble().toString()),
                Point(playerPos.x, playerPos.y)
            )

            if (player.mines.size != newMines.size) {
                player.copy(mines = newMines)
            } else null
        }
    }

    override fun onDeployMine() = onDeployMine(HumanPlayerID)

    fun onDetonateMine(playerID: PlayerID, mineID: MineID): Boolean {
        if (!_isGamePaused.value) return false

        return modifyPlayerByID(playerID) { player ->
            val mine = player.mines.first { it.id == mineID }

            mine.damagePlayers()

            val newPlayerMines =
                player.mines.toMutableSet().apply { removeAll { it.id == mineID } }.toSet()
            if (newPlayerMines.size != player.mines.size) {
                player.copy(mines = newPlayerMines)
            } else {
                null
            }
        } != null
    }

    override fun onDetonateMine(mineID: MineID) =
        onDetonateMine(HumanPlayerID, mineID)


    override fun onPauseGame() {
        _isGamePaused.value = true
    }

    override fun onResumeGame() {
        _isGamePaused.value = false
    }

    override fun onClickSonar(): Sector? {
        val scanningPlayer = players.value.first { it.id == HumanPlayerID }
        return players.value.filter { it.id != HumanPlayerID }.firstNotNullOfOrNull {
            val topLeftX = (it.position.x / sonarScanSize) * sonarScanSize
            val topLeftY = (it.position.y / sonarScanSize) * sonarScanSize

            val bottomRightX = topLeftX + (sonarScanSize - 1)
            val bottomRightY = topLeftY + (sonarScanSize - 1)

            if (topLeftX <= scanningPlayer.position.x && topLeftY <= scanningPlayer.position.y) {
                if (bottomRightX >= scanningPlayer.position.x && bottomRightY >= scanningPlayer.position.y) {
                    Sector(Point(topLeftX, topLeftY), Point(bottomRightX, bottomRightY))
                } else null
            } else null
        }
    }

    private fun modifyPlayerByID(playerID: PlayerID, modify: (player: Player) -> Player?): Player? {
        val playerIndex = players.value.indexOfFirst { it.id == playerID }
        val modified = modify(players.value[playerIndex]) ?: return null
        players.value = players.value.toMutableList().apply {
            this[playerIndex] = modified
        }.toList()
        return modified
    }

    private fun Player.canReleaseMine() = mines.size < 4

    private fun Mine.damagePlayers() {
        players.value.forEach {
            modifyPlayerByID(it.id) { player ->
                val distancePoint = player.position - this.position
                val distance =
                    sqrt(((distancePoint.x * distancePoint.x) + (distancePoint.y * distancePoint.y)).toDouble())
                when (distance) {
                    1.0 -> {
                        player.copy(health = player.health - 1)
                    }

                    0.0 -> {
                        player.copy(health = player.health - 2)
                    }

                    else -> null
                }
            }
        }
    }

    companion object {
        private val HumanPlayerID = PlayerID(0)
    }
}