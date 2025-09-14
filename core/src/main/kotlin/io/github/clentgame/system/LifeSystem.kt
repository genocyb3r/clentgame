package io.github.clentgame.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.NoneOf
import io.github.clentgame.component.AnimationComponent
import io.github.clentgame.component.AnimationModel
import io.github.clentgame.component.AnimationType
import io.github.clentgame.component.DeadComponent
import io.github.clentgame.component.FloatingTextComponent
import io.github.clentgame.component.LifeComponent
import io.github.clentgame.component.PhysicComponent
import io.github.clentgame.component.PlayerComponent
import io.github.clentgame.event.EntityDamageEvent
import io.github.clentgame.event.EntityDeathEvent
import io.github.clentgame.event.fire
import ktx.assets.disposeSafely
import kotlin.math.roundToInt

@AllOf([LifeComponent::class])
@NoneOf([DeadComponent::class])
class LifeSystem(
    private val lifeCmps: ComponentMapper<LifeComponent>,
    private val deadCmps: ComponentMapper<DeadComponent>,
    private val playerCmps: ComponentMapper<PlayerComponent>,
    private val physicCmps: ComponentMapper<PhysicComponent>,
    private val aniCmps: ComponentMapper<AnimationComponent>,
    private val gameStage: Stage,
): IteratingSystem(){
    private val damageFont = BitmapFont(Gdx.files.internal("damage.fnt")).apply {
        data.setScale(0.3f)
    }
    private val floatingTextStyle = LabelStyle(damageFont, Color.WHITE)

    override fun onTickEntity(entity: Entity) {
        val lifeCmp = lifeCmps[entity]
        lifeCmp.life = (lifeCmp.life + lifeCmp.regeneration * deltaTime).coerceAtMost(lifeCmp.maxLife)

        if (lifeCmp.takeDamage > 0){
            val physicCmp = physicCmps[entity]
            lifeCmp.life -= lifeCmp.takeDamage
            gameStage.fire(EntityDamageEvent(entity))
            floatingText(lifeCmp.takeDamage.roundToInt().toString(), physicCmp.body.position, physicCmp.size)
            lifeCmp.takeDamage = 0f
        }
        if (lifeCmp.isDead){

            gameStage.fire(EntityDeathEvent(AnimationModel.PLAYER))
            aniCmps.getOrNull(entity)?.let{aniCmp ->
                aniCmp.nextAnimation(AnimationType.DEATH)
                aniCmp.playMode = Animation.PlayMode.NORMAL
            }
           configureEntity(entity){
               deadCmps.add(it){
                   if (it in playerCmps){
                       reviveTime = 10f

//                       Gdx.app.debug("DEAD ALERT", "Player is dead")
//                       Gdx.app.exit()

                   }
               }
           }
        }
    }

    private fun floatingText(text: String, position: com.badlogic.gdx.math.Vector2, size: com.badlogic.gdx.math.Vector2){
        world.entity{
            add<FloatingTextComponent>{
                txtLocation.set(position.x, position.y + size.y * 0.5f)
                lifeSpan = 1.5f
                label = Label(text, floatingTextStyle)
            }
        }
    }

    override fun onDispose() {
        damageFont.disposeSafely()
    }


}
