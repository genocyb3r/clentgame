package io.github.clentgame

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ExtendViewport
import io.github.clentgame.event.GamePauseEvent
import io.github.clentgame.event.GameResumeEvent
import ktx.app.KtxGame
import ktx.app.KtxScreen
import io.github.clentgame.screen.GameScreen
import io.github.clentgame.screen.GameUiScreen
import io.github.clentgame.screen.InventoryUiScreen
import io.github.clentgame.ui.disposeSkin
import io.github.clentgame.ui.loadSkin
import ktx.app.clearScreen
import ktx.assets.disposeSafely
import kotlin.getValue

/** [com.badlogic.gdx.ApplicationListener] implementation shared by all platforms. */
class ClentGame : KtxGame<KtxScreen>(), EventListener{

    private val batch: Batch by lazy { SpriteBatch() }
    val gamesStage: Stage by lazy {  Stage(ExtendViewport(16f, 9f))}
    val uiStage: Stage by lazy {  Stage(ExtendViewport(320f, 180f))}
    private var paused = false

    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG
        loadSkin()

        gamesStage.addListener(this)

        addScreen(GameScreen(this))
        addScreen(GameUiScreen())
        addScreen(InventoryUiScreen())
//        setScreen<InventoryUiScreen>()
//        setScreen<UiScreen>()
        setScreen<GameScreen>()
    }

    override fun resize(width: Int, height: Int) {
        gamesStage.viewport.update(width, height, true)
        uiStage.viewport.update(width, height, true)
        super.resize(width, height)

    }

    override fun render() {
        clearScreen(0f, 0f, 0f, 1f)
        val dt: Float = if (paused) 0f else Gdx.graphics.deltaTime
        currentScreen.render(dt)
    }


    override fun dispose() {
        super.dispose()
        gamesStage.disposeSafely()
        uiStage.disposeSafely()
        batch.disposeSafely()
        disposeSkin()
    }

    override fun handle(event: Event): Boolean {
        when(event){
            is GamePauseEvent -> {
                paused = true
                currentScreen.pause()
            }
            is GameResumeEvent -> {
                paused = false
                currentScreen.resume()
            }
            else -> return false
        }
        return true
    }

    companion object {
        const val UNIT_SCALE = 1 / 16f
        const val TILE_SIZE = 16f
    }
}
