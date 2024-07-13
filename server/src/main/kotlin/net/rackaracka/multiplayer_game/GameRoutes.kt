package net.rackaracka.multiplayer_game

import io.ktor.server.routing.Route
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.DefaultWebSocketSession
import kotlinx.coroutines.launch
import java.util.Collections
import java.util.concurrent.atomic.AtomicInteger

fun Route.gameRoutes() {
    val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
    val game = GameService()
    webSocket("players") {
        val thisConnection = Connection(this)
        connections += thisConnection
        val player = game.connectPlayer()
        try {
            launch {
                game.playersFlow.collect {
                    println("[${thisConnection.name}]: players $it")
                    connections.forEach { ws ->
                        ws.session.sendSerialized(it)
                    }
                }
            }
            for (frame in incoming) {
                println("Incoming: ${frame.data.decodeToString()}")
            }
        } catch (e: Exception) {
            println("[${thisConnection.name}]: Error: ${e.message}")
        } finally {
            println("[${thisConnection.name}]: Removing")
            connections -= thisConnection
            game.disconnectPlayer(player)
            connections.forEach { ws ->
                ws.session.sendSerialized(game.playersFlow.value)
            }
        }
    }
}


class Connection(val session: WebSocketServerSession) {
    companion object {
        val lastId = AtomicInteger(0)
    }

    val name = "user${lastId.getAndIncrement()}"
}