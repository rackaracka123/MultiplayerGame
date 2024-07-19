package net.rackaracka.multiplayer_game

data object GameNotPausedException : Exception("Game must be paused to perform this action")
data object GamePausedException : Exception("Game must NOT be paused to perform this action")
