package io.github.clentgame.screen


import box2dLight.Light
import box2dLight.RayHandler
import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ai.GdxAI
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.world
import io.github.clentgame.ClentGame
import io.github.clentgame.component.AIComponent
import io.github.clentgame.component.FloatingTextComponent
import io.github.clentgame.component.ImageComponent
import io.github.clentgame.component.LightComponent
import io.github.clentgame.component.PhysicComponent
import io.github.clentgame.component.StateComponent
import io.github.clentgame.input.JoystickTextureGenerator
import io.github.clentgame.input.PlayerKeyboardInputProcessor
import io.github.clentgame.input.ScreenJoystick
import io.github.clentgame.input.gdxInputProcessor
import io.github.clentgame.system.AiSystem
import io.github.clentgame.system.AnimationSystem
import io.github.clentgame.system.AttackSystem
import io.github.clentgame.system.AudioSystem
import io.github.clentgame.system.CameraSystem
import io.github.clentgame.system.CollisionDespawnSystem
import io.github.clentgame.system.CollisionSpawnSystem
import io.github.clentgame.system.DeadSystem
import io.github.clentgame.system.DebugSystem
import io.github.clentgame.system.DialogSystem
import io.github.clentgame.system.EntitySpawnSystem
import io.github.clentgame.system.FloatingTextSystem
import io.github.clentgame.system.InventorySystem
import io.github.clentgame.system.LifeSystem
import io.github.clentgame.system.LightSystem
import io.github.clentgame.system.LootSystem
import io.github.clentgame.system.MoveSystem
import io.github.clentgame.system.PhysicSystem
import io.github.clentgame.system.PortalSystem
import io.github.clentgame.system.RenderSystem
import io.github.clentgame.system.StateSystem
import io.github.clentgame.ui.model.DialogModel
import io.github.clentgame.ui.model.GameModel
import io.github.clentgame.ui.model.InventoryModel
import io.github.clentgame.ui.view.PauseView
import io.github.clentgame.ui.view.dialogView
import io.github.clentgame.ui.view.gameView
import io.github.clentgame.ui.view.inventoryView
import io.github.clentgame.ui.view.pauseView
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import ktx.box2d.createWorld
import ktx.log.logger
import ktx.math.vec2
import ktx.scene2d.actors
import kotlin.reflect.KClass

class GameScreen(game: ClentGame): KtxScreen {
    private val gamesStage = game.gamesStage
    private val uiStage = game.uiStage
    private lateinit var batch: SpriteBatch
    private val textureAtlas = TextureAtlas("graphics/gameObjects.atlas")

    private lateinit var joystick: ScreenJoystick
    private val phWorld = createWorld(
        gravity = vec2()).apply { autoClearForces=false }

    private val rayHandler = RayHandler(phWorld).apply {
        RayHandler.useDiffuseLight(true)
        Light.setGlobalContactFilter(LightComponent.playerCategory, 1, LightComponent.environmentCategory)
        setAmbientLight(Color.ROYAL)
    }

    private lateinit var joystickKnobTexture: Texture
    private lateinit var joystickBackgroundTexture: Texture


    private val eWorld: World = world{
        injectables {
            add(gamesStage)
            add("uiStage", uiStage   )
            add(textureAtlas)
            add(phWorld)
            add(rayHandler)

        }
        components {
            add<ImageComponent.Companion.ImageComponentListener>()
            add<PhysicComponent.PhysicComponentListener>()
            add<FloatingTextComponent.Companion.FloatingTextComponentListener>()
            add<StateComponent.Companion.StateComponentListener>()
            add<AIComponent.Companion.AIComponentListener>()
            add<LightComponent.Companion.LightComponentListener>()

        }

        systems{
            add<EntitySpawnSystem>()
            add<CollisionSpawnSystem>()
            add<CollisionDespawnSystem>()
            add<PortalSystem>()
            add<MoveSystem>()
            add<AttackSystem>()
            add<LootSystem>()
            add<DialogSystem>()
            add<InventorySystem>()
            add<DeadSystem>()
            add<LifeSystem>()
            add<PhysicSystem>()
            add<LightSystem>()
            add<AnimationSystem>()
            add<StateSystem>()
            add<AiSystem>()
            add<CameraSystem>()
            add<FloatingTextSystem>()
            add<RenderSystem>()
            add<AudioSystem>()
            add<DebugSystem>()
        }

    }

    init {
        uiStage.actors{
            gameView(GameModel(eWorld, gamesStage))
            dialogView(DialogModel(gamesStage))
            inventoryView(InventoryModel(eWorld, gamesStage)){
                this.isVisible = false
            }
            pauseView { this.isVisible = false }
        }

    }


    override fun show() {
        log.debug { "GameScreen is shown" }

        batch = SpriteBatch()
        eWorld.systems.forEach { add ->
            if(add is EventListener) {
                gamesStage.addListener(add)
            }
        }
        eWorld.system<PortalSystem>().setMap("map/map.tmx")


        joystickBackgroundTexture = JoystickTextureGenerator.createJoystickBackground(100)
        joystickKnobTexture = JoystickTextureGenerator.createJoystickKnob(40)


        val joystickRadius = 100f
        joystick = ScreenJoystick(
            joystickKnobTexture,
            joystickBackgroundTexture,
            joystickRadius
        )
        gdxInputProcessor(uiStage)
        PlayerKeyboardInputProcessor(eWorld, gamesStage, joystick, uiStage)


    }

    private fun pauseWorld(pause: Boolean){
        val mandatorySystems: Set<KClass<out IntervalSystem>> = setOf(
            AnimationSystem::class,
            CameraSystem::class,
            RenderSystem::class,
            DebugSystem::class,
        )

        eWorld.systems
            .filter { it::class !in mandatorySystems }
            .forEach { it.enabled = !pause }

        uiStage.actors.filterIsInstance<PauseView>().first().isVisible = pause

    }

    override fun pause() = pauseWorld(true)

    override fun resume()= pauseWorld(false)

    override fun resize(width: Int, height: Int) {
        val screenX = gamesStage.viewport.screenX
        val screenY = gamesStage.viewport.screenY
        val screenW = gamesStage.viewport.screenWidth
        val screenH = gamesStage.viewport.screenHeight
        rayHandler.useCustomViewport(screenX, screenY, screenW, screenH)
    }

    override fun render(delta: Float) {
        val dt = delta.coerceAtMost(0.25f)
        GdxAI.getTimepiece().update(dt)
        eWorld.update(delta.coerceAtMost(0.25f))
        if (Gdx.app.type == Application.ApplicationType.Android) {
            batch.begin()
            joystick.render(batch)
            batch.end()
        }
    }

    override fun dispose() {
        eWorld.dispose()
        textureAtlas.disposeSafely()
        phWorld.disposeSafely()
        rayHandler.disposeSafely()

        if (::joystickKnobTexture.isInitialized) {
            joystickKnobTexture.disposeSafely()
        }
        if (::joystickBackgroundTexture.isInitialized) {
            joystickBackgroundTexture.disposeSafely()
        }

        if (::batch.isInitialized) {
            batch.disposeSafely()
        }

    }
    companion object {
        private val log = logger<GameScreen>()
    }
}

