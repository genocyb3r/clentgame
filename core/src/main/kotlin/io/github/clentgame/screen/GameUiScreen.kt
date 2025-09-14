package io.github.clentgame.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.world
import io.github.clentgame.component.LifeComponent
import io.github.clentgame.component.PlayerComponent
import io.github.clentgame.event.EntityDamageEvent
import io.github.clentgame.event.fire
import io.github.clentgame.ui.Drawables
import io.github.clentgame.ui.model.GameModel
import io.github.clentgame.ui.view.GameView
import io.github.clentgame.ui.view.gameView
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import ktx.scene2d.actors


class GameUiScreen: KtxScreen {
    private val stage: Stage = Stage(ExtendViewport(320f, 180f))
    private val eWorld = world {}
    private val playerEntity: Entity
    private val model = GameModel(eWorld, stage)
    private lateinit var gameView: GameView

    init {
//        loadSkin()
        playerEntity =  eWorld.entity {
            add<PlayerComponent>()
            add<LifeComponent>{
                maxLife = 5f
                life = 3f
            }
        }
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)

    }

    override fun show() {
//        stage.clear()
        stage.actors{
            gameView = gameView(model)
        }
        stage.isDebugAll = true

    }

    override fun render(delta: Float) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            hide()
            show()
        }else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            stage.fire(EntityDamageEvent(playerEntity))
        }else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            gameView.playerLife(0.5f)
        }else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            gameView.playerLife(0.1f)
        }else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {
            gameView.showEnemyInfo(Drawables.SLIME, 0.5f)
        }
        else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) {
            gameView.popUp("YOu found something [#ff0000]cool[]!")
        }

        stage.act()
        stage.draw()
    }

    override fun dispose() {
        stage.disposeSafely()
//        disposeSkin()
    }
}
