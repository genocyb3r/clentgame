package io.github.clentgame.system

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.github.quillraven.fleks.IntervalSystem
import io.github.clentgame.system.AttackSystem.Companion.AABB_RECT
import ktx.assets.disposeSafely
import ktx.graphics.use

class DebugSystem(
    private val phWorld: com.badlogic.gdx.physics.box2d.World,
    private val stage: com.badlogic.gdx.scenes.scene2d.Stage,
): IntervalSystem(enabled = true) {
    private lateinit var phDebugRenderer: com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
    private lateinit var shapeRenderer: com.badlogic.gdx.graphics.glutils.ShapeRenderer

    init {
        if (enabled) {
            phDebugRenderer = com.badlogic.gdx.physics.box2d.Box2DDebugRenderer()
            shapeRenderer = com.badlogic.gdx.graphics.glutils.ShapeRenderer()
        }
    }

    override fun onTick() {
        phDebugRenderer.render(phWorld, stage.camera.combined)
        shapeRenderer.use(ShapeRenderer.ShapeType.Line, stage.camera.combined){
            it.setColor(1.0f, 0.0f, 0.0f, 1.0f)
            it.rect(AABB_RECT.x, AABB_RECT.y, AABB_RECT.width - AABB_RECT.x , AABB_RECT.height- AABB_RECT.y)
        }
    }

    override fun onDispose() {
        if (enabled) {
            phDebugRenderer.disposeSafely()
            shapeRenderer.disposeSafely()
        }

    }

}
