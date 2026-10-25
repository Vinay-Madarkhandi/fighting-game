package com.vinay.minimilitia.weapons

import com.badlogic.gdx.math.MathUtils

class Weapon(val config: WeaponConfig) {
    var currentClip: Int = config.clipSize
    var reserveAmmo: Int = config.maxReserveAmmo
    var timeUntilNextShot: Float = 0f
    var reloadTimer: Float = 0f
    var isReloading: Boolean = false
    var burstShotsRemaining: Int = 0
    var burstTimer: Float = 0f

    val isClipEmpty: Boolean get() = currentClip <= 0
    val canFire: Boolean get() = !isReloading && timeUntilNextShot <= 0f && currentClip > 0

    fun update(delta: Float) {
        if (timeUntilNextShot > 0f) {
            timeUntilNextShot -= delta
        }

        if (burstShotsRemaining > 0) {
            burstTimer -= delta
        }

        if (isReloading) {
            reloadTimer -= delta
            if (reloadTimer <= 0f) {
                finishReload()
            }
        }
    }

    fun startReload(): Boolean {
        if (isReloading || currentClip >= config.clipSize || reserveAmmo <= 0) {
            return false
        }
        isReloading = true
        reloadTimer = config.reloadTime
        return true
    }

    private fun finishReload() {
        val needed = config.clipSize - currentClip
        val toAdd = needed.coerceAtMost(reserveAmmo)
        currentClip += toAdd
        reserveAmmo -= toAdd
        isReloading = false
    }

    fun addAmmo(amount: Int) {
        reserveAmmo = (reserveAmmo + amount).coerceAtMost(config.maxReserveAmmo)
    }

    /**
     * Calculates the actual trajectory angle in degrees including spread and recoil
     */
    fun calculateTrajectoryAngle(baseAimAngleDegrees: Float): Float {
        val halfSpread = config.spreadAngle / 2f
        val randomSpread = MathUtils.random(-halfSpread, halfSpread)
        return (baseAimAngleDegrees + randomSpread).toFloat()
    }
}
