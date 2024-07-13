package net.rackaracka.multiplayer_game

interface GameRepo {
    suspend fun session(command: suspend GameCommand.() -> Unit, onPlayerUpdate: (List<Player>) -> Unit)
}
