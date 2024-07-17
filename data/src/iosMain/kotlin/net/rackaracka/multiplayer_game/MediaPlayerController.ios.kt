package net.rackaracka.multiplayer_game

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.delay
import platform.AVFoundation.AVAsset
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.play
import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.create
import platform.Foundation.fileURLWithPathComponents
import platform.Foundation.writeToURL

actual class MediaPlayerControllerImpl : MediaPlayerController {
    private lateinit var avPlayer: AVPlayer

    override suspend fun prepare(mediaBytes: ByteArray, listener: MediaPlayerListener) {
        val nsData = mediaBytes.toNSData()
        avPlayer = AVPlayer(AVPlayerItem(nsData.toAVAsset()))
        listener.onPrepared()
    }

    override suspend fun start() {
        delay(1000)
        avPlayer.play()
        delay(10000)
    }

    override fun pause() {
        TODO("Not yet implemented")
    }

    override fun stop() {
        TODO("Not yet implemented")
    }

    override fun isPlaying(): Boolean {
        TODO("Not yet implemented")
    }
}

@OptIn(ExperimentalForeignApi::class)
fun NSData.toAVAsset(): AVAsset {
    val directory = NSTemporaryDirectory()
    val fileName = "${NSUUID().UUIDString}.mp3"
    val fullURL = NSURL.fileURLWithPathComponents(listOf(directory, fileName))!!
    writeToURL(fullURL, true)
    val asset = AVAsset.assetWithURL(fullURL)
    return asset
}


@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun ByteArray.toNSData(): NSData = usePinned {
    NSData.create(bytes = it.addressOf(0), length = size.convert())
}