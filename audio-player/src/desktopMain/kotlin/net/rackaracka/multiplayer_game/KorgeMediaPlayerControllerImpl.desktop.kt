package net.rackaracka.multiplayer_game

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

actual class PlatformContext(val context: CoroutineContext = CoroutineScope(Dispatchers.Main).coroutineContext)

actual suspend fun <T> withContext(
    platformContext: PlatformContext,
    block: suspend CoroutineScope.() -> T
): T = kotlinx.coroutines.withContext(platformContext.context, block)