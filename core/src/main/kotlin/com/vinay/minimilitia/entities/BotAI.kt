package com.vinay.minimilitia.entities

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.vinay.minimilitia.constants.GameConstants

enum class BotState {
    PATROL,
    CHASE,
    ATTACK,
    RETREAT
}

class BotAI(
    val botPlayer: Player,
    private val waypoints: List<Vector2> = emptyList()
) {
    var state: BotState = BotState.PATROL
    private var stateTimer = 0f
    private var currentWaypointIndex = 0
    private var reactionTimer = 0f

    private val moveVec = Vector2()
    private val aimVec = Vector2()
    private var wantsFire = false
    private var wantsThrust = false

    fun update(delta: Float, targetPlayer: Player?, nearbyHealthPacks: List<WeaponPickup> = emptyList()) {
        if (botPlayer.isDead) return
        stateTimer += delta
        reactionTimer += delta

        if (targetPlayer == null || targetPlayer.isDead) {
            state = BotState.PATROL
        } else {
            val dist = botPlayer.body.position.dst(targetPlayer.body.position)
            if (botPlayer.health < 25f && nearbyHealthPacks.isNotEmpty()) {
                state = BotState.RETREAT
            } else if (dist < 14f) {
                state = BotState.ATTACK
            } else if (dist < 28f) {
                state = BotState.CHASE
            } else {
                state = BotState.PATROL
            }
        }

        moveVec.setZero()
        aimVec.setZero()
        wantsFire = false
        wantsThrust = false

        when (state) {
            BotState.PATROL -> {
                if (waypoints.isNotEmpty()) {
                    val targetWp = waypoints[currentWaypointIndex]
                    val botPos = botPlayer.body.position
                    val diff = targetWp.cpy().sub(botPos)

                    if (diff.len() < 2.0f) {
                        currentWaypointIndex = (currentWaypointIndex + 1) % waypoints.size
                    } else {
                        moveVec.x = diff.x.coerceIn(-1f, 1f)
                        if (diff.y > 1.5f && botPlayer.boostFuel > 20f) {
                            moveVec.y = 1f
                            wantsThrust = true
                        }
                    }
                } else {
                    // Random wander
                    if (stateTimer > 3f) {
                        stateTimer = 0f
                        moveVec.x = if (MathUtils.randomBoolean()) 1f else -1f
                    }
                }
            }

            BotState.CHASE -> {
                val botPos = botPlayer.body.position
                val targetPos = targetPlayer!!.body.position
                val diff = targetPos.cpy().sub(botPos)

                moveVec.x = if (diff.x > 0.5f) 1f else if (diff.x < -0.5f) -1f else 0f

                // Jetpack boost if target is elevated or obstacle in front
                if (diff.y > 1.0f && botPlayer.boostFuel > 15f) {
                    moveVec.y = 1f
                    wantsThrust = true
                }

                // Lead aim slightly
                aimVec.set(diff.nor())
            }

            BotState.ATTACK -> {
                val botPos = botPlayer.body.position
                val targetPos = targetPlayer!!.body.position
                val diff = targetPos.cpy().sub(botPos)

                // Add slight human-like aim deviation
                val aimDeviation = Vector2(MathUtils.random(-0.1f, 0.1f), MathUtils.random(-0.1f, 0.1f))
                aimVec.set(diff.nor().add(aimDeviation)).nor()

                // Reposition during combat (hovering & dodging)
                if (diff.y > 0.5f && botPlayer.boostFuel > 20f) {
                    wantsThrust = true
                    moveVec.y = 0.8f
                } else if (botPlayer.isGrounded && MathUtils.randomBoolean(0.02f)) {
                    wantsThrust = true
                    moveVec.y = 1.0f
                }

                // Keep distance: backpedal if too close to shotgun/melee
                if (diff.len() < 4f) {
                    moveVec.x = if (diff.x > 0) -0.8f else 0.8f
                } else {
                    moveVec.x = if (diff.x > 0) 0.6f else -0.6f
                }

                // Fire weapon burst
                wantsFire = botPlayer.equippedWeapon.canFire
                if (botPlayer.equippedWeapon.isClipEmpty) {
                    botPlayer.reload()
                }
            }

            BotState.RETREAT -> {
                val healthPack = nearbyHealthPacks.filter { it.isActive }.minByOrNull {
                    it.body.position.dst2(botPlayer.body.position)
                }
                if (healthPack != null) {
                    val diff = healthPack.body.position.cpy().sub(botPlayer.body.position)
                    moveVec.x = diff.x.coerceIn(-1f, 1f)
                    if (diff.y > 1f && botPlayer.boostFuel > 15f) {
                        moveVec.y = 1f
                        wantsThrust = true
                    }
                }
            }
        }

        botPlayer.handleInput(moveVec, aimVec, wantsFire, wantsThrust)
    }
}
