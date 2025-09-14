package io.github.clentgame.ui.model

import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.world
import io.github.clentgame.component.AnimationComponent
import io.github.clentgame.component.LifeComponent
import io.github.clentgame.component.PlayerComponent
import io.github.clentgame.event.EntityAggroEvent
import io.github.clentgame.event.EntityDamageEvent
import io.github.clentgame.event.EntityLootEvent
import javax.swing.text.html.parser.Entity


class GameModel(
    world: World,
    stage: Stage
): PropertyChangeSource(), EventListener{

    private val playerCmps: ComponentMapper<PlayerComponent> = world.mapper()
    private val lifeCmps: ComponentMapper<LifeComponent> = world.mapper()
    private val aniCmps: ComponentMapper<AnimationComponent> = world.mapper()

    var playerLife by propertyNotifier(1f)
    var enemyType by propertyNotifier("")
    private var lastEnemy = com.github.quillraven.fleks.Entity(-1)
    var enemyLife by propertyNotifier(1f)
    var lootText by propertyNotifier("")


    init {
        stage.addListener(this)
    }

    private fun updateEnemy(enemy: com.github.quillraven.fleks.Entity){
        val lifeCmp = lifeCmps[enemy]
        enemyLife = lifeCmp.life/lifeCmp.maxLife
        if(lastEnemy != enemy){
            lastEnemy = enemy
            aniCmps.getOrNull(enemy)?.model?.atlasKey?.let{ type ->
                this.enemyType = type
            }
        }

    }

    override fun handle(event: Event): Boolean {
        when(event){
            is EntityDamageEvent -> {
                val isPlayer = event.entity in playerCmps
                val lifeCmp = lifeCmps[event.entity]
                if (isPlayer){
                    playerLife = lifeCmp.life/lifeCmp.maxLife
                }else{
                    updateEnemy(event.entity)
                }
            }
            is EntityLootEvent ->
                lootText = "You found something useful!"

            is EntityAggroEvent -> {
                updateEnemy(event.entity)

            }

            else -> return false
        }

        return true
    }
}
