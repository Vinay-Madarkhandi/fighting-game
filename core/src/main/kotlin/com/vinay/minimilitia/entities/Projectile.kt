package com.vinay.minimilitia.entities

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.vinay.minimilitia.constants.GameConstants
import com.vinay.minimilitia.weapons.ProjectileType

class Projectile(
    val id: String,
    val ownerId: String,
    val type: ProjectileType,
    val damage: Float,
    val speed: Float,
    val startPos: Vector2,
    val direction: Vector2,
    val b2World: World,
    val blastRadius: Float = 3.2f,
    val isBouncing: Boolean = false,
    val maxBounces: Int = 3
) {
    var isAlive = true
    var lifetime = when (type) {
        ProjectileType.ROCKET -> 4.0f
        ProjectileType.GRENADE_SHELL, ProjectileType.FRAG_GRENADE -> 2.5f
        ProjectileType.SAW_BLADE -> 5.0f
        ProjectileType.FLAME_PARTICLE -> 0.4f
        ProjectileType.LASER_BEAM -> 0.08f
        else -> 2.0f
    }
    var bounceCount = 0

    val body: Body

    init {
        val projType = this.type
        val bodyDef = BodyDef().apply {
            type = BodyDef.BodyType.DynamicBody
            position.set(startPos)
            bullet = (projType != ProjectileType.FLAME_PARTICLE)
            linearDamping = if (projType == ProjectileType.FLAME_PARTICLE) 2.5f else 0f
            gravityScale = when (projType) {
                ProjectileType.ROCKET, ProjectileType.LASER_BEAM -> 0f
                ProjectileType.GRENADE_SHELL, ProjectileType.FRAG_GRENADE -> 1.0f
                ProjectileType.SAW_BLADE -> 0.8f
                ProjectileType.PELLET -> 0.2f
                else -> 0.05f
            }
        }
        body = b2World.createBody(bodyDef)
        body.userData = this

        val shape = CircleShape().apply {
            radius = when (projType) {
                ProjectileType.ROCKET -> 0.35f
                ProjectileType.GRENADE_SHELL, ProjectileType.FRAG_GRENADE -> 0.3f
                ProjectileType.SAW_BLADE -> 0.45f
                ProjectileType.FLAME_PARTICLE -> 0.5f
                else -> 0.12f
            }
        }

        val fixtureDef = FixtureDef().apply {
            this.shape = shape
            density = 0.5f
            friction = if (isBouncing) 0.1f else 0.4f
            restitution = if (isBouncing) 0.75f else 0.05f
            filter.categoryBits = GameConstants.CATEGORY_PROJECTILE
            filter.maskBits = (GameConstants.CATEGORY_TERRAIN.toInt() or GameConstants.CATEGORY_PLAYER.toInt()).toShort()
        }
        val fixture = body.createFixture(fixtureDef)
        fixture.userData = "projectile"
        shape.dispose()

        // Set initial velocity
        body.linearVelocity = direction.cpy().nor().scl(speed)
    }

    fun update(delta: Float) {
        lifetime -= delta
        if (lifetime <= 0f) {
            isAlive = false
        }
    }

    fun onHitTerrain() {
        if (isBouncing && bounceCount < maxBounces) {
            bounceCount++
        } else {
            isAlive = false
        }
    }

    fun onHitPlayer() {
        isAlive = false
    }

    fun render(shapeRenderer: ShapeRenderer) {
        if (!isAlive) return
        val pos = body.position
        val vel = body.linearVelocity

        when (type) {
            ProjectileType.BULLET, ProjectileType.PELLET, ProjectileType.SNIPER_ROUND -> {
                shapeRenderer.setColor(if (type == ProjectileType.SNIPER_ROUND) Color.CYAN else Color.YELLOW)
                val tailLength = (speed * 0.04f).coerceIn(0.2f, 0.8f)
                val tail = pos.cpy().sub(vel.cpy().nor().scl(tailLength))
                shapeRenderer.line(tail.x, tail.y, pos.x, pos.y)
            }
            ProjectileType.ROCKET -> {
                shapeRenderer.setColor(Color.RED)
                shapeRenderer.circle(pos.x, pos.y, 0.3f, 8)
                shapeRenderer.setColor(Color.ORANGE)
                val tail = pos.cpy().sub(vel.cpy().nor().scl(0.5f))
                shapeRenderer.line(tail.x, tail.y, pos.x, pos.y)
            }
            ProjectileType.SAW_BLADE -> {
                shapeRenderer.setColor(Color.LIGHT_GRAY)
                shapeRenderer.circle(pos.x, pos.y, 0.45f, 10)
            }
            ProjectileType.LASER_BEAM -> {
                shapeRenderer.setColor(Color.GREEN)
                val tail = pos.cpy().sub(vel.cpy().nor().scl(1.5f))
                shapeRenderer.line(tail.x, tail.y, pos.x, pos.y)
            }
            ProjectileType.FLAME_PARTICLE -> {
                shapeRenderer.setColor(1f, 0.4f, 0f, 0.7f)
                shapeRenderer.circle(pos.x, pos.y, 0.4f, 8)
            }
            else -> {
                shapeRenderer.setColor(Color.WHITE)
                shapeRenderer.circle(pos.x, pos.y, 0.2f, 8)
            }
        }
    }

    fun destroy() {
        b2World.destroyBody(body)
    }
}
