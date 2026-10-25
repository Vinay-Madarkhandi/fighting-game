package com.vinay.minimilitia.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.vinay.minimilitia.constants.GameConstants
import com.vinay.minimilitia.entities.Player

class InGameHUD(
    val virtualWidth: Float = GameConstants.VIRTUAL_WIDTH,
    val virtualHeight: Float = GameConstants.VIRTUAL_HEIGHT
) {
    private val font = BitmapFont()
    private val killFeed = mutableListOf<Pair<String, Float>>() // Message to remaining time

    init {
        font.data.setScale(1.1f)
    }

    fun addKillMessage(killer: String, victim: String, weapon: String) {
        killFeed.add("$killer killed $victim with $weapon" to 4.0f)
        if (killFeed.size > 4) killFeed.removeAt(0)
    }

    fun update(delta: Float) {
        val iterator = killFeed.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            val newTime = item.second - delta
            if (newTime <= 0f) {
                iterator.remove()
            }
        }
    }

    fun render(shapeRenderer: ShapeRenderer, batch: SpriteBatch, player: Player, matchTimeSeconds: Float) {
        // Render HUD Bars with ShapeRenderer
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // 1. Health Bar (Top-Left)
        val barX = 25f
        val barY = virtualHeight - 35f
        val barWidth = 180f
        val barHeight = 16f

        // Background
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.8f)
        shapeRenderer.rect(barX - 2f, barY - 2f, barWidth + 4f, barHeight + 4f)

        // Health Fill
        val healthPct = (player.health / GameConstants.PLAYER_MAX_HEALTH).coerceIn(0f, 1f)
        if (healthPct > 0.35f) {
            shapeRenderer.setColor(Color.FOREST)
        } else {
            shapeRenderer.setColor(Color.RED)
        }
        shapeRenderer.rect(barX, barY, barWidth * healthPct, barHeight)

        // 2. Jetpack Boost Meter (Below Health Bar)
        val boostY = barY - 22f
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.8f)
        shapeRenderer.rect(barX - 2f, boostY - 2f, barWidth + 4f, barHeight + 4f)

        // Boost Fill
        val boostPct = (player.boostFuel / GameConstants.PLAYER_MAX_BOOST).coerceIn(0f, 1f)
        shapeRenderer.setColor(0.1f, 0.65f, 1.0f, 0.9f)
        shapeRenderer.rect(barX, boostY, barWidth * boostPct, barHeight)

        // 3. Match Timer Panel (Top Center)
        val timerW = 120f
        val timerX = virtualWidth / 2f - timerW / 2f
        val timerY = virtualHeight - 35f
        shapeRenderer.setColor(0f, 0f, 0f, 0.6f)
        shapeRenderer.rect(timerX, timerY, timerW, 25f)

        // 4. Weapon Panel Background (Top Right)
        val weaponW = 200f
        val weaponX = virtualWidth - weaponW - 25f
        val weaponY = virtualHeight - 45f
        shapeRenderer.setColor(0f, 0f, 0f, 0.6f)
        shapeRenderer.rect(weaponX, weaponY, weaponW, 35f)

        shapeRenderer.end()

        // Render Text with SpriteBatch
        batch.begin()

        // Health & Boost text
        font.color = Color.WHITE
        font.draw(batch, "HEALTH: ${player.health.toInt()}", barX + 6f, barY + 13f)
        font.draw(batch, "BOOST: ${player.boostFuel.toInt()}%", barX + 6f, boostY + 13f)

        // Match Time
        val minutes = (matchTimeSeconds / 60).toInt()
        val seconds = (matchTimeSeconds % 60).toInt()
        val timeStr = String.format("%02d:%02d", minutes, seconds)
        font.color = Color.GOLD
        font.draw(batch, timeStr, timerX + 38f, timerY + 18f)

        // Kills / Deaths
        font.color = Color.WHITE
        font.draw(batch, "K: ${player.kills}  D: ${player.deaths}", timerX + 25f, timerY - 6f)

        // Equipped Weapon & Ammo
        val weapon = player.equippedWeapon
        val ammoStr = "${weapon.config.name}: ${weapon.currentClip} / ${weapon.reserveAmmo}"
        font.color = if (weapon.isReloading) Color.YELLOW else Color.WHITE
        val status = if (weapon.isReloading) " (RELOADING...)" else ""
        font.draw(batch, ammoStr + status, weaponX + 10f, weaponY + 24f)

        // Grenades
        font.color = Color.GREEN
        font.draw(batch, "GRENADES: ${player.grenadeCount}", weaponX + 10f, weaponY - 6f)

        // Kill Feed (Top Right, below weapon panel)
        var feedY = weaponY - 30f
        for ((msg, _) in killFeed) {
            font.color = Color.ORANGE
            font.draw(batch, msg, weaponX - 40f, feedY)
            feedY -= 18f
        }

        // Respawn Overlay if dead
        if (player.isDead) {
            font.color = Color.RED
            font.data.setScale(1.6f)
            font.draw(batch, "YOU WERE KILLED", virtualWidth / 2f - 110f, virtualHeight / 2f + 30f)
            font.color = Color.WHITE
            font.data.setScale(1.2f)
            font.draw(batch, "Respawning in ${player.respawnTimer.toInt() + 1}s...", virtualWidth / 2f - 90f, virtualHeight / 2f - 10f)
            font.data.setScale(1.1f)
        }

        batch.end()
    }

    fun dispose() {
        font.dispose()
    }
}
