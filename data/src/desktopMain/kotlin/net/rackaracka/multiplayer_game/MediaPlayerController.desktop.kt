package net.rackaracka.multiplayer_game

actual class MediaPlayerControllerImpl : MediaPlayerController {
    private val korgeMediaPlayerController = KorgeMediaPlayerController(PlatformContext())

    override suspend fun prepare(mediaBytes: ByteArray, listener: MediaPlayerListener) =
        korgeMediaPlayerController.prepare(mediaBytes, object : KorgeMediaPlayerListener {
            override fun onPrepared() = listener.onPrepared()

            override fun onCompletion() = listener.onPrepared()

            override fun onError(exception: Exception) = listener.onError(exception)
        })

    override suspend fun start() =
        korgeMediaPlayerController.start()

    override fun pause() = korgeMediaPlayerController.pause()

    override fun stop() = korgeMediaPlayerController.stop()

    override fun isPlaying(): Boolean = korgeMediaPlayerController.isPlaying()
}