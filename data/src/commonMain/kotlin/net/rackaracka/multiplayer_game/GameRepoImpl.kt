package net.rackaracka.multiplayer_game

import io.ktor.client.HttpClient
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
                    val gameController = object : GameController {
                        override fun move(x: Int, y: Int) {
                            this@webSocket.launch {
                                this@webSocket.sendSerialized(
                                    GameCommandDTO.MoveDTO(x, y) as GameCommandDTO
                                )
                            }
                        }
                    }
                    gameScope(gameController)
                }
                while (isActive) {
                    val data = receiveDeserialized<List<PlayerDTO>>()
                    println("Incoming: $data")
                    gameEventListener.onPositionsChanged(data.mapToPlayer())
                }
            }
        } catch (e: Exception) {
            println("Could not connect to server. $e")
        }
    }
}

private fun List<PlayerDTO>.mapToPlayer() = map { PlayerPosition(x = it.x, y = it.y) }
