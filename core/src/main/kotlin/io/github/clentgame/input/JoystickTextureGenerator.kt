package io.github.clentgame.input


import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture

object JoystickTextureGenerator {

    fun createJoystickBackground(radius: Int, color: Color = Color.GRAY): Texture {
        val pixmap = Pixmap(radius * 2, radius * 2, Pixmap.Format.RGBA8888)
        pixmap.setColor(color.r, color.g, color.b, 0.5f) // Semi-transparent
        pixmap.fillCircle(radius, radius, radius)

        val texture = Texture(pixmap)
        pixmap.dispose()
        return texture
    }

    fun createJoystickKnob(radius: Int, color: Color = Color.WHITE): Texture {
        val pixmap = Pixmap(radius * 2, radius * 2, Pixmap.Format.RGBA8888)
        pixmap.setColor(color)
        pixmap.fillCircle(radius, radius, radius)

        // Add a border
        pixmap.setColor(Color.BLACK)
        pixmap.drawCircle(radius, radius, radius)

        val texture = Texture(pixmap)
        pixmap.dispose()
        return texture
    }
}

