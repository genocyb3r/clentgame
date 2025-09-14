package io.github.clentgame.ui.view

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import io.github.clentgame.ui.Labels
import ktx.actors.plusAssign
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.label

class PauseView(skin: Skin): KTable, Table(skin) {

    init {
        setFillParent(true)

        if (!skin.has(PIXMAP_KEY, TextureRegionDrawable::class.java)){
            skin.add(PIXMAP_KEY, TextureRegionDrawable(
                Texture(
                    Pixmap(1,1, Pixmap.Format.RGBA8888).apply {
                        this.drawPixel(0, 0, Color.rgba8888(0.1f, 0.1f,0.1f, 0.7f))
                    }
                )
            ))
        }
        background = skin.get(PIXMAP_KEY, TextureRegionDrawable::class.java)
        this += label("[#ff0000]Pause[]", Labels.LARGE.skinKey){lblCell ->
            lblCell.expand().fill()
            this.setAlignment(Align.center)
        }
    }

    companion object {
        private val PIXMAP_KEY = "pauseTexturePixmap"
    }
}

@Scene2dDsl
fun <S> KWidget<S>.pauseView(skin: Skin = Scene2DSkin.defaultSkin, init: PauseView.(S) -> Unit = {}
): PauseView = actor(PauseView(skin), init)
