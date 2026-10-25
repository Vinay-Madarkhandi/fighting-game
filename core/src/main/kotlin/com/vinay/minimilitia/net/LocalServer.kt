package com.vinay.minimilitia.net

import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class LocalServer(val port: Int = 8080, val mapName: String = "outpost") {
    private var serverEngine: ApplicationEngine? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val nextPlayerId = AtomicInteger(1)
    private val sessions = ConcurrentHashMap<String, WebSocketSession>()
    private val playerStates = ConcurrentHashMap<String, PlayerNetState>()

    var isRunning = false
        private set

    fun start() {
        if (isRunning) return
        isRunning = true

        serverEngine = embeddedServer(CIO, port = port) {
            install(WebSockets) {
                pingPeriod = Duration.ofSeconds(15)
                timeout = Duration.ofSeconds(30)
                maxFrameSize = Long.MAX_VALUE
                masking = false
            }

            routing {
                webSocket("/game") {
                    val assignedId = "p_${nextPlayerId.getAndIncrement()}"
                    sessions[assignedId] = this

                    try {
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                val text = frame.readText()
                                handleClientMessage(assignedId, text)
                            }
                        }
                    } catch (e: Exception) {
                        // Connection lost
                    } finally {
                        sessions.remove(assignedId)
                        playerStates.remove(assignedId)
                        broadcast(GameMessage.PlayerDisconnected(assignedId))
                    }
                }
            }
        }.start(wait = false)

        // Start world snapshot broadcast loop at 25 Hz
        scope.launch {
            while (isRunning) {
                delay(40) // 25 times per second
                if (playerStates.isNotEmpty()) {
                    val snapshot = GameMessage.WorldStateSnapshot(
                        players = playerStates.values.toList(),
                        timestamp = System.currentTimeMillis()
                    )
                    broadcast(snapshot)
                }
            }
        }
    }

    private suspend fun handleClientMessage(senderId: String, text: String) {
        try {
            val msg = Json.decodeFromString<GameMessage>(text)
            when (msg) {
                is GameMessage.JoinRequest -> {
                    val initialList = playerStates.values.toList()
                    val newPlayer = PlayerNetState(
                        id = senderId,
                        name = msg.playerName,
                        team = msg.team,
                        x = 10f,
                        y = 10f,
                        vx = 0f,
                        vy = 0f,
                        aimAngle = 0f,
                        isThrusting = false,
                        isFiring = false,
                        health = 100f,
                        equippedWeapon = "ak47",
                        kills = 0,
                        deaths = 0,
                        isDead = false
                    )
                    playerStates[senderId] = newPlayer

                    // Send acceptance to joined player
                    val accept = GameMessage.JoinAccept(
                        assignedId = senderId,
                        roomCode = "LAN",
                        mapName = mapName,
                        initialPlayers = initialList
                    )
                    sendTo(senderId, accept)

                    // Announce to others
                    broadcast(GameMessage.PlayerJoined(newPlayer), excludeId = senderId)
                }

                is GameMessage.InputUpdate -> {
                    val current = playerStates[senderId]
                    if (current != null) {
                        playerStates[senderId] = current.copy(
                            x = msg.x,
                            y = msg.y,
                            vx = msg.vx,
                            vy = msg.vy,
                            aimAngle = msg.aimAngle,
                            isThrusting = msg.isThrusting,
                            isFiring = msg.isFiring
                        )
                    }
                }

                is GameMessage.FireEvent -> {
                    broadcast(
                        GameMessage.RemoteFire(
                            shooterId = senderId,
                            weaponId = msg.weaponId,
                            originX = msg.originX,
                            originY = msg.originY,
                            angle = msg.angle
                        ),
                        excludeId = senderId
                    )
                }

                is GameMessage.HitReport -> {
                    val victim = playerStates[msg.victimId]
                    if (victim != null && !victim.isDead) {
                        val newHp = (victim.health - msg.damage).coerceAtLeast(0f)
                        val isKilled = newHp <= 0f

                        playerStates[msg.victimId] = victim.copy(
                            health = newHp,
                            isDead = isKilled,
                            deaths = if (isKilled) victim.deaths + 1 else victim.deaths
                        )

                        if (isKilled) {
                            val attacker = playerStates[senderId]
                            if (attacker != null) {
                                playerStates[senderId] = attacker.copy(kills = attacker.kills + 1)
                            }
                        }

                        broadcast(
                            GameMessage.DamageNotice(
                                victimId = msg.victimId,
                                attackerId = senderId,
                                damage = msg.damage,
                                remainingHealth = newHp,
                                isKilled = isKilled,
                                weaponId = msg.weaponId
                            )
                        )
                    }
                }
                else -> {}
            }
        } catch (_: Exception) {}
    }

    private suspend fun sendTo(playerId: String, msg: GameMessage) {
        val session = sessions[playerId] ?: return
        val json = Json.encodeToString(msg)
        try {
            session.send(Frame.Text(json))
        } catch (_: Exception) {}
    }

    private suspend fun broadcast(msg: GameMessage, excludeId: String? = null) {
        val json = Json.encodeToString(msg)
        for ((id, session) in sessions) {
            if (id != excludeId) {
                try {
                    session.send(Frame.Text(json))
                } catch (_: Exception) {}
            }
        }
    }

    fun stop() {
        isRunning = false
        scope.cancel()
        serverEngine?.stop(1000, 2000)
        sessions.clear()
        playerStates.clear()
    }
}
