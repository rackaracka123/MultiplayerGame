package net.rackaracka.multiplayer_game

interface GameRepo {
    suspend fun session(
        gameScope: suspend GameController.() -> Unit,
        gameEventListener: GameEventListener
    )
}

interface GameEventListener {
    fun onPositionsChanged(playerPositions: List<PlayerPosition>)
}
