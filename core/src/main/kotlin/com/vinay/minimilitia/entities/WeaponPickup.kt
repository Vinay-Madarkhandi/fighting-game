package com.vinay.minimilitia.entities

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.vinay.minimilitia.constants.GameConstants

class WeaponPickup(
    val id: String,
    val weaponId: String,
    val position: Vector2,
    val b2World: World,
    val isStaticSpawner: Boolean = true,
    val respawnDelay: Float = 15f
) {
    var isActive = true
    var respawnTimer = 0f
    val body: Body

    val isHealthPack: Boolean get() = weaponId == "healthpack"
    val isBoostTank: Boolean get() = weaponId == "boosttank"

    init {
        val bodyDef = BodyDef().apply {
            type = if (isStaticSpawner) BodyDef.BodyType.StaticBody else BodyDef.BodyType.DynamicBody
            this.position.set(position)
        }
        body = b2World.createBody(bodyDef)
        body.userData = this

        val shape = CircleShape().apply {
            radius = 0.8f
        }
        val fixtureDef = FixtureDef().apply {
            this.shape = shape
            isSensor = true
            filter.categoryBits = GameConstants.CATEGORY_PICKUP
            filter.maskBits = GameConstants.CATEGORY_PLAYER
        }
        val fixture = body.createFixture(fixtureDef)
        fixture.userData = "pickup"
        shape.dispose()
    }

    fun update(delta: Float) {
        if (!isActive && isStaticSpawner) {
            respawnTimer -= delta
            if (respawnTimer <= 0f) {
                isActive = true
            }
        }
    }

    fun collect() {
        isActive = false
        if (isStaticSpawner) {
            respawnTimer = respawnDelay
        } else {
            // Dropped weapon gets destroyed
            b2World.destroyBody(body)
        }
    }

    fun render(shapeRenderer: ShapeRenderer) {
        if (!isActive) return
        val pos = body.position

        when {
            isHealthPack -> {
                // Green health cross
                shapeRenderer.setColor(Color.GREEN)
                shapeRenderer.circle(pos.x, pos.y, 0.45f, 10)
                shapeRenderer.setColor(Color.WHITE)
                shapeRenderer.rect(pos.x - 0.25f, pos.y - 0.08f, 0.5f, 0.16f)
                shapeRenderer.rect(pos.x - 0.08f, pos.y - 0.25f, 0.16f, 0.5f)
            }
            isBoostTank -> {
                // Blue jetpack fuel tank
                shapeRenderer.setColor(Color.CYAN)
                shapeRenderer.circle(pos.x, pos.y, 0.4f, 10)
                shapeRenderer.setColor(Color.YELLOW)
                shapeRenderer.circle(pos.x, pos.y, 0.2f, 6)
            }
            else -> {
                // Weapon box
                shapeRenderer.setColor(Color.ORANGE)
                shapeRenderer.rect(pos.x - 0.5f, pos.y - 0.2f, 1.0f, 0.4f)
                shapeRenderer.setColor(Color.WHITE)
                shapeRenderer.rect(pos.x - 0.35f, pos.y - 0.05f, 0.7f, 0.1f)
            }
        }
    }

    fun destroy() {
        b2World.destroyBody(body)
    }
}
