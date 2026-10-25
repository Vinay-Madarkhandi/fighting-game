package com.vinay.minimilitia.maps

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.objects.PolygonMapObject
import com.badlogic.gdx.maps.objects.PolylineMapObject
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.vinay.minimilitia.constants.GameConstants

data class SpawnPoint(val id: String, val position: Vector2, val team: Int = 0)
data class WeaponSpawn(val id: String, val position: Vector2, val allowedWeapons: List<String>)

class GameMap(
    val mapPath: String,
    val b2World: World
) {
    val tiledMap: TiledMap
    val renderer: OrthogonalTiledMapRenderer
    val mapWidthPixels: Float
    val mapHeightPixels: Float
    val mapWidthMeters: Float
    val mapHeightMeters: Float

    val playerSpawns = mutableListOf<SpawnPoint>()
    val team1Spawns = mutableListOf<SpawnPoint>()
    val team2Spawns = mutableListOf<SpawnPoint>()
    val weaponSpawns = mutableListOf<WeaponSpawn>()

    private val terrainBody: Body

    init {
        val loader = TmxMapLoader()
        tiledMap = loader.load(mapPath)
        renderer = OrthogonalTiledMapRenderer(tiledMap, 1f)

        val prop = tiledMap.properties
        val widthTiles = prop.get("width", Int::class.java) ?: 50
        val heightTiles = prop.get("height", Int::class.java) ?: 30
        val tileWidth = prop.get("tilewidth", Int::class.java) ?: 64
        val tileHeight = prop.get("tileheight", Int::class.java) ?: 64

        mapWidthPixels = (widthTiles * tileWidth).toFloat()
        mapHeightPixels = (heightTiles * tileHeight).toFloat()
        mapWidthMeters = mapWidthPixels / GameConstants.PPM
        mapHeightMeters = mapHeightPixels / GameConstants.PPM

        // Create static body for terrain
        val bodyDef = BodyDef().apply {
            type = BodyDef.BodyType.StaticBody
            position.set(0f, 0f)
        }
        terrainBody = b2World.createBody(bodyDef)
        terrainBody.userData = "terrain"

        parseMapLayers()
    }

    private fun parseMapLayers() {
        for (layer in tiledMap.layers) {
            when (layer.name.lowercase()) {
                "collision" -> parseCollisionLayer(layer)
                "objects" -> parseObjectsLayer(layer)
            }
        }

        // If no player spawns found in map, generate fallbacks
        if (playerSpawns.isEmpty()) {
            playerSpawns.add(SpawnPoint("fallback_0", Vector2(mapWidthMeters * 0.25f, mapHeightMeters * 0.6f)))
            playerSpawns.add(SpawnPoint("fallback_1", Vector2(mapWidthMeters * 0.75f, mapHeightMeters * 0.6f)))
        }
    }

    private fun parseCollisionLayer(layer: MapLayer) {
        val ppm = GameConstants.PPM
        for (obj in layer.objects) {
            when (obj) {
                is PolygonMapObject -> {
                    val poly = obj.polygon
                    val vertices = poly.transformedVertices
                    val count = vertices.size / 2

                    if (count >= 3) {
                        // Box2D supports up to 8 vertices per convex polygon fixture.
                        // For arbitrary polygons, we build a ChainShape loop
                        val worldVertices = Array(count) { i ->
                            Vector2(vertices[i * 2] / ppm, vertices[i * 2 + 1] / ppm)
                        }

                        val chain = ChainShape()
                        try {
                            chain.createLoop(worldVertices)
                            val fixtureDef = FixtureDef().apply {
                                shape = chain
                                friction = 0.6f
                                restitution = 0.05f
                                filter.categoryBits = GameConstants.CATEGORY_TERRAIN
                                filter.maskBits = -1 // Collide with all
                            }
                            val fixture = terrainBody.createFixture(fixtureDef)
                            fixture.userData = "terrain"
                        } catch (e: Exception) {
                            // If loop creation fails due to coincident points, try createChain
                            try {
                                chain.createChain(worldVertices)
                                val fixtureDef = FixtureDef().apply {
                                    shape = chain
                                    friction = 0.6f
                                    filter.categoryBits = GameConstants.CATEGORY_TERRAIN
                                }
                                val fixture = terrainBody.createFixture(fixtureDef)
                                fixture.userData = "terrain"
                            } catch (_: Exception) {}
                        } finally {
                            chain.dispose()
                        }
                    }
                }
                is PolylineMapObject -> {
                    val line = obj.polyline
                    val vertices = line.transformedVertices
                    val count = vertices.size / 2
                    if (count >= 2) {
                        val worldVertices = Array(count) { i ->
                            Vector2(vertices[i * 2] / ppm, vertices[i * 2 + 1] / ppm)
                        }
                        val chain = ChainShape()
                        try {
                            chain.createChain(worldVertices)
                            val fixtureDef = FixtureDef().apply {
                                shape = chain
                                friction = 0.6f
                                filter.categoryBits = GameConstants.CATEGORY_TERRAIN
                            }
                            terrainBody.createFixture(fixtureDef).userData = "terrain"
                        } catch (_: Exception) {} finally {
                            chain.dispose()
                        }
                    }
                }
                is RectangleMapObject -> {
                    val rect = obj.rectangle
                    val shape = PolygonShape()
                    val center = Vector2((rect.x + rect.width / 2f) / ppm, (rect.y + rect.height / 2f) / ppm)
                    shape.setAsBox(rect.width / 2f / ppm, rect.height / 2f / ppm, center, 0f)

                    val fixtureDef = FixtureDef().apply {
                        this.shape = shape
                        friction = 0.6f
                        filter.categoryBits = GameConstants.CATEGORY_TERRAIN
                    }
                    terrainBody.createFixture(fixtureDef).userData = "terrain"
                    shape.dispose()
                }
            }
        }
    }

    private fun parseObjectsLayer(layer: MapLayer) {
        val ppm = GameConstants.PPM
        for (obj in layer.objects) {
            val name = obj.name ?: ""
            val x = obj.properties.get("x", Float::class.java) ?: 0f
            val y = obj.properties.get("y", Float::class.java) ?: 0f
            val worldPos = Vector2(x / ppm, y / ppm)

            when {
                name.startsWith("sp_p_") -> {
                    val spawn = SpawnPoint(name, worldPos)
                    playerSpawns.add(spawn)
                }
                name.startsWith("ctf_sp_1") -> {
                    team1Spawns.add(SpawnPoint(name, worldPos, 1))
                }
                name.startsWith("ctf_sp_2") -> {
                    team2Spawns.add(SpawnPoint(name, worldPos, 2))
                }
                name.startsWith("wp_p_") -> {
                    val weaponProp = obj.properties.get("weapon", String::class.java) ?: "ak47"
                    val allowed = weaponProp.split(",").map { it.trim().lowercase() }
                    weaponSpawns.add(WeaponSpawn(name, worldPos, allowed))
                }
            }
        }
    }

    fun render(camera: OrthographicCamera) {
        renderer.setView(camera)
        renderer.render()
    }

    fun getRandomSpawn(team: Int = 0): Vector2 {
        val list = when (team) {
            1 -> if (team1Spawns.isNotEmpty()) team1Spawns else playerSpawns
            2 -> if (team2Spawns.isNotEmpty()) team2Spawns else playerSpawns
            else -> playerSpawns
        }
        return if (list.isNotEmpty()) list.random().position.cpy() else Vector2(mapWidthMeters / 2f, mapHeightMeters / 2f)
    }

    fun dispose() {
        tiledMap.dispose()
        renderer.dispose()
    }
}
