package io.github.clentgame.system

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import io.github.clentgame.component.AnimationComponent
import io.github.clentgame.component.AnimationType
import io.github.clentgame.component.LootComponent
import io.github.clentgame.event.EntityLootEvent
import io.github.clentgame.event.fire

@AllOf([LootComponent::class])
class LootSystem(
    private val loopCmps: ComponentMapper<LootComponent>,
    private val aniCmps: ComponentMapper<AnimationComponent>,
    private val stage: Stage
): IteratingSystem() {
    override fun onTickEntity(entity: Entity) {
        with(loopCmps[entity]){
            if (interactEntity == null ){
                return
            }
            stage.fire(EntityLootEvent(aniCmps[entity].model))
            configureEntity(entity){loopCmps.remove(it)}
            aniCmps.getOrNull(entity)?.let {aniCmp ->
                aniCmp.nextAnimation(AnimationType.OPEN)
                aniCmp.playMode = Animation.PlayMode.NORMAL
            }
        }
    }
}
