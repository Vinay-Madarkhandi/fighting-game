package com.vinay.minimilitia.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.vinay.minimilitia.MiniMilitiaGame
import com.vinay.minimilitia.audio.AudioManager
import com.vinay.minimilitia.constants.GameConstants
import com.vinay.minimilitia.entities.*
import com.vinay.minimilitia.maps.GameMap
import com.vinay.minimilitia.net.*
import com.vinay.minimilitia.physics.ContactCallback
import com.vinay.minimilitia.physics.PhysicsWorld
import com.vinay.minimilitia.ui.InGameHUD
import com.vinay.minimilitia.ui.TouchControls
import com.vinay.minimilitia.weapons.ProjectileType
import com.vinay.minimilitia.weapons.WeaponDefinitions
import java.util.UUID

class PlayScreen(
    val game: MiniMilitiaGame,
    val mapPath: String = "maps/1outpost_new.tmx",
    val isSoloPractice: Boolean = true,
    val networkManager: NetworkManager? = null,
    val isHostingServer: Boolean = false,
    val botCount: Int = 3
) : Screen, ContactCallback {

    private val camera: OrthographicCamera = OrthographicCamera()
    private val viewport: ExtendViewport = ExtendViewport(
        GameConstants.VIRTUAL_WIDTH / GameConstants.PPM,
        GameConstants.VIRTUAL_HEIGHT / GameConstants.PPM,
        camera
    )

    private val uiCamera: OrthographicCamera = OrthographicCamera()
    private val uiViewport: ExtendViewport = ExtendViewport(
        GameConstants.VIRTUAL_WIDTH,
        GameConstants.VIRTUAL_HEIGHT,
        uiCamera
    )

    private val shapeRenderer: ShapeRenderer = ShapeRenderer()
    private val batch: SpriteBatch = SpriteBatch()

    private val physicsWorld: PhysicsWorld = PhysicsWorld(this)
    private val gameMap: GameMap
    private val audioManager: AudioManager = game.audioManager

    // Entities
    val localPlayer: Player
    val remotePlayers = mutableMapOf<String, Player>()
    val bots = mutableListOf<BotAI>()
    val projectiles = mutableListOf<Projectile>()
    val pickups = mutableListOf<WeaponPickup>()

    // UI & Controls
    private val touchControls: TouchControls = TouchControls(GameConstants.VIRTUAL_WIDTH, GameConstants.VIRTUAL_HEIGHT)
    private val hud: InGameHUD = InGameHUD(GameConstants.VIRTUAL_WIDTH, GameConstants.VIRTUAL_HEIGHT)

    private var matchTimer = 300f // 5 minute match

    init {
        // Initialize Map
        gameMap = GameMap(mapPath, physicsWorld.b2World)

        // Initialize Local Player at random spawn
        val spawnPos = gameMap.getRandomSpawn()
        localPlayer = Player("local", "Player", true, physicsWorld.b2World, spawnPos)

        // Spawn Map Weapon Pickups
        for ((idx, wspawn) in gameMap.weaponSpawns.withIndex()) {
            val weaponToSpawn = wspawn.allowedWeapons.randomOrNull() ?: "ak47"
            val pickup = WeaponPickup("pickup_$idx", weaponToSpawn, wspawn.position, physicsWorld.b2World)
            pickups.add(pickup)
        }

        // Spawn Bots if in Solo Practice Mode
        if (isSoloPractice) {
            val botNames = listOf("Sarge", "Cadet", "Sniper", "Gunner", "Commando")
            for (i in 0 until botCount) {
                val bPos = gameMap.getRandomSpawn()
                val botP = Player("bot_$i", botNames.getOrElse(i) { "Bot $i" }, false, physicsWorld.b2World, bPos)
                val botWeapons = listOf("ak47", "shotgun", "uzi", "m16", "mp5")
                botP.pickupWeapon(botWeapons[i % botWeapons.size])
                remotePlayers[botP.id] = botP

                val waypoints = gameMap.playerSpawns.map { it.position }
                bots.add(BotAI(botP, waypoints))
            }
        }
    }

    override fun render(delta: Float) {
        val dt = delta.coerceAtMost(0.1f)
        update(dt)

        // Clear screen with sky blue color
        Gdx.gl.glClearColor(0.2f, 0.45f, 0.7f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // 1. Render TMX Map layers
        gameMap.render(camera)

        // 2. Render Box2D World entities (Pickups, Projectiles, Players)
        shapeRenderer.projectionMatrix = camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Render Pickups
        for (pickup in pickups) {
            pickup.render(shapeRenderer)
        }

        // Render Remote Players & Bots
        for (player in remotePlayers.values) {
            player.render(shapeRenderer)
        }

        // Render Local Player
        localPlayer.render(shapeRenderer)

        // Render Projectiles
        for (proj in projectiles) {
            proj.render(shapeRenderer)
        }

        shapeRenderer.end()

        // 3. Render In-Game HUD and Touch Controls in UI coordinates
        shapeRenderer.projectionMatrix = uiCamera.combined
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        touchControls.render(shapeRenderer)
        shapeRenderer.end()

        // Render HUD text and bars
        batch.projectionMatrix = uiCamera.combined
        hud.render(shapeRenderer, batch, localPlayer, matchTimer)
    }

    private fun update(delta: Float) {
        matchTimer = (matchTimer - delta).coerceAtLeast(0f)
        hud.update(delta)

        // 1. Update Touch Controls & Desktop Mouse Aim
        touchControls.update(uiViewport)
        val mouseScreen = Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
        camera.unproject(mouseScreen)
        val playerPos = localPlayer.body.position
        touchControls.setMouseAim(
            mouseScreen.x * GameConstants.PPM,
            mouseScreen.y * GameConstants.PPM,
            playerPos.x * GameConstants.PPM,
            playerPos.y * GameConstants.PPM
        )

        // Process Action Buttons
        if (touchControls.wantsReload) {
            localPlayer.reload()
            audioManager.play("reload.wav")
        }
        if (touchControls.wantsSwap) {
            localPlayer.switchWeapon()
            audioManager.play("switch.wav")
        }
        if (touchControls.wantsGrenade && localPlayer.grenadeCount > 0) {
            throwGrenade(localPlayer)
        }
        if (touchControls.wantsMelee) {
            performMelee(localPlayer)
        }

        // 2. Update Local Player
        localPlayer.handleInput(
            touchControls.moveVector,
            touchControls.aimVector,
            touchControls.isFiring,
            touchControls.isThrusting
        )
        localPlayer.update(delta, audioManager)

        // Local Player Firing
        if (localPlayer.isFiring && localPlayer.canFire()) {
            firePlayerWeapon(localPlayer)
        }

        // Check local player respawn
        if (localPlayer.isDead && localPlayer.respawnTimer <= 0f) {
            localPlayer.respawn(gameMap.getRandomSpawn())
        }

        // 3. Update Bots
        for (bot in bots) {
            bot.update(delta, localPlayer, pickups)
            bot.botPlayer.update(delta, null)
            if (bot.botPlayer.isFiring && bot.botPlayer.canFire()) {
                firePlayerWeapon(bot.botPlayer)
            }
            if (bot.botPlayer.isDead && bot.botPlayer.respawnTimer <= 0f) {
                bot.botPlayer.respawn(gameMap.getRandomSpawn())
            }
        }

        // 4. Update Projectiles
        val projIterator = projectiles.iterator()
        while (projIterator.hasNext()) {
            val proj = projIterator.next()
            proj.update(delta)
            if (!proj.isAlive) {
                // Trigger explosion if explosive
                if (proj.type == ProjectileType.ROCKET ||
                    proj.type == ProjectileType.GRENADE_SHELL ||
                    proj.type == ProjectileType.FRAG_GRENADE
                ) {
                    triggerExplosion(proj.body.position, proj.damage, proj.blastRadius, proj.ownerId)
                }
                proj.destroy()
                projIterator.remove()
            }
        }

        // 5. Update Pickups
        for (pickup in pickups) {
            pickup.update(delta)
        }

        // 6. Step Box2D Physics Simulation
        physicsWorld.step(delta)

        // 7. Update Camera smoothly tracking local player
        updateCamera()

        // 8. Process Network Synchronization (if connected)
        processNetwork(delta)
    }

    private fun firePlayerWeapon(player: Player) {
        val weapon = player.equippedWeapon
        val finalAngle = player.onFired()
        audioManager.playSpatial(weapon.config.soundEffect, localPlayer.body.position, player.body.position)

        val pellets = weapon.config.pelletsPerShot
        val startPos = player.body.position.cpy().add(0f, 0.2f)

        for (p in 0 until pellets) {
            val pelletAngle = if (pellets > 1) {
                weapon.calculateTrajectoryAngle(player.aimAngleDegrees)
            } else {
                finalAngle
            }
            val rad = MathUtils.degreesToRadians * pelletAngle
            val dir = Vector2(MathUtils.cos(rad), MathUtils.sin(rad))

            val proj = Projectile(
                id = UUID.randomUUID().toString(),
                ownerId = player.id,
                type = weapon.config.projectileType,
                damage = weapon.config.damage,
                speed = weapon.config.bulletSpeed,
                startPos = startPos.cpy().add(dir.cpy().scl(0.8f)),
                direction = dir,
                b2World = physicsWorld.b2World,
                isBouncing = (weapon.config.projectileType == ProjectileType.SAW_BLADE || weapon.config.projectileType == ProjectileType.GRENADE_SHELL)
            )
            projectiles.add(proj)
        }

        // Send fire event over network if local
        if (player.isLocal && networkManager?.isConnected == true) {
            networkManager.sendFire(weapon.config.id, startPos, player.aimAngleDegrees)
        }
    }

    private fun throwGrenade(player: Player) {
        player.grenadeCount--
        audioManager.play("throw.wav")
        val rad = MathUtils.degreesToRadians * player.aimAngleDegrees
        val dir = Vector2(MathUtils.cos(rad), MathUtils.sin(rad))
        val startPos = player.body.position.cpy().add(0f, 0.4f)

        val grenade = Projectile(
            id = UUID.randomUUID().toString(),
            ownerId = player.id,
            type = ProjectileType.FRAG_GRENADE,
            damage = 130f,
            speed = 16f,
            startPos = startPos.add(dir.cpy().scl(0.6f)),
            direction = dir,
            b2World = physicsWorld.b2World,
            isBouncing = true,
            maxBounces = 4
        )
        projectiles.add(grenade)
    }

    private fun performMelee(player: Player) {
        audioManager.play("melee.wav")
        val meleeRange = 1.8f
        val playerPos = player.body.position

        // Check enemies within range
        val targets = if (player.isLocal) remotePlayers.values else listOf(localPlayer)
        for (target in targets) {
            if (!target.isDead && target.body.position.dst(playerPos) < meleeRange) {
                val isKilled = target.takeDamage(75f, player.id)
                audioManager.play("impact.wav")
                if (isKilled) {
                    player.kills++
                    audioManager.playDeathScream(localPlayer.body.position, target.body.position)
                    hud.addKillMessage(player.name, target.name, "Melee")
                }
            }
        }
    }

    private fun triggerExplosion(pos: Vector2, maxDamage: Float, radius: Float, attackerId: String) {
        audioManager.playSpatial("explode.wav", localPlayer.body.position, pos)

        val allPlayers = mutableListOf<Player>()
        allPlayers.add(localPlayer)
        allPlayers.addAll(remotePlayers.values)

        for (player in allPlayers) {
            if (player.isDead) continue
            val dist = player.body.position.dst(pos)
            if (dist < radius) {
                val falloff = (1f - dist / radius).coerceIn(0f, 1f)
                val damage = maxDamage * falloff
                val isKilled = player.takeDamage(damage, attackerId)

                // Impulse knockback away from explosion
                val knockbackDir = player.body.position.cpy().sub(pos).nor()
                player.body.applyLinearImpulse(knockbackDir.scl(falloff * 25f * player.body.mass), player.body.worldCenter, true)

                if (isKilled) {
                    val attacker = allPlayers.find { it.id == attackerId }
                    attacker?.kills = (attacker?.kills ?: 0) + 1
                    audioManager.playDeathScream(localPlayer.body.position, player.body.position)
                    hud.addKillMessage(attacker?.name ?: "Someone", player.name, "Explosion")
                }
            }
        }
    }

    private fun updateCamera() {
        // Smoothly interpolate camera position to player position
        val targetX = localPlayer.body.position.x
        val targetY = localPlayer.body.position.y
        camera.position.x = MathUtils.lerp(camera.position.x, targetX, 0.12f)
        camera.position.y = MathUtils.lerp(camera.position.y, targetY, 0.12f)

        // Clamp camera to map boundaries
        val halfViewW = camera.viewportWidth * camera.zoom / 2f
        val halfViewH = camera.viewportHeight * camera.zoom / 2f

        camera.position.x = camera.position.x.coerceIn(halfViewW, gameMap.mapWidthMeters - halfViewW)
        camera.position.y = camera.position.y.coerceIn(halfViewH, gameMap.mapHeightMeters - halfViewH)
        camera.update()
    }

    private fun processNetwork(delta: Float) {
        val net = networkManager ?: return
        if (!net.isConnected) return

        // Send local input / state to server at ~25 Hz
        net.sendInput(
            localPlayer.body.position,
            localPlayer.body.linearVelocity,
            localPlayer.aimAngleDegrees,
            localPlayer.isThrusting,
            localPlayer.isFiring
        )

        // Process incoming network messages
        while (!net.incomingEvents.isEmpty()) {
            val event = net.incomingEvents.poll() ?: break
            when (event) {
                is GameMessage.WorldStateSnapshot -> {
                    for (pState in event.players) {
                        if (pState.id != localPlayer.id) {
                            var remote = remotePlayers[pState.id]
                            if (remote == null) {
                                val newRemote = Player(pState.id, pState.name, false, physicsWorld.b2World, Vector2(pState.x, pState.y), pState.team)
                                remotePlayers[pState.id] = newRemote
                                remote = newRemote
                            }
                            // Smoothly interpolate position
                            remote.body.setTransform(
                                MathUtils.lerp(remote.body.position.x, pState.x, 0.4f),
                                MathUtils.lerp(remote.body.position.y, pState.y, 0.4f),
                                0f
                            )
                            remote.aimAngleDegrees = pState.aimAngle
                            remote.isThrusting = pState.isThrusting
                            remote.isFiring = pState.isFiring
                            remote.health = pState.health
                            remote.isDead = pState.isDead
                        }
                    }
                }
                is GameMessage.RemoteFire -> {
                    val shooter = remotePlayers[event.shooterId]
                    if (shooter != null) {
                        shooter.aimAngleDegrees = event.angle
                        firePlayerWeapon(shooter)
                    }
                }
                is GameMessage.DamageNotice -> {
                    val victim = if (event.victimId == localPlayer.id) localPlayer else remotePlayers[event.victimId]
                    val attacker = if (event.attackerId == localPlayer.id) localPlayer else remotePlayers[event.attackerId]
                    victim?.health = event.remainingHealth
                    if (event.isKilled) {
                        victim?.isDead = true
                        attacker?.kills = (attacker?.kills ?: 0) + 1
                        hud.addKillMessage(attacker?.name ?: "Someone", victim?.name ?: "Player", event.weaponId)
                    }
                }
                is GameMessage.PlayerDisconnected -> {
                    val removed = remotePlayers.remove(event.playerId)
                    if (removed != null) {
                        physicsWorld.b2World.destroyBody(removed.body)
                    }
                }
                else -> {}
            }
        }
    }

    // Contact Callbacks
    override fun onProjectileHitTerrain(projectileUserData: Any, hitPoint: Vector2) {
        val proj = projectileUserData as? Projectile ?: return
        proj.onHitTerrain()
        audioManager.playSpatial("ricochet.wav", localPlayer.body.position, hitPoint)
    }

    override fun onProjectileHitPlayer(projectileUserData: Any, playerUserData: Any, hitPoint: Vector2) {
        val proj = projectileUserData as? Projectile ?: return
        val playerTag = playerUserData.toString()
        val victimId = playerTag.removePrefix("player_")

        // Avoid friendly fire / self-inflicted bullet damage
        if (victimId == proj.ownerId) return

        val victim = if (victimId == localPlayer.id) localPlayer else remotePlayers[victimId] ?: return
        val isKilled = victim.takeDamage(proj.damage, proj.ownerId)
        proj.onHitPlayer()

        audioManager.playSpatial("impact2.wav", localPlayer.body.position, hitPoint)

        if (isKilled) {
            val killer = if (proj.ownerId == localPlayer.id) localPlayer else remotePlayers[proj.ownerId]
            killer?.kills = (killer?.kills ?: 0) + 1
            audioManager.playDeathScream(localPlayer.body.position, hitPoint)
            hud.addKillMessage(killer?.name ?: "Someone", victim.name, proj.type.name)
        }
    }

    override fun onPlayerPickup(playerUserData: Any, pickupUserData: Any) {
        val playerTag = playerUserData.toString()
        val playerId = playerTag.removePrefix("player_")
        val player = if (playerId == localPlayer.id) localPlayer else remotePlayers[playerId] ?: return
        val pickup = pickupUserData as? WeaponPickup ?: return

        if (!pickup.isActive) return

        if (pickup.isHealthPack) {
            player.pickupHealth(50f)
            audioManager.playSpatial("pickup.wav", localPlayer.body.position, pickup.body.position)
            pickup.collect()
        } else if (pickup.isBoostTank) {
            player.pickupBoost(100f)
            audioManager.playSpatial("pickup.wav", localPlayer.body.position, pickup.body.position)
            pickup.collect()
        } else {
            // Weapon pickup
            player.pickupWeapon(pickup.weaponId)
            audioManager.playSpatial("pickup.wav", localPlayer.body.position, pickup.body.position)
            pickup.collect()
        }
    }

    override fun onPlayerGroundContact(playerUserData: Any, isGrounded: Boolean) {
        val playerTag = playerUserData.toString()
        val playerId = playerTag.removePrefix("player_")
        val player = if (playerId == localPlayer.id) localPlayer else remotePlayers[playerId]
        player?.isGrounded = isGrounded
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
        uiViewport.update(width, height, true)
    }

    override fun show() {}
    override fun pause() {}
    override fun resume() {}
    override fun hide() {}

    override fun dispose() {
        gameMap.dispose()
        physicsWorld.dispose()
        shapeRenderer.dispose()
        batch.dispose()
        hud.dispose()
    }
}
