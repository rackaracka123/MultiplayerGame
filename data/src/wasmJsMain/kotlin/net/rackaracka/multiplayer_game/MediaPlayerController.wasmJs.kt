package net.rackaracka.multiplayer_game

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.w3c.dom.Audio
import org.w3c.dom.url.URL
import org.w3c.files.Blob

// WORKS without 3rd party

actual class MediaPlayerControllerImpl : MediaPlayerController {
    private var audio: Audio? = null
    private var isAudioPlaying = false

    init {
        val testAudio =
            Audio("https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4")
        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)
            testAudio.play()
        }
    }

    override suspend fun prepare(mediaBytes: ByteArray, listener: MediaPlayerListener) {
        try {
            val url = URL.createObjectURL(Blob(JsArray<JsAny?>().apply {
                set(0, mediaBytes.toJsReference())
            }))
            audio = Audio(url)
            audio?.oncanplaythrough = {
                listener.onPrepared()
            }
            audio?.onended = {
                listener.onCompletion()
            }
        } catch (e: Exception) {
            listener.onError(e)
        }
    }

    override suspend fun start() {
        audio?.play()
        isAudioPlaying = true
    }

    override fun pause() {
        audio?.pause()
        isAudioPlaying = false
    }

    override fun stop() {
        audio?.pause()
        audio?.currentTime = 0.0
        isAudioPlaying = false
    }

    override fun isPlaying(): Boolean {
        return isAudioPlaying
    }
}
