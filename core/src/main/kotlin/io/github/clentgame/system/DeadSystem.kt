package io.github.clentgame.system

import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import io.github.clentgame.component.AnimationComponent
import io.github.clentgame.component.DeadComponent
import io.github.clentgame.component.LifeComponent
import io.github.clentgame.event.EntityDeathEvent
import io.github.clentgame.event.fire

@AllOf([DeadComponent::class])
class DeadSystem(
    private val deadCmps: ComponentMapper<DeadComponent>,
    private val lifeCmps: ComponentMapper<LifeComponent>,
    private val stage: Stage,
    private val animationCmps: ComponentMapper<AnimationComponent>,
): IteratingSystem() {
    override fun onTickEntity(entity: Entity) {
        val deadCmp = deadCmps[entity]
        if (deadCmp.reviveTime == 0f){
            // Add delay for death animation
            deadCmp.reviveTime = -0.5f
            return
        }
        if (deadCmp.reviveTime < 0f){
            deadCmp.reviveTime += deltaTime
            if (deadCmp.reviveTime >= 0f){
                stage.fire(EntityDeathEvent(animationCmps[entity].model))
                world.remove(entity)
            }
            return
        }
        deadCmp.reviveTime -= deltaTime
        if (deadCmp.reviveTime <= 0f){
            with(lifeCmps[entity]) { life = maxLife}
            configureEntity(entity) {
                deadCmps.remove(entity)
            }
        }
    }
}
