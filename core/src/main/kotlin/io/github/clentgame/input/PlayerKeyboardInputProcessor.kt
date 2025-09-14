package io.github.clentgame.input

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Input.Keys.DOWN
import com.badlogic.gdx.Input.Keys.LEFT
import com.badlogic.gdx.Input.Keys.RIGHT
import com.badlogic.gdx.Input.Keys.SPACE
import com.badlogic.gdx.Input.Keys.UP
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.World
import io.github.clentgame.component.AttackComponent
import io.github.clentgame.component.DeadComponent
import io.github.clentgame.component.MoveComponent
import io.github.clentgame.component.PlayerComponent
import io.github.clentgame.event.GamePauseEvent
import io.github.clentgame.event.GameResumeEvent
import io.github.clentgame.event.fire
import ktx.app.KtxInputAdapter
import ktx.math.vec2

fun gdxInputProcessor(processor: InputProcessor){
    val currProcessor = Gdx.input.inputProcessor
    if (currProcessor==null){
        Gdx.input.inputProcessor = processor
    }else {
        if(currProcessor is InputMultiplexer){
            if (processor !in currProcessor.processors){
                currProcessor.addProcessor(processor)
            }

        }else{
            Gdx.input.inputProcessor = InputMultiplexer(currProcessor, processor)
        }
    }
}

class PlayerKeyboardInputProcessor(
    world: World,
    private val gameStage: Stage,
    private val joystick: ScreenJoystick,
    private val uistage: Stage,
    private val moveCmps: ComponentMapper<MoveComponent> = world.mapper(),
    private val attackCmps: ComponentMapper<AttackComponent> = world.mapper(),
    private val deadCmps: ComponentMapper<DeadComponent> = world.mapper(),

    ) : KtxInputAdapter {
    private var keyboardSin = 0f
    private var keyboardCos = 0f
    private val tmpVec = vec2()
    private val playerEntities = world.family(allOf = arrayOf(PlayerComponent::class))
    private var paused = false


    // Check if running on Android
    private val isAndroid = Gdx.app.type == Application.ApplicationType.Android

    init {
        gdxInputProcessor(this)
    }

    private fun Int.isMovementKey(): Boolean {
        return this == UP || this == DOWN || this == RIGHT || this == LEFT
    }

    private fun updatePlayerMovement(){
        playerEntities.forEach { player ->
            // Skip dead players
            if (player in deadCmps) return@forEach

            with(moveCmps[player]) {
                if (isAndroid) {
                    // On Android: Use joystick if no keyboard input, otherwise prioritize keyboard
                    val finalCos = if (keyboardCos != 0f) keyboardCos else joystick.getCos()
                    val finalSin = if (keyboardSin != 0f) keyboardSin else joystick.getSin()
                    tmpVec.set(finalCos, finalSin).nor()
                } else {
                    // On Desktop: Only use keyboard input, ignore joystick
                    tmpVec.set(keyboardCos, keyboardSin).nor()
                }
                cos = tmpVec.x
                sin = tmpVec.y
            }
        }
    }


    override fun keyDown(keycode: Int): Boolean {
        if(keycode.isMovementKey()) {
            when (keycode){
                UP -> keyboardSin = 1f
                DOWN -> keyboardSin = -1f
                RIGHT -> keyboardCos = 1f
                LEFT -> keyboardCos = -1f
            }
            updatePlayerMovement()
            return true
        }else if (keycode == Input.Keys.I){
            uistage.actors.get(1).isVisible = !uistage.actors.get(1).isVisible
        }else if (keycode == Input.Keys.P){
            paused = !paused
            gameStage.fire(if (paused) GamePauseEvent() else GameResumeEvent())
        }
        return false
    }


    override fun keyUp(keycode: Int): Boolean {
        if (keycode.isMovementKey()) {
            when (keycode) {
                UP -> keyboardSin = if (Gdx.input.isKeyPressed(DOWN)) -1f else 0f
                DOWN -> keyboardSin = if (Gdx.input.isKeyPressed(UP))  1f else 0f
                RIGHT -> keyboardCos = if (Gdx.input.isKeyPressed(LEFT)) -1f else 0f
                LEFT -> keyboardCos = if (Gdx.input.isKeyPressed(RIGHT)) 1f else 0f
            }
            updatePlayerMovement()
            return true
        } else if (keycode == SPACE) {
            playerEntities.forEach {
                // Skip dead players
                if (it in deadCmps) return@forEach

                with(attackCmps[it]) {
                    doAttack = true
                }
            }
            return true
        }
        return false
    }

    // Touch input handling - only process on Android
    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (!isAndroid) return false  // Ignore touch input on desktop

        val flippedY = Gdx.graphics.height - screenY
        val handled = joystick.handleTouch(screenX.toFloat(), flippedY.toFloat(), true)
        if (handled) {
            updatePlayerMovement()
        }
        return handled
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (!isAndroid) return false  // Ignore touch input on desktop

        val flippedY = Gdx.graphics.height - screenY
        val handled = joystick.handleTouch(screenX.toFloat(), flippedY.toFloat(), false)
        if (handled) {
            updatePlayerMovement()
        }
        return handled
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        if (!isAndroid) return false  // Ignore touch input on desktop

        val flippedY = Gdx.graphics.height - screenY
        val handled = joystick.handleTouch(screenX.toFloat(), flippedY.toFloat(), true)
        if (handled) {
            updatePlayerMovement()
        }
        return handled
    }
}
