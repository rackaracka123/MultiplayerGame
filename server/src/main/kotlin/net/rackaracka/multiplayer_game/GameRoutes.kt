package net.rackaracka.multiplayer_game

import io.ktor.server.routing.Route
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.server.websocket.receiveDeserialized
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Collections
import java.util.concurrent.atomic.AtomicInteger

fun Route.gameRoutes() {
    val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
    val game = GameService()
    webSocket("game") {
        val thisConnection = Connection(this)
        connections += thisConnection
        val playerId = PlayerID(thisConnection.name)
        game.connectPlayer(playerId)
        try {
            launch {
                game.playersFlow.collect {
                    println("[${thisConnection.name}]: players $it")
                    connections.forEach { ws ->
                        ws.session.sendSerialized(it.map { it.second })
                    }
                }
            }

            while (isActive) {
                val command: GameCommandDTO = receiveDeserialized()
                when (val cmd = command) {
                    is GameCommandDTO.MoveDTO -> {
                        println("Incoming: Move command")
                        game.move(playerId, cmd.direction)
                    }
                }
            }
        } catch (e: Exception) {
            println("[${thisConnection.name}]: Error: ${e.message}")
        } finally {
            println("[${thisConnection.name}]: Removing")
            connections -= thisConnection
            game.disconnectPlayer(playerId)
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