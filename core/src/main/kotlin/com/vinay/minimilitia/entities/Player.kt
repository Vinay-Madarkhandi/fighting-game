package com.vinay.minimilitia.entities

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.vinay.minimilitia.audio.AudioManager
import com.vinay.minimilitia.constants.GameConstants
import com.vinay.minimilitia.weapons.Weapon
import com.vinay.minimilitia.weapons.WeaponDefinitions

class Player(
    val id: String,
    var name: String,
    val isLocal: Boolean,
    val b2World: World,
    initialPos: Vector2,
    var team: Int = 0 // 0 = FFA, 1 = Blue Team, 2 = Red Team
) {
    var health: Float = GameConstants.PLAYER_MAX_HEALTH
    var boostFuel: Float = GameConstants.PLAYER_MAX_BOOST
    var isGrounded: Boolean = false
    var isThrusting: Boolean = false
    var isFiring: Boolean = false
    var isDead: Boolean = false
    var respawnTimer: Float = 0f
    var spawnShieldTimer: Float = GameConstants.PLAYER_SPAWN_SHIELD_DURATION
    var facingRight: Boolean = true
    var aimAngleDegrees: Float = 0f

    var kills: Int = 0
    var deaths: Int = 0

    // Weapons
    var primaryWeapon: Weapon = Weapon(WeaponDefinitions.get("ak47"))
    var secondaryWeapon: Weapon? = Weapon(WeaponDefinitions.get("deagle"))
    var activeWeaponSlot: Int = 0 // 0 = primary, 1 = secondary
    var grenadeCount: Int = 3

    val equippedWeapon: Weapon get() = if (activeWeaponSlot == 1 && secondaryWeapon != null) secondaryWeapon!! else primaryWeapon

    val body: Body

    init {
        val bodyDef = BodyDef().apply {
            type = BodyDef.BodyType.DynamicBody
            position.set(initialPos)
            fixedRotation = true
            linearDamping = 0.8f
        }
        body = b2World.createBody(bodyDef)
        body.userData = "player_$id"

        // Torso / Head capsule fixture (circle for smooth sliding over terrain)
        val circleShape = CircleShape().apply {
            radius = GameConstants.PLAYER_RADIUS
            position.set(0f, 0.2f)
        }
        val mainFixtureDef = FixtureDef().apply {
            shape = circleShape
            density = 1.0f
            friction = 0.2f
            restitution = 0.0f
            filter.categoryBits = GameConstants.CATEGORY_PLAYER
            filter.maskBits = (GameConstants.CATEGORY_TERRAIN.toInt() or
                    GameConstants.CATEGORY_PROJECTILE.toInt() or
                    GameConstants.CATEGORY_PICKUP.toInt()).toShort()
        }
        val mainFixture = body.createFixture(mainFixtureDef)
        mainFixture.userData = "player_body"
        circleShape.dispose()

        // Foot sensor to detect ground contact
        val footShape = PolygonShape().apply {
            setAsBox(0.4f, 0.15f, Vector2(0f, -GameConstants.PLAYER_RADIUS + 0.1f), 0f)
        }
        val footFixtureDef = FixtureDef().apply {
            shape = footShape
            isSensor = true
            filter.categoryBits = GameConstants.CATEGORY_SENSOR
            filter.maskBits = GameConstants.CATEGORY_TERRAIN
        }
        val footFixture = body.createFixture(footFixtureDef)
        footFixture.userData = "foot_sensor"
        footShape.dispose()
    }

    fun handleInput(
        moveVec: Vector2,
        aimVec: Vector2,
        wantsFire: Boolean,
        wantsThrust: Boolean
    ) {
        if (isDead) return

        // Aiming
        if (aimVec.len2() > 0.05f) {
            aimAngleDegrees = MathUtils.radiansToDegrees * MathUtils.atan2(aimVec.y, aimVec.x)
            facingRight = (aimAngleDegrees > -90f && aimAngleDegrees < 90f)
        }

        // Horizontal Movement
        if (kotlin.math.abs(moveVec.x) > GameConstants.JOYSTICK_DEADZONE) {
            val targetVelX = moveVec.x * GameConstants.PLAYER_WALK_SPEED
            val velChangeX = targetVelX - body.linearVelocity.x
            body.applyLinearImpulse(Vector2(velChangeX * body.mass * 0.4f, 0f), body.worldCenter, true)
            if (aimVec.len2() <= 0.05f) {
                facingRight = moveVec.x > 0
            }
        } else {
            // Apply ground braking
            if (isGrounded) {
                body.linearVelocity = Vector2(body.linearVelocity.x * 0.7f, body.linearVelocity.y)
            }
        }

        // Jetpack Thrust
        isThrusting = (wantsThrust || moveVec.y > GameConstants.JETPACK_TRIGGER_Y) && boostFuel > 0f
        if (isThrusting) {
            val thrustDir = Vector2(moveVec.x * 0.6f, (if (moveVec.y > 0.1f) moveVec.y else 1.0f)).nor()
            val force = thrustDir.scl(GameConstants.PLAYER_JETPACK_THRUST * body.mass)
            body.applyForceToCenter(force, true)
        }

        isFiring = wantsFire
    }

    fun update(delta: Float, audioManager: AudioManager?) {
        // Respawn countdown
        if (isDead) {
            respawnTimer -= delta
            if (respawnTimer <= 0f) {
                // Respawn ready
            }
            return
        }

        // Spawn shield countdown
        if (spawnShieldTimer > 0f) {
            spawnShieldTimer -= delta
        }

        // Boost fuel update
        if (isThrusting) {
            boostFuel = (boostFuel - GameConstants.PLAYER_BOOST_BURN_RATE * delta).coerceAtLeast(0f)
            if (isLocal) {
                audioManager?.startJetpackThrust()
            }
        } else {
            if (isLocal) {
                audioManager?.stopJetpackThrust()
            }
            val rechargeMultiplier = if (isGrounded) 1.6f else 1.0f
            boostFuel = (boostFuel + GameConstants.PLAYER_BOOST_RECHARGE_RATE * rechargeMultiplier * delta)
                .coerceAtMost(GameConstants.PLAYER_MAX_BOOST)
        }

        // Update weapons
        primaryWeapon.update(delta)
        secondaryWeapon?.update(delta)
    }

    fun canFire(): Boolean = !isDead && equippedWeapon.canFire

    fun onFired(): Float {
        val weapon = equippedWeapon
        weapon.currentClip--
        weapon.timeUntilNextShot = weapon.config.fireRate

        // Recoil kick impulse
        val recoilRad = MathUtils.degreesToRadians * (aimAngleDegrees + 180f)
        val recoilForce = Vector2(MathUtils.cos(recoilRad), MathUtils.sin(recoilRad))
            .scl(weapon.config.recoilAngle * 0.15f * body.mass)
        body.applyLinearImpulse(recoilForce, body.worldCenter, true)

        return weapon.calculateTrajectoryAngle(aimAngleDegrees)
    }

    fun switchWeapon() {
        if (secondaryWeapon != null) {
            activeWeaponSlot = if (activeWeaponSlot == 0) 1 else 0
        }
    }

    fun reload(): Boolean = equippedWeapon.startReload()

    fun pickupWeapon(weaponId: String) {
        val config = WeaponDefinitions.get(weaponId)
        if (secondaryWeapon == null && config.canDualWield) {
            secondaryWeapon = Weapon(config)
            activeWeaponSlot = 1
        } else {
            // Replace currently equipped weapon
            if (activeWeaponSlot == 0) {
                primaryWeapon = Weapon(config)
            } else {
                secondaryWeapon = Weapon(config)
            }
        }
    }

    fun pickupHealth(amount: Float = 50f) {
        health = (health + amount).coerceAtMost(GameConstants.PLAYER_MAX_HEALTH)
    }

    fun pickupBoost(amount: Float = 100f) {
        boostFuel = (boostFuel + amount).coerceAtMost(GameConstants.PLAYER_MAX_BOOST)
    }

    fun takeDamage(amount: Float, attackerId: String? = null): Boolean {
        if (isDead || spawnShieldTimer > 0f) return false

        health -= amount
        if (health <= 0f) {
            health = 0f
            isDead = true
            deaths++
            respawnTimer = GameConstants.PLAYER_RESPAWN_DELAY
            return true
        }
        return false
    }

    fun respawn(spawnPos: Vector2) {
        body.setTransform(spawnPos, 0f)
        body.linearVelocity = Vector2.Zero
        health = GameConstants.PLAYER_MAX_HEALTH
        boostFuel = GameConstants.PLAYER_MAX_BOOST
        isDead = false
        respawnTimer = 0f
        spawnShieldTimer = GameConstants.PLAYER_SPAWN_SHIELD_DURATION
        primaryWeapon.currentClip = primaryWeapon.config.clipSize
    }

    fun render(shapeRenderer: ShapeRenderer) {
        if (isDead) return
        val pos = body.position

        // Spawn Shield Visual (pulsing circle)
        if (spawnShieldTimer > 0f) {
            val alpha = (0.4f + 0.3f * MathUtils.sin(spawnShieldTimer * 8f)).coerceIn(0f, 1f)
            shapeRenderer.setColor(0f, 0.8f, 1f, alpha)
            shapeRenderer.circle(pos.x, pos.y + 0.2f, GameConstants.PLAYER_RADIUS * 1.5f, 16)
        }

        // Team Color
        val teamColor = when (team) {
            1 -> Color.ROYAL
            2 -> Color.CORAL
            else -> if (isLocal) Color.FOREST else Color.GRAY
        }

        // Torso / Body
        shapeRenderer.setColor(teamColor)
        shapeRenderer.circle(pos.x, pos.y + 0.2f, GameConstants.PLAYER_RADIUS, 14)

        // Head
        shapeRenderer.setColor(0.95f, 0.8f, 0.7f, 1.0f) // Skin tone
        shapeRenderer.circle(pos.x + (if (facingRight) 0.15f else -0.15f), pos.y + 0.65f, 0.35f, 12)

        // Army Helmet
        shapeRenderer.setColor(0.2f, 0.35f, 0.15f, 1.0f)
        shapeRenderer.circle(pos.x + (if (facingRight) 0.15f else -0.15f), pos.y + 0.85f, 0.38f, 12)

        // Jetpack unit on back
        shapeRenderer.setColor(Color.DARK_GRAY)
        val jetpackX = pos.x + (if (facingRight) -0.45f else 0.45f)
        shapeRenderer.rect(jetpackX - 0.12f, pos.y, 0.24f, 0.65f)

        // Jetpack flame when thrusting
        if (isThrusting) {
            shapeRenderer.setColor(Color.ORANGE)
            shapeRenderer.triangle(
                jetpackX - 0.15f, pos.y,
                jetpackX + 0.15f, pos.y,
                jetpackX + MathUtils.random(-0.08f, 0.08f), pos.y - MathUtils.random(0.4f, 0.7f)
            )
            shapeRenderer.setColor(Color.YELLOW)
            shapeRenderer.triangle(
                jetpackX - 0.08f, pos.y,
                jetpackX + 0.08f, pos.y,
                jetpackX, pos.y - MathUtils.random(0.2f, 0.4f)
            )
        }

        // Weapon Arm aiming
        val aimRad = MathUtils.degreesToRadians * aimAngleDegrees
        val gunLength = 0.9f
        val muzzleX = pos.x + MathUtils.cos(aimRad) * gunLength
        val muzzleY = pos.y + 0.2f + MathUtils.sin(aimRad) * gunLength

        shapeRenderer.setColor(Color.BLACK)
        shapeRenderer.rectLine(pos.x, pos.y + 0.2f, muzzleX, muzzleY, 0.12f)

        // Aim laser pointer for local player
        if (isLocal) {
            shapeRenderer.setColor(1f, 0f, 0f, 0.35f)
            val laserEnd = pos.cpy().add(Vector2(MathUtils.cos(aimRad), MathUtils.sin(aimRad)).scl(12f))
            shapeRenderer.line(muzzleX, muzzleY, laserEnd.x, laserEnd.y)
        }
    }
}
