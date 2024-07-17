package net.rackaracka.multiplayer_game

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.CoroutineContext

actual class MediaPlayerControllerImpl : MediaPlayerController, KoinComponent {
    private val context: Context by inject()

    private val korgeMediaPlayerController = KorgeMediaPlayerController(PlatformContext(context))

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