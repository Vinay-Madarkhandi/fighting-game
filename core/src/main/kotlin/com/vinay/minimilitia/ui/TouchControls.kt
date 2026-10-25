package com.vinay.minimilitia.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport
import com.vinay.minimilitia.constants.GameConstants

class TouchControls(val screenWidth: Float, val screenHeight: Float) {
    // Left Joystick (Movement & Jetpack)
    val leftBase = Vector2(140f, 130f)
    val leftKnob = Vector2(leftBase)
    val moveVector = Vector2()
    var isLeftTouched = false
    private var leftPointer = -1

    // Right Joystick (Aim & Fire)
    val rightBase = Vector2(screenWidth - 140f, 130f)
    val rightKnob = Vector2(rightBase)
    val aimVector = Vector2()
    var isRightTouched = false
    private var rightPointer = -1

    val maxRadius = 75f

    // Action Buttons
    val reloadBtnPos = Vector2(screenWidth - 70f, 260f)
    val swapBtnPos = Vector2(screenWidth - 150f, 260f)
    val grenadeBtnPos = Vector2(screenWidth - 230f, 260f)
    val meleeBtnPos = Vector2(screenWidth - 220f, 170f)
    val zoomBtnPos = Vector2(screenWidth - 70f, 340f)
    val buttonRadius = 32f

    // Button states
    var wantsReload = false
    var wantsSwap = false
    var wantsGrenade = false
    var wantsMelee = false
    var wantsZoom = false

    val isFiring: Boolean get() = aimVector.len() > GameConstants.FIRE_TRIGGER_RADIUS || Gdx.input.isButtonPressed(Input.Buttons.LEFT)
    val isThrusting: Boolean get() = moveVector.y > GameConstants.JETPACK_TRIGGER_Y || Gdx.input.isKeyPressed(Input.Keys.SPACE) || Gdx.input.isKeyPressed(Input.Keys.W)

