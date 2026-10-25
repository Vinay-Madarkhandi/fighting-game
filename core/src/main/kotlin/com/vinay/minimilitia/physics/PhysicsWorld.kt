package com.vinay.minimilitia.physics

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.vinay.minimilitia.constants.GameConstants

interface ContactCallback {
    fun onProjectileHitTerrain(projectileUserData: Any, hitPoint: Vector2)
    fun onProjectileHitPlayer(projectileUserData: Any, playerUserData: Any, hitPoint: Vector2)
    fun onPlayerPickup(playerUserData: Any, pickupUserData: Any)
    fun onPlayerGroundContact(playerUserData: Any, isGrounded: Boolean)
}

class PhysicsWorld(private val contactCallback: ContactCallback? = null) {
    val b2World: World = World(Vector2(0f, GameConstants.GRAVITY_Y), true)
    private var accumulator = 0f

    init {
        setupContactListener()
    }

    private fun setupContactListener() {
        b2World.setContactListener(object : ContactListener {
            override fun beginContact(contact: Contact) {
                val fixA = contact.fixtureA
                val fixB = contact.fixtureB
                val dataA = fixA.userData
                val dataB = fixB.userData
                val bodyDataA = fixA.body.userData
                val bodyDataB = fixB.body.userData

                // Foot sensor contact check
                if (dataA == "foot_sensor" || dataB == "foot_sensor") {
                    val playerObj = if (dataA == "foot_sensor") bodyDataA else bodyDataB
                    if (playerObj != null) {
                        contactCallback?.onPlayerGroundContact(playerObj, true)
                    }
                }

                // Projectile contacts
                val projData = if (dataA == "projectile") bodyDataA else if (dataB == "projectile") bodyDataB else null
                if (projData != null) {
                    val otherData = if (projData == bodyDataA) bodyDataB else bodyDataA
                    val worldManifold = contact.worldManifold
                    val hitPoint = if (worldManifold.numberOfContactPoints > 0) worldManifold.points[0] else fixA.body.position

                    if (otherData != null && otherData.toString().startsWith("player_")) {
                        contactCallback?.onProjectileHitPlayer(projData, otherData, hitPoint)
                    } else {
                        contactCallback?.onProjectileHitTerrain(projData, hitPoint)
                    }
                }

                // Pickups
                val pickupData = if (dataA == "pickup") bodyDataA else if (dataB == "pickup") bodyDataB else null
                if (pickupData != null) {
                    val otherData = if (pickupData == bodyDataA) bodyDataB else bodyDataA
                    if (otherData != null && otherData.toString().startsWith("player_")) {
                        contactCallback?.onPlayerPickup(otherData, pickupData)
                    }
                }
            }

            override fun endContact(contact: Contact) {
                val fixA = contact.fixtureA
                val fixB = contact.fixtureB
                val dataA = fixA.userData
                val dataB = fixB.userData
                val bodyDataA = fixA.body.userData
                val bodyDataB = fixB.body.userData

                if (dataA == "foot_sensor" || dataB == "foot_sensor") {
                    val playerObj = if (dataA == "foot_sensor") bodyDataA else bodyDataB
                    if (playerObj != null) {
                        contactCallback?.onPlayerGroundContact(playerObj, false)
                    }
                }
            }

            override fun preSolve(contact: Contact, oldManifold: Manifold) {}
            override fun postSolve(contact: Contact, impulse: ContactImpulse) {}
        })
    }

    fun step(delta: Float) {
        val frameTime = delta.coerceAtMost(0.25f)
        accumulator += frameTime
        while (accumulator >= GameConstants.TIME_STEP) {
            b2World.step(GameConstants.TIME_STEP, GameConstants.VELOCITY_ITERATIONS, GameConstants.POSITION_ITERATIONS)
            accumulator -= GameConstants.TIME_STEP
        }
    }

    fun dispose() {
        b2World.dispose()
    }
}
