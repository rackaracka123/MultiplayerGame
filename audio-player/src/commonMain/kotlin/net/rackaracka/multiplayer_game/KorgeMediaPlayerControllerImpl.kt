package net.rackaracka.multiplayer_game

import korlibs.audio.sound.Sound
import korlibs.audio.sound.SoundChannel
import korlibs.audio.sound.await
import korlibs.audio.sound.playing
import korlibs.audio.sound.readSound
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay

interface KorgeMediaPlayerListener {
    fun onPrepared()
    fun onCompletion()
    fun onError(exception: Exception)
}

expect class PlatformContext

expect suspend fun <T> withContext(
    platformContext: PlatformContext,
    block: suspend CoroutineScope.() -> T
): T

class KorgeMediaPlayerController(private val context: PlatformContext) {
    private lateinit var sound: Sound
    private lateinit var soundChannel: SoundChannel
    private lateinit var listener: KorgeMediaPlayerListener

    suspend fun prepare(mediaBytes: ByteArray, listener: KorgeMediaPlayerListener) {
        this.listener = listener
        try {
            sound = mediaBytes.readSound()
            listener.onPrepared()
        } catch (e: Exception) {
            listener.onError(e)
        }
    }

    suspend fun start() {
        withContext(context) {
            sound.play()
            delay(100000)
            // Av någon anledning så är play suspended
            // men den håller inte tråden suspended tills ljudet är klart
            // Så man måste hålla tråden upptagen tills play är klar
        }
    }

    fun pause() {
        soundChannel.pause()
    }

    fun stop() {
        soundChannel.stop()
    }

    fun isPlaying(): Boolean {
        return try {
            soundChannel.playing
        } catch (e: Exception) {
            false
        }
    }
}
