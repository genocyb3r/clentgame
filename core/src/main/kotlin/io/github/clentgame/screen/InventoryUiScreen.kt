package io.github.clentgame.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.world
import io.github.clentgame.component.InventoryComponent
import io.github.clentgame.component.ItemCategory
import io.github.clentgame.component.PlayerComponent
import io.github.clentgame.event.EntityDamageEvent
import io.github.clentgame.event.fire
import io.github.clentgame.input.gdxInputProcessor
import io.github.clentgame.ui.Drawables
import io.github.clentgame.ui.model.InventoryModel
import io.github.clentgame.ui.model.ItemModel
import io.github.clentgame.ui.view.InventoryView
import io.github.clentgame.ui.view.gameView
import io.github.clentgame.ui.view.inventoryView
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import ktx.scene2d.actors


class InventoryUiScreen: KtxScreen {
    private val stage: Stage = Stage(ExtendViewport(320f, 180f))
    private val eWorld = world {}
    private val playerEntity: Entity
    private val model = InventoryModel(eWorld, stage)
    private lateinit var inventoryView: InventoryView

    init {
//        loadSkin()
        playerEntity =  eWorld.entity {
            add<PlayerComponent>()
            add<InventoryComponent>()

        }
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)

    }

    override fun show() {
        stage.clear()
        stage.addListener(model)
        stage.actors{
            inventoryView = inventoryView(model)

        }
        gdxInputProcessor(stage)
        stage.isDebugAll = false

    }

    override fun render(delta: Float) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            hide()
            show()
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)){
            inventoryView.item(ItemModel(-1, ItemCategory.BOOTS, "boots", 1, false))
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)){
            inventoryView.item(ItemModel(-1, ItemCategory.HELMET, "helmet", 3, false))
        }

        stage.act()
        stage.draw()
    }

    override fun dispose() {
        stage.disposeSafely()
    }
}
