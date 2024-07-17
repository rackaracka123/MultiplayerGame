package net.rackaracka.multiplayer_game

import io.ktor.util.toJsArray
import org.w3c.dom.Audio
import org.w3c.dom.url.URL
import org.w3c.files.Blob

// It's a miracle that this code works
private fun wrapInsideArray(any: JsAny): JsArray<JsAny?> = js("[any]")


actual class MediaPlayerControllerImpl : MediaPlayerController {
    private var audio: Audio? = null
    private var isAudioPlaying = false
    
    override suspend fun prepare(mediaBytes: ByteArray, listener: MediaPlayerListener) {
        try {
            val bytes = wrapInsideArray(mediaBytes.toJsArray())
            val url = URL.createObjectURL(Blob(bytes))
            audio = Audio(url)
            audio?.oncanplaythrough = {
                listener.onPrepared()
            }
            audio?.onended = {
                listener.onCompletion()
            }
        } catch (e: Exception) {
            println("Error: $e")
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
