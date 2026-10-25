package com.vinay.minimilitia

import com.vinay.minimilitia.constants.GameConstants
import com.vinay.minimilitia.net.*
import com.vinay.minimilitia.weapons.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class GameplayLogicTest {

    @Test
    fun testAllWeaponsRegisteredAndBalanced() {
        assertEquals("Should have all 24 Mini Militia weapons defined", 24, WeaponDefinitions.ALL_WEAPONS.size)

        for ((id, config) in WeaponDefinitions.ALL_WEAPONS) {
            assertEquals(id, config.id)
            assertTrue("Weapon ${config.name} damage should be positive", config.damage > 0)
            assertTrue("Weapon ${config.name} fireRate should be positive", config.fireRate > 0)
            assertTrue("Weapon ${config.name} clipSize should be positive", config.clipSize > 0)
            assertTrue("Weapon ${config.name} reloadTime should be positive", config.reloadTime > 0)
            if (config.category != WeaponCategory.MELEE) {
                assertTrue("Ranged weapon ${config.name} bulletSpeed should be positive", config.bulletSpeed > 0)
            }
            assertNotNull("Weapon ${config.name} projectileType must not be null", config.projectileType)
        }
    }

    @Test
    fun testWeaponFiringAndReloading() {
        val ak47Config = WeaponDefinitions.ALL_WEAPONS["ak47"]!!
        val weapon = Weapon(ak47Config)

        assertEquals(ak47Config.clipSize, weapon.currentClip)
        assertEquals(ak47Config.maxReserveAmmo, weapon.reserveAmmo)
        assertFalse(weapon.isReloading)
        assertTrue(weapon.canFire)

        // Deplete one bullet
        weapon.currentClip -= 1
        assertEquals(ak47Config.clipSize - 1, weapon.currentClip)

        // Start reload
        val started = weapon.startReload()
        assertTrue("Reload should start when clip is not full", started)
        assertTrue(weapon.isReloading)
        assertFalse("Cannot fire while reloading", weapon.canFire)

        // Advance reload time partially
        weapon.update(ak47Config.reloadTime / 2f)
        assertTrue("Still reloading halfway through", weapon.isReloading)

        // Finish reload
        weapon.update(ak47Config.reloadTime / 2f + 0.1f)
        assertFalse("Reload should complete", weapon.isReloading)
        assertEquals(ak47Config.clipSize, weapon.currentClip)
        assertEquals(ak47Config.maxReserveAmmo - 1, weapon.reserveAmmo)
    }

    @Test
    fun testShotgunPelletCount() {
        val shotgunConfig = WeaponDefinitions.ALL_WEAPONS["shotgun"]!!
        assertEquals("Shotgun fires 8 spread pellets per shot", 8, shotgunConfig.pelletsPerShot)
        assertEquals(ProjectileType.PELLET, shotgunConfig.projectileType)
        assertTrue(shotgunConfig.spreadAngle > 10f)
    }

    @Test
    fun testRocketLauncherProperties() {
        val rocketConfig = WeaponDefinitions.ALL_WEAPONS["rocket"]!!
        assertEquals(ProjectileType.ROCKET, rocketConfig.projectileType)
        assertEquals(1, rocketConfig.clipSize)
        assertTrue(rocketConfig.damage >= 140f)
        assertEquals(WeaponCategory.HEAVY_SPECIAL, rocketConfig.category)
    }

    @Test
    fun testNetworkPlayerNetStateSerialization() {
        val state = PlayerNetState(
            id = "player-123",
            name = "Sarge",
            team = 1,
            x = 12.5f,
            y = 24.0f,
            vx = 4.2f,
            vy = -1.1f,
            aimAngle = 45.0f,
            isThrusting = true,
            isFiring = true,
            health = 85.0f,
            equippedWeapon = "ak47",
            kills = 3,
            deaths = 1,
            isDead = false
        )
        val jsonStr = Json.encodeToString(state)
        val decoded = Json.decodeFromString<PlayerNetState>(jsonStr)

        assertEquals(state.id, decoded.id)
        assertEquals(state.name, decoded.name)
        assertEquals(state.team, decoded.team)
        assertEquals(state.x, decoded.x, 0.001f)
        assertEquals(state.y, decoded.y, 0.001f)
        assertEquals(state.aimAngle, decoded.aimAngle, 0.001f)
        assertEquals(state.isThrusting, decoded.isThrusting)
        assertEquals(state.isFiring, decoded.isFiring)
        assertEquals(state.health, decoded.health, 0.001f)
        assertEquals(state.equippedWeapon, decoded.equippedWeapon)
        assertEquals(state.kills, decoded.kills)
    }

    @Test
    fun testGameMessageSerialization() {
        val hitMsg: GameMessage = GameMessage.HitReport(
            victimId = "player-456",
            damage = 45f,
            weaponId = "sniper"
        )
        val jsonHit = Json.encodeToString(hitMsg)
        val decodedHit = Json.decodeFromString<GameMessage>(jsonHit) as GameMessage.HitReport

        assertEquals("player-456", decodedHit.victimId)
        assertEquals(45f, decodedHit.damage, 0.001f)
        assertEquals("sniper", decodedHit.weaponId)

        val fireMsg: GameMessage = GameMessage.FireEvent(
            weaponId = "m4",
            originX = 10f,
            originY = 20f,
            angle = 90f
        )
        val jsonFire = Json.encodeToString(fireMsg)
        val decodedFire = Json.decodeFromString<GameMessage>(jsonFire) as GameMessage.FireEvent

        assertEquals("m4", decodedFire.weaponId)
        assertEquals(10f, decodedFire.originX, 0.001f)
        assertEquals(90f, decodedFire.angle, 0.001f)
    }

    @Test
    fun testPhysicsConstantsIntegrity() {
        assertEquals(32f, GameConstants.PPM, 0.0001f)
        assertEquals(-22.0f, GameConstants.GRAVITY_Y, 0.0001f)
        assertTrue("Category bits should be distinct power-of-2 masks",
            (GameConstants.CATEGORY_TERRAIN.toInt() and GameConstants.CATEGORY_PLAYER.toInt()) == 0)
        assertTrue("Category bits should be distinct power-of-2 masks",
            (GameConstants.CATEGORY_PLAYER.toInt() and GameConstants.CATEGORY_PROJECTILE.toInt()) == 0)
        assertTrue("Category bits should be distinct power-of-2 masks",
            (GameConstants.CATEGORY_PROJECTILE.toInt() and GameConstants.CATEGORY_PICKUP.toInt()) == 0)
    }
}
