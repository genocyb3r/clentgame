package io.github.clentgame.component

import com.badlogic.gdx.ai.fsm.DefaultStateMachine
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.clentgame.ai.AiEntity
import io.github.clentgame.ai.DefaultState
import io.github.clentgame.ai.EntityState

data class StateComponent (
    var nextState: EntityState = DefaultState.IDLE,
    val stateMachine: DefaultStateMachine<AiEntity, EntityState> = DefaultStateMachine()

){
    companion object{
        class StateComponentListener(
            private val world: World,
            private val stage: Stage,
        ): com.github.quillraven.fleks.ComponentListener<StateComponent>{
            override fun onComponentAdded(entity: Entity, component: StateComponent) {
                component.stateMachine.owner = AiEntity(entity, world, stage)
            }

            override fun onComponentRemoved(entity: Entity, component: StateComponent) = Unit

        }
    }
}
