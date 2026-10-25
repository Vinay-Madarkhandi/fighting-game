package com.vinay.minimilitia.constants

object GameConstants {
    // Virtual Display Viewport (16:9 standard, auto-scaled with ExtendViewport)
    const val VIRTUAL_WIDTH = 960f
    const val VIRTUAL_HEIGHT = 540f

    // Box2D Physics Scaling: 1 meter = 32 pixels
    const val PPM = 32f

    // Physics Simulation Constants
    const val GRAVITY_Y = -22f // In m/s^2 (~704 px/s^2)
    const val TIME_STEP = 1f / 60f
    const val VELOCITY_ITERATIONS = 8
    const val POSITION_ITERATIONS = 3

    // Player Characteristics
    const val PLAYER_RADIUS = 0.65f // ~20 pixels radius
    const val PLAYER_HEIGHT = 1.6f  // ~51 pixels tall
    const val PLAYER_MAX_HEALTH = 100f
    const val PLAYER_MAX_BOOST = 100f
    const val PLAYER_WALK_SPEED = 7.5f // m/s
    const val PLAYER_JETPACK_THRUST = 42f // m/s^2 thrust force
    const val PLAYER_BOOST_BURN_RATE = 25f // units per second (4 seconds of flight)
    const val PLAYER_BOOST_RECHARGE_RATE = 20f // units per second
    const val PLAYER_RESPAWN_DELAY = 3f // seconds
    const val PLAYER_SPAWN_SHIELD_DURATION = 3f // seconds

    // Collision Categories
    const val CATEGORY_TERRAIN: Short = 0x0001
    const val CATEGORY_PLAYER: Short = 0x0002
    const val CATEGORY_PROJECTILE: Short = 0x0004
    const val CATEGORY_PICKUP: Short = 0x0008
    const val CATEGORY_SENSOR: Short = 0x0010

    // Touch Controller Deadzones and Thresholds
    const val JOYSTICK_DEADZONE = 0.15f
    const val JETPACK_TRIGGER_Y = 0.35f // Push left joystick up > 0.35 to fire jetpack
    const val FIRE_TRIGGER_RADIUS = 0.65f // Pull right joystick outward > 0.65 to fire weapon
}
