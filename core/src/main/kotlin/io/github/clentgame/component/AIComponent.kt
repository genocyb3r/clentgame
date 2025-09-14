package io.github.clentgame.component

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ai.btree.BehaviorTree
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeParser
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.ComponentListener
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.clentgame.ai.AiEntity

class AIComponent(
    val nearbyEntities: MutableSet<Entity> = mutableSetOf(),
    var treePath: String = "",
) {
    lateinit var behaviorTree: BehaviorTree<AiEntity>

    companion object{
        class AIComponentListener(
            private val world: World,
            private val stage: Stage,
        ): ComponentListener<AIComponent> {
            private val treeParser = BehaviorTreeParser<AiEntity>()

            override fun onComponentAdded(entity: Entity, component: AIComponent) {
                component.behaviorTree = treeParser.parse(
                    Gdx.files.internal(component.treePath),
                    AiEntity(entity, world, stage)
                )
            }
            override fun onComponentRemoved(entity: Entity, component: AIComponent) = Unit

        }
    }
}