    fun update(viewport: Viewport? = null) {
        // Clear one-shot button triggers
        wantsReload = false
        wantsSwap = false
        wantsGrenade = false
        wantsMelee = false
        wantsZoom = false

        // Check Keyboard inputs (for desktop development and testing)
        var kbMoveX = 0f
        var kbMoveY = 0f
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) kbMoveX -= 1f
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) kbMoveX += 1f
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.SPACE)) kbMoveY += 1f
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) kbMoveY -= 0.5f

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) wantsReload = true
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) wantsSwap = true
        if (Gdx.input.isKeyJustPressed(Input.Keys.G)) wantsGrenade = true
        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) wantsMelee = true
        if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) wantsZoom = true

        if (!isLeftTouched && (kbMoveX != 0f || kbMoveY != 0f)) {
            moveVector.set(kbMoveX, kbMoveY).nor()
        }

        // Process touch pointers
        for (i in 0 until 5) {
            if (Gdx.input.isTouched(i)) {
                val touchPos = if (viewport != null) {
                    viewport.unproject(Vector2(Gdx.input.getX(i).toFloat(), Gdx.input.getY(i).toFloat()))
                } else {
                    Vector2(Gdx.input.getX(i).toFloat(), (Gdx.graphics.height - Gdx.input.getY(i)).toFloat())
                }
                val touchX = touchPos.x
                val touchY = touchPos.y

                // Check Left Joystick
                if (!isLeftTouched && touchX < screenWidth * 0.4f && touchY < screenHeight * 0.6f) {
                    isLeftTouched = true
                    leftPointer = i
                }

                // Check Right Joystick
                if (!isRightTouched && touchX > screenWidth * 0.6f && touchY < screenHeight * 0.5f) {
                    isRightTouched = true
                    rightPointer = i
                }

                // Check button taps on touch down
                if (Gdx.input.justTouched()) {
                    if (touchPos.dst(reloadBtnPos) < buttonRadius * 1.3f) wantsReload = true
                    if (touchPos.dst(swapBtnPos) < buttonRadius * 1.3f) wantsSwap = true
                    if (touchPos.dst(grenadeBtnPos) < buttonRadius * 1.3f) wantsGrenade = true
                    if (touchPos.dst(meleeBtnPos) < buttonRadius * 1.3f) wantsMelee = true
                    if (touchPos.dst(zoomBtnPos) < buttonRadius * 1.3f) wantsZoom = true
                }

                // Update active Left Joystick
                if (leftPointer == i) {
                    val delta = touchPos.cpy().sub(leftBase)
                    if (delta.len() > maxRadius) delta.nor().scl(maxRadius)
                    leftKnob.set(leftBase).add(delta)
                    moveVector.set(delta.x / maxRadius, delta.y / maxRadius)
                }

                // Update active Right Joystick
                if (rightPointer == i) {
                    val delta = touchPos.cpy().sub(rightBase)
                    if (delta.len() > maxRadius) delta.nor().scl(maxRadius)
                    rightKnob.set(rightBase).add(delta)
                    aimVector.set(delta.x / maxRadius, delta.y / maxRadius)
                }
            } else {
                if (leftPointer == i) {
                    isLeftTouched = false
                    leftPointer = -1
                    leftKnob.set(leftBase)
                    moveVector.setZero()
                }
                if (rightPointer == i) {
                    isRightTouched = false
                    rightPointer = -1
                    rightKnob.set(rightBase)
                    aimVector.setZero()
                }
            }
        }
    }

    fun setMouseAim(screenMouseX: Float, screenMouseY: Float, playerScreenX: Float, playerScreenY: Float) {
        if (!isRightTouched) {
            val diff = Vector2(screenMouseX - playerScreenX, screenMouseY - playerScreenY)
            if (diff.len2() > 4f) {
                aimVector.set(diff.nor())
            }
        }
    }

    fun render(shapeRenderer: ShapeRenderer) {
        // Render Left Joystick (Move & Jetpack)
        shapeRenderer.setColor(1f, 1f, 1f, 0.25f)
        shapeRenderer.circle(leftBase.x, leftBase.y, maxRadius, 20)
        if (isThrusting) {
            shapeRenderer.setColor(1f, 0.6f, 0.1f, 0.7f)
        } else {
            shapeRenderer.setColor(0.2f, 0.8f, 1f, 0.6f)
        }
        shapeRenderer.circle(leftKnob.x, leftKnob.y, 28f, 16)

        // Render Right Joystick (Aim & Fire)
        shapeRenderer.setColor(1f, 1f, 1f, 0.25f)
        shapeRenderer.circle(rightBase.x, rightBase.y, maxRadius, 20)
        if (isFiring) {
            shapeRenderer.setColor(1f, 0.2f, 0.2f, 0.7f)
        } else {
            shapeRenderer.setColor(1f, 1f, 0.2f, 0.6f)
        }
        shapeRenderer.circle(rightKnob.x, rightKnob.y, 28f, 16)

        // Action Buttons
        // Reload
        shapeRenderer.setColor(0.2f, 0.6f, 1f, 0.6f)
        shapeRenderer.circle(reloadBtnPos.x, reloadBtnPos.y, buttonRadius, 14)

        // Swap Weapon
        shapeRenderer.setColor(0.9f, 0.5f, 0.1f, 0.6f)
        shapeRenderer.circle(swapBtnPos.x, swapBtnPos.y, buttonRadius, 14)

        // Grenade
        shapeRenderer.setColor(0.3f, 0.8f, 0.3f, 0.6f)
        shapeRenderer.circle(grenadeBtnPos.x, grenadeBtnPos.y, buttonRadius, 14)

        // Melee / Punch
        shapeRenderer.setColor(0.9f, 0.2f, 0.2f, 0.6f)
        shapeRenderer.circle(meleeBtnPos.x, meleeBtnPos.y, buttonRadius, 14)

        // Zoom
        shapeRenderer.setColor(0.7f, 0.3f, 0.9f, 0.6f)
        shapeRenderer.circle(zoomBtnPos.x, zoomBtnPos.y, buttonRadius, 14)
    }
}
