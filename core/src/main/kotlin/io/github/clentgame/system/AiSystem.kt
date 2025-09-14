package io.github.clentgame.system

import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.NoneOf
import io.github.clentgame.component.AIComponent
import io.github.clentgame.component.DeadComponent


@AllOf([AIComponent::class])
@NoneOf([DeadComponent::class])
class AiSystem(
    private val aiCmps: ComponentMapper<AIComponent>
): IteratingSystem() {
    override fun onTickEntity(entity: Entity) {
        with(aiCmps[entity]){
            behaviorTree.step()
        }
    }
}
