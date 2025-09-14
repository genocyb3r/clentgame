package io.github.clentgame.input

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2

class ScreenJoystick(
    private val knobTexture: Texture,
    private val backgroundTexture: Texture,
    private val radius: Float = 100f,
    offsetX: Float = 50f,
    offsetY: Float = 50f
) {
    private val center = Vector2(radius + offsetX, radius + offsetY)
    private val knobPosition = Vector2(center)
    private val touchPosition = Vector2()
    private val tempVector = Vector2()

    private val knobRadius = radius * 0.4f
    private val backgroundSize = radius * 2f

    var isPressed = false
        private set

    val direction = Vector2()

    var strength = 0f
        private set

    fun handleTouch(screenX: Float, screenY: Float, pressed: Boolean): Boolean {
        val distanceFromCenter = center.dst(screenX, screenY)

        return when {
            pressed && canStartInteraction(distanceFromCenter) -> {
                startInteraction(screenX, screenY)
                true
            }
            pressed && isPressed -> {
                updateInteraction(screenX, screenY)
                true
            }
            !pressed && isPressed -> {
                endInteraction()
                true
            }
            else -> false
        }
    }

    private fun canStartInteraction(distanceFromCenter: Float): Boolean =
        !isPressed && distanceFromCenter <= radius

    private fun startInteraction(screenX: Float, screenY: Float) {
        isPressed = true
        updateInteraction(screenX, screenY)
    }

    private fun updateInteraction(screenX: Float, screenY: Float) {
        touchPosition.set(screenX, screenY)
        updateKnobPosition()
        calculateDirectionAndStrength()
    }

    private fun endInteraction() {
        isPressed = false
        resetJoystick()
    }

    private fun updateKnobPosition() {
        tempVector.set(touchPosition).sub(center)
        val distance = tempVector.len()

        if (distance <= radius) {
            knobPosition.set(touchPosition)
            strength = distance / radius
        } else {
            tempVector.nor().scl(radius)
            knobPosition.set(center).add(tempVector)
            strength = 1f
        }
    }

    private fun calculateDirectionAndStrength() {
        tempVector.set(knobPosition).sub(center)

        if (radius > 0f) {
            direction.set(tempVector.x / radius, tempVector.y / radius)
        } else {
            direction.setZero()
        }
    }

    private fun resetJoystick() {
        knobPosition.set(center)
        direction.setZero()
        strength = 0f
    }

    fun render(batch: SpriteBatch) {
        renderBackground(batch)
        renderKnob(batch)
    }

    private fun renderBackground(batch: SpriteBatch) {
        batch.draw(
            backgroundTexture,
            center.x - radius,
            center.y - radius,
            backgroundSize,
            backgroundSize
        )
    }

    private fun renderKnob(batch: SpriteBatch) {
        batch.draw(
            knobTexture,
            knobPosition.x - knobRadius * 0.5f,
            knobPosition.y - knobRadius * 0.5f,
            knobRadius,
            knobRadius
        )
    }

    fun getCos(): Float = direction.x * strength

    fun getSin(): Float = direction.y * strength

    fun getAngle(): Float = MathUtils.atan2(direction.y, direction.x) * MathUtils.radiansToDegrees

    fun setPosition(x: Float, y: Float) {
        val deltaX = x - center.x
        val deltaY = y - center.y
        center.set(x, y)
        knobPosition.add(deltaX, deltaY)
    }

    fun isInBounds(x: Float, y: Float): Boolean = center.dst(x, y) <= radius
}
