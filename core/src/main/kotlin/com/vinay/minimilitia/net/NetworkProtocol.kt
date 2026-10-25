package com.vinay.minimilitia.net

import kotlinx.serialization.Serializable

@Serializable
data class PlayerNetState(
    val id: String,
    val name: String,
    val team: Int,
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val aimAngle: Float,
    val isThrusting: Boolean,
    val isFiring: Boolean,
    val health: Float,
    val equippedWeapon: String,
    val kills: Int,
    val deaths: Int,
    val isDead: Boolean
)

@Serializable
sealed class GameMessage {
    // Client -> Server
    @Serializable
    data class JoinRequest(val playerName: String, val team: Int) : GameMessage()

    @Serializable
    data class InputUpdate(
        val x: Float,
        val y: Float,
        val vx: Float,
        val vy: Float,
        val aimAngle: Float,
        val isThrusting: Boolean,
        val isFiring: Boolean
    ) : GameMessage()

    @Serializable
    data class FireEvent(
        val weaponId: String,
        val originX: Float,
        val originY: Float,
        val angle: Float
    ) : GameMessage()

    @Serializable
    data class HitReport(
        val victimId: String,
        val damage: Float,
        val weaponId: String
    ) : GameMessage()

    // Server -> Client
    @Serializable
    data class JoinAccept(
        val assignedId: String,
        val roomCode: String,
        val mapName: String,
        val initialPlayers: List<PlayerNetState>
    ) : GameMessage()

    @Serializable
    data class PlayerJoined(
        val player: PlayerNetState
    ) : GameMessage()

    @Serializable
    data class WorldStateSnapshot(
        val players: List<PlayerNetState>,
        val timestamp: Long
    ) : GameMessage()

    @Serializable
    data class RemoteFire(
        val shooterId: String,
        val weaponId: String,
        val originX: Float,
        val originY: Float,
        val angle: Float
    ) : GameMessage()

    @Serializable
    data class DamageNotice(
        val victimId: String,
        val attackerId: String,
        val damage: Float,
        val remainingHealth: Float,
        val isKilled: Boolean,
        val weaponId: String
    ) : GameMessage()

    @Serializable
    data class RespawnNotice(
        val playerId: String,
        val spawnX: Float,
        val spawnY: Float
    ) : GameMessage()

    @Serializable
    data class PlayerDisconnected(val playerId: String) : GameMessage()
}
