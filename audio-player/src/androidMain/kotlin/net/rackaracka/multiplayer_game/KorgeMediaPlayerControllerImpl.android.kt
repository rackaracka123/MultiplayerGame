package net.rackaracka.multiplayer_game

import android.content.Context
import korlibs.io.android.withAndroidContext
import kotlinx.coroutines.CoroutineScope

actual class PlatformContext(val context: Context)

actual suspend fun <T> withContext(
    platformContext: PlatformContext,
    block: suspend CoroutineScope.() -> T
): T = withAndroidContext(platformContext.context.applicationContext, block)