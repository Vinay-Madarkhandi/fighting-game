package com.vinay.minimilitia.net

import com.badlogic.gdx.math.Vector2
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentLinkedQueue

class NetworkManager {
    private val client = HttpClient(CIO) {
        install(WebSockets)
    }
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var activeSession: WebSocketSession? = null

    var isConnected = false
        private set
    var localAssignedId: String? = null
        private set

    val incomingEvents = ConcurrentLinkedQueue<GameMessage>()

    fun connect(host: String, port: Int, playerName: String, team: Int = 0) {
        scope.launch {
            try {
                client.webSocket(host = host, port = port, path = "/game") {
                    activeSession = this
                    isConnected = true

                    // Send Join Request
                    send(Frame.Text(Json.encodeToString<GameMessage>(GameMessage.JoinRequest(playerName, team))))

                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            try {
                                val msg = Json.decodeFromString<GameMessage>(frame.readText())
                                if (msg is GameMessage.JoinAccept) {
                                    localAssignedId = msg.assignedId
                                }
                                incomingEvents.add(msg)
                            } catch (_: Exception) {}
                        }
                    }
                }
            } catch (e: Exception) {
                isConnected = false
            } finally {
                isConnected = false
                activeSession = null
            }
        }
    }

    fun sendInput(pos: Vector2, vel: Vector2, aimAngle: Float, isThrusting: Boolean, isFiring: Boolean) {
        if (!isConnected || activeSession == null) return
        val msg = GameMessage.InputUpdate(
            x = pos.x,
            y = pos.y,
            vx = vel.x,
            vy = vel.y,
            aimAngle = aimAngle,
            isThrusting = isThrusting,
            isFiring = isFiring
        )
        send(msg)
    }

    fun sendFire(weaponId: String, origin: Vector2, angle: Float) {
        if (!isConnected || activeSession == null) return
        val msg = GameMessage.FireEvent(
            weaponId = weaponId,
            originX = origin.x,
            originY = origin.y,
            angle = angle
        )
        send(msg)
    }

    fun sendHit(victimId: String, damage: Float, weaponId: String) {
        if (!isConnected || activeSession == null) return
        val msg = GameMessage.HitReport(
            victimId = victimId,
            damage = damage,
            weaponId = weaponId
        )
        send(msg)
    }

    private fun send(msg: GameMessage) {
        val session = activeSession ?: return
        scope.launch {
            try {
                session.send(Frame.Text(Json.encodeToString(msg)))
            } catch (_: Exception) {}
        }
    }

    fun disconnect() {
        isConnected = false
        scope.launch {
            try {
                activeSession?.close()
            } catch (_: Exception) {}
            activeSession = null
        }
    }

    fun dispose() {
        disconnect()
        scope.cancel()
        client.close()
    }
}
