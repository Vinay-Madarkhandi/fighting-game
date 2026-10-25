package com.vinay.minimilitia.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.vinay.minimilitia.MiniMilitiaGame
import com.vinay.minimilitia.constants.GameConstants
import com.vinay.minimilitia.weapons.WeaponConfig
import com.vinay.minimilitia.weapons.WeaponDefinitions

class ArmoryScreen(val game: MiniMilitiaGame) : Screen {
    private val camera = OrthographicCamera()
    private val viewport = ExtendViewport(GameConstants.VIRTUAL_WIDTH, GameConstants.VIRTUAL_HEIGHT, camera)
    private val shapeRenderer = ShapeRenderer()
    private val batch = SpriteBatch()
    private val font = BitmapFont()
    private val titleFont = BitmapFont()

    private val weaponList: List<WeaponConfig> = WeaponDefinitions.ALL_WEAPONS.values.toList()
    private var selectedIndex = 0

    init {
        font.data.setScale(1.1f)
        titleFont.data.setScale(1.8f)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.08f, 0.10f, 0.14f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // Navigation input
        if (Gdx.input.justTouched()) {
            val touchPos = viewport.unproject(Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat()))
            val touchX = touchPos.x
            val touchY = touchPos.y

            // Back button
            if (touchX < 140f && touchY > GameConstants.VIRTUAL_HEIGHT - 60f) {
                game.audioManager.play("back.wav")
                game.screen = MainMenuScreen(game)
                return
            }

            // Previous Weapon
            if (touchX < 100f && touchY in 150f..350f) {
                selectedIndex = if (selectedIndex > 0) selectedIndex - 1 else weaponList.size - 1
                playSelectedWeaponSound()
            }

            // Next Weapon
            if (touchX > GameConstants.VIRTUAL_WIDTH - 100f && touchY in 150f..350f) {
                selectedIndex = (selectedIndex + 1) % weaponList.size
                playSelectedWeaponSound()
            }

            // Test Fire Button
            if (touchX in (GameConstants.VIRTUAL_WIDTH / 2f - 90f)..(GameConstants.VIRTUAL_WIDTH / 2f + 90f) && touchY in 50f..100f) {
                playSelectedWeaponSound()
            }
        }

        val weapon = weaponList[selectedIndex]

        // 1. Render Background & Stat Panels
        shapeRenderer.projectionMatrix = camera.combined
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Top bar
        shapeRenderer.setColor(0.12f, 0.18f, 0.24f, 0.9f)
        shapeRenderer.rect(0f, GameConstants.VIRTUAL_HEIGHT - 60f, GameConstants.VIRTUAL_WIDTH, 60f)

        // Back button
        shapeRenderer.setColor(0.8f, 0.2f, 0.2f, 0.9f)
        shapeRenderer.rect(20f, GameConstants.VIRTUAL_HEIGHT - 50f, 90f, 40f)

        // Central weapon display panel
        shapeRenderer.setColor(0.15f, 0.22f, 0.30f, 0.85f)
        val panelX = 120f
        val panelY = 110f
        val panelW = GameConstants.VIRTUAL_WIDTH - 240f
        val panelH = 340f
        shapeRenderer.rect(panelX, panelY, panelW, panelH)

        // Weapon silhouette preview placeholder
        shapeRenderer.setColor(Color.BLACK)
        shapeRenderer.rect(GameConstants.VIRTUAL_WIDTH / 2f - 100f, 320f, 200f, 45f)
        shapeRenderer.setColor(Color.GOLD)
        shapeRenderer.rect(GameConstants.VIRTUAL_WIDTH / 2f - 70f, 335f, 140f, 15f)

        // Navigation arrow buttons (< and >)
        shapeRenderer.setColor(0.3f, 0.6f, 0.9f, 0.7f)
        shapeRenderer.triangle(40f, 250f, 80f, 290f, 80f, 210f)
        shapeRenderer.triangle(GameConstants.VIRTUAL_WIDTH - 40f, 250f, GameConstants.VIRTUAL_WIDTH - 80f, 290f, GameConstants.VIRTUAL_WIDTH - 80f, 210f)

        // Test Fire button
        shapeRenderer.setColor(0.9f, 0.45f, 0.1f, 0.9f)
        shapeRenderer.rect(GameConstants.VIRTUAL_WIDTH / 2f - 80f, 50f, 160f, 42f)

        shapeRenderer.end()

        // 2. Render Text
        batch.projectionMatrix = camera.combined
        batch.begin()

        // Title
        titleFont.color = Color.GOLD
        titleFont.draw(batch, "ARMORY: ${weapon.name}", 130f, GameConstants.VIRTUAL_HEIGHT - 22f)

        font.color = Color.WHITE
        font.draw(batch, "< BACK", 35f, GameConstants.VIRTUAL_HEIGHT - 25f)

        // Weapon Stats List
        val statX = panelX + 50f
        var statY = 280f
        val lineSpacing = 28f

        font.color = Color.CYAN
        font.draw(batch, "CATEGORY: ${weapon.category}", statX, statY)
        statY -= lineSpacing

        font.color = Color.WHITE
        font.draw(batch, "POWER / DAMAGE: ${weapon.damage}", statX, statY)
        statY -= lineSpacing

        font.draw(batch, "FIRE RATE: ${weapon.fireRate}s per round", statX, statY)
        statY -= lineSpacing

        font.draw(batch, "CLIP CAPACITY: ${weapon.clipSize} rounds", statX, statY)
        statY -= lineSpacing

        font.draw(batch, "RELOAD DURATION: ${weapon.reloadTime}s", statX, statY)
        statY -= lineSpacing

        font.draw(batch, "BULLET SPEED: ${weapon.bulletSpeed} m/s", statX, statY)
        statY -= lineSpacing

        font.draw(batch, "RECOIL ANGLE: ${weapon.recoilAngle}°", statX, statY)
        statY -= lineSpacing

        val dualWieldText = if (weapon.canDualWield) "YES (Dual Wield Supported!)" else "NO (Single Handed/Two-Handed)"
        font.color = if (weapon.canDualWield) Color.GREEN else Color.LIGHT_GRAY
        font.draw(batch, "DUAL WIELD: $dualWieldText", statX, statY)

        // Test fire button text
        font.color = Color.WHITE
        font.draw(batch, "TEST FIRE SFX", GameConstants.VIRTUAL_WIDTH / 2f - 52f, 76f)

        // Index counter
        font.color = Color.LIGHT_GRAY
        font.draw(batch, "${selectedIndex + 1} / ${weaponList.size}", GameConstants.VIRTUAL_WIDTH / 2f - 20f, 135f)

        batch.end()
    }

    private fun playSelectedWeaponSound() {
        val weapon = weaponList[selectedIndex]
        game.audioManager.play(weapon.soundEffect)
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun show() {}
    override fun pause() {}
    override fun resume() {}
    override fun hide() {}

    override fun dispose() {
        shapeRenderer.dispose()
        batch.dispose()
        font.dispose()
        titleFont.dispose()
    }
}
