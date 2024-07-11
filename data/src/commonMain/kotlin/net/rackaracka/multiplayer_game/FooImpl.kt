package net.rackaracka.multiplayer_game

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.readBytes

class PlayerRepoImpl: PlayerRepo {
    override suspend fun playerSession(onPlayerChanged: (Player) -> Unit) {
        val httpClient = HttpClient() {
            install(WebSockets)
        }

        try {
            httpClient.webSocket(
                method = HttpMethod.Get,
                host = "localhost",
                port = 8080,
                path = "/player"
            ) {
                for (frame in incoming) {
                    val loc = frame.readBytes().decodeToString()
                    onPlayerChanged(Player(loc.toInt(), loc.toInt()))
                }
            }
        } catch (e: Exception) {
            println("Could not connect to server.")
        }
    }
}