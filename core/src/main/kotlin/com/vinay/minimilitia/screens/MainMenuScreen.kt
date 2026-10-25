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
import com.vinay.minimilitia.net.LocalServer
import com.vinay.minimilitia.net.NetworkManager

data class MenuButton(
    val id: String,
    val text: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val color: Color,
    val action: () -> Unit
)

class MainMenuScreen(val game: MiniMilitiaGame) : Screen {
    private val camera = OrthographicCamera()
    private val viewport = ExtendViewport(GameConstants.VIRTUAL_WIDTH, GameConstants.VIRTUAL_HEIGHT, camera)
    private val shapeRenderer = ShapeRenderer()
    private val batch = SpriteBatch()
    private val font = BitmapFont()
    private val titleFont = BitmapFont()

    private val buttons = mutableListOf<MenuButton>()
    private var selectedMap = "maps/1outpost_new.tmx"
    private var mapNameDisplay = "OUTPOST"

    init {
        font.data.setScale(1.2f)
        titleFont.data.setScale(2.2f)

        val btnW = 280f
        val btnH = 46f
        val startX = GameConstants.VIRTUAL_WIDTH / 2f - btnW / 2f
        var startY = GameConstants.VIRTUAL_HEIGHT * 0.58f
        val spacing = 58f

        // 1. Solo Practice
        buttons.add(MenuButton("solo", "PRACTICE (SOLO)", startX, startY, btnW, btnH, Color(0.2f, 0.65f, 0.2f, 0.9f)) {
            game.audioManager.play("readyup.wav")
            game.screen = PlayScreen(game, mapPath = selectedMap, isSoloPractice = true)
        })

        // 2. Host LAN Game
        startY -= spacing
        buttons.add(MenuButton("host", "HOST MULTIPLAYER", startX, startY, btnW, btnH, Color(0.2f, 0.5f, 0.85f, 0.9f)) {
            game.audioManager.play("readyup.wav")
            val server = LocalServer(8080, "outpost")
            server.start()
            val netClient = NetworkManager()
            netClient.connect("127.0.0.1", 8080, "Host")
            game.screen = PlayScreen(game, mapPath = selectedMap, isSoloPractice = false, networkManager = netClient, isHostingServer = true, botCount = 1)
        })

        // 3. Join LAN Game
        startY -= spacing
        buttons.add(MenuButton("join", "JOIN LAN / ONLINE", startX, startY, btnW, btnH, Color(0.7f, 0.4f, 0.15f, 0.9f)) {
            game.audioManager.play("readyup.wav")
            val netClient = NetworkManager()
            netClient.connect("127.0.0.1", 8080, "Cadet")
            game.screen = PlayScreen(game, mapPath = selectedMap, isSoloPractice = false, networkManager = netClient, isHostingServer = false, botCount = 0)
        })

        // 4. Armory / Weapon Stats
        startY -= spacing
        buttons.add(MenuButton("armory", "ARMORY & WEAPONS", startX, startY, btnW, btnH, Color(0.55f, 0.2f, 0.7f, 0.9f)) {
            game.audioManager.play("pickup.wav")
            game.screen = ArmoryScreen(game)
        })

        // 5. Map Toggle
        startY -= spacing
        buttons.add(MenuButton("map", "MAP: $mapNameDisplay", startX, startY, btnW, btnH, Color(0.3f, 0.3f, 0.3f, 0.9f)) {
            toggleMap()
        })
    }

    private fun toggleMap() {
        if (selectedMap.contains("outpost")) {
            selectedMap = "maps/11catacombs_new.tmx"
            mapNameDisplay = "CATACOMBS"
        } else if (selectedMap.contains("catacombs")) {
            selectedMap = "maps/ambush.tmx"
            mapNameDisplay = "AMBUSH"
        } else {
            selectedMap = "maps/1outpost_new.tmx"
            mapNameDisplay = "OUTPOST"
        }
        val mapBtnIdx = buttons.indexOfFirst { it.id == "map" }
        if (mapBtnIdx != -1) {
            val old = buttons[mapBtnIdx]
            buttons[mapBtnIdx] = old.copy(text = "MAP: $mapNameDisplay")
        }
        game.audioManager.play("switch.wav")
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.08f, 0.12f, 0.16f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // Process button clicks
        if (Gdx.input.justTouched()) {
            val touchPos = viewport.unproject(Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat()))
            val touchX = touchPos.x
            val touchY = touchPos.y
            for (btn in buttons) {
                if (touchX >= btn.x && touchX <= btn.x + btn.width &&
                    touchY >= btn.y && touchY <= btn.y + btn.height) {
                    btn.action()
                    break
                }
            }
        }

        // 1. Render Background & Button Panels
        shapeRenderer.projectionMatrix = camera.combined
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Title backdrop banner
        shapeRenderer.setColor(0.12f, 0.18f, 0.24f, 0.85f)
        shapeRenderer.rect(GameConstants.VIRTUAL_WIDTH / 2f - 240f, GameConstants.VIRTUAL_HEIGHT * 0.76f, 480f, 65f)

        // Buttons
        for (btn in buttons) {
            // Shadow
            shapeRenderer.setColor(0f, 0f, 0f, 0.4f)
            shapeRenderer.rect(btn.x + 3f, btn.y - 3f, btn.width, btn.height)

            // Button body
            shapeRenderer.setColor(btn.color)
            shapeRenderer.rect(btn.x, btn.y, btn.width, btn.height)

            // Outline highlight
            shapeRenderer.setColor(1f, 1f, 1f, 0.35f)
            shapeRenderer.rect(btn.x, btn.y + btn.height - 3f, btn.width, 3f)
        }

        shapeRenderer.end()

        // 2. Render Text
        batch.projectionMatrix = camera.combined
        batch.begin()

        // Title
        titleFont.color = Color.GOLD
        titleFont.draw(batch, "MINI MILITIA", GameConstants.VIRTUAL_WIDTH / 2f - 145f, GameConstants.VIRTUAL_HEIGHT * 0.84f)

        // Subtitle
        font.color = Color.LIGHT_GRAY
        font.draw(batch, "DOODLE ARMY 2 - REPLICA", GameConstants.VIRTUAL_WIDTH / 2f - 120f, GameConstants.VIRTUAL_HEIGHT * 0.74f)

        // Button texts
        font.color = Color.WHITE
        for (btn in buttons) {
            val textX = btn.x + 25f
            val textY = btn.y + btn.height * 0.65f
            font.draw(batch, btn.text, textX, textY)
        }

        // Version & instructions
        font.data.setScale(0.85f)
        font.color = Color.DARK_GRAY
        font.draw(batch, "Controls: Dual-Stick Touch on Android | WASD + Mouse on Desktop", 20f, 30f)
        font.data.setScale(1.2f)

        batch.end()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun show() {
        game.audioManager.playVoiceLine("welcome.wav")
    }

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
