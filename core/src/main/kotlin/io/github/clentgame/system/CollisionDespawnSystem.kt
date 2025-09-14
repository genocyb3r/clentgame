package io.github.clentgame.system

import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import io.github.clentgame.component.TiledComponent
import io.github.clentgame.event.CollisionDespawnEvent
import io.github.clentgame.event.fire


@AllOf([TiledComponent::class])
class CollisionDespawnSystem(
    private val tiledCmps: ComponentMapper<TiledComponent>,
    private val stage: Stage
): IteratingSystem() {

    override fun onTickEntity(entity: Entity) {
        with(tiledCmps[entity]){
            if(tiledCmps[entity].nearbyEntities.isEmpty()){
                stage.fire(CollisionDespawnEvent(cell))
                world.remove(entity)
            }

        }

    }
}
