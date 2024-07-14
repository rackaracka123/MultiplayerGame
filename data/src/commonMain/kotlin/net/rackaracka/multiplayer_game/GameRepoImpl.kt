package net.rackaracka.multiplayer_game

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class GameRepoImpl : GameRepo {
    override suspend fun session(
        gameScope: suspend GameController.() -> Unit,
        gameEventListener: GameEventListener
    ) {
        val httpClient = HttpClient() {
            install(WebSockets) {
                contentConverter = KotlinxWebsocketSerializationConverter(Json)
            }
        }

        try {
            httpClient.webSocket(
                method = HttpMethod.Get,
                host = "192.168.0.49",
                port = 8080,
                path = "/game"
            ) {
                launch {
                    val gameController = GameControllerImpl(this@webSocket)
                    gameScope(gameController)
                }
                while (isActive) {
                    val data = receiveDeserialized<List<PlayerPositionDTO>>()
                    gameEventListener.onPositionsChanged(data.mapToPlayer())
                }
            }
        } catch (e: Exception) {
            println("Could not connect to server. $e")
        }
    }
}

private class GameControllerImpl(private val wss: DefaultClientWebSocketSession) : GameController {
    override fun move(direction: Direction) {
        wss.sendCommand(direction.toMoveCommand())
    }
}

private fun DefaultClientWebSocketSession.sendCommand(command: GameCommandDTO) {
    launch {
        sendSerialized(command)
    }
}
