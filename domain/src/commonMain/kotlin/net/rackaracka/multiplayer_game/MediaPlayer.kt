package net.rackaracka.multiplayer_game

interface MediaPlayerController {
    suspend fun prepare(
        mediaBytes: ByteArray,
        listener: MediaPlayerListener
    )

    suspend fun start()
    fun pause()
    fun stop()
    fun isPlaying(): Boolean
}

interface MediaPlayerListener {
    fun onPrepared()
    fun onCompletion()
    fun onError(exception: Exception)
}
