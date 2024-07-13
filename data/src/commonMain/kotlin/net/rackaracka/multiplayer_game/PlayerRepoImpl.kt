package net.rackaracka.multiplayer_game

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.websocket.readBytes
import kotlinx.serialization.json.Json

class PlayerRepoImpl : PlayerRepo {
    override suspend fun playerSession(onPlayerChanged: (Player) -> Unit) {
        val httpClient = HttpClient() {
            install(WebSockets) {
                contentConverter = KotlinxWebsocketSerializationConverter(Json)
            }
        }

        try {
            httpClient.webSocket(
                method = HttpMethod.Get,
                host = "localhost",
                port = 8080,
                path = "/player"
            ) {
                for (frame in incoming) {
                    val data = receiveDeserialized<PlayerDTO>()
                    onPlayerChanged(data.toPlayer())
                }
            }
        } catch (e: Exception) {
            println("Could not connect to server.")
        }
    }
}

private fun PlayerDTO.toPlayer(): Player = Player(x = x, y = y)
