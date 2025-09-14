package io.github.clentgame.system

import box2dLight.RayHandler
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Interpolation
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import io.github.clentgame.component.LightComponent


@AllOf([LightComponent::class])
data class LightSystem(
    private val rayHandler: RayHandler,
    private val lightCmps: ComponentMapper<LightComponent>,

): IteratingSystem() {

    private var ambientTransitionTime = 1f
    private var ambientColor = Color(1f,1f,1f,1f)
    private var ambientColorFrom = dayAmbientLight
    private var ambientColorTo = nightAmbientLight

    override fun onTick() {
        super.onTick()

        if (Gdx.input.isKeyJustPressed(Input.Keys.N) && ambientTransitionTime == 1f) {
            ambientTransitionTime = 0f
            ambientColorFrom = dayAmbientLight
            ambientColorTo = nightAmbientLight
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.D) && ambientTransitionTime == 1f) {
            ambientTransitionTime = 0f
            ambientColorFrom = nightAmbientLight
            ambientColorTo = dayAmbientLight
        }
        if (ambientTransitionTime < 1f){
            ambientTransitionTime = (ambientTransitionTime + deltaTime * 0.5f).coerceAtMost(1f)
            ambientColor.r = interpolation.apply(ambientColorFrom.r, ambientColorTo.r, ambientTransitionTime)
            ambientColor.g= interpolation.apply(ambientColorFrom.g, ambientColorTo.g, ambientTransitionTime)
            ambientColor.b = interpolation.apply(ambientColorFrom.b, ambientColorTo.b, ambientTransitionTime)
            ambientColor.a = interpolation.apply(ambientColorFrom.a, ambientColorTo.a, ambientTransitionTime)

            rayHandler.setAmbientLight(ambientColor)
        }

    }

    override fun onTickEntity(entity: Entity) {
        val lightCmp = lightCmps[entity]
        val (distance, time, direction, light) = lightCmp

        lightCmp.distanceTime = (time+direction*deltaTime).coerceIn(0f, 1f)
        if (lightCmp.distanceTime == 0f || lightCmp.distanceTime == 1f){
            lightCmp.distanceDirection *= -1
        }
        light.distance = interpolation.apply(distance.start, distance.endInclusive, lightCmp.distanceTime)
    }

    companion object{
        private val interpolation: Interpolation = Interpolation.smooth
        private val dayAmbientLight = Color.WHITE
        private val nightAmbientLight = Color.ROYAL

    }
}
