package io.github.clentgame.system

import com.badlogic.gdx.math.MathUtils
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.NoneOf
import io.github.clentgame.component.AnimationComponent
import io.github.clentgame.component.AttackComponent
import io.github.clentgame.component.AttackState
import io.github.clentgame.component.DisarmComponent
import io.github.clentgame.component.ImageComponent
import io.github.clentgame.component.LifeComponent
import io.github.clentgame.component.LootComponent
import io.github.clentgame.component.PhysicComponent
import io.github.clentgame.component.PlayerComponent
import io.github.clentgame.event.EntityAttackEvent
import io.github.clentgame.event.fire
import io.github.clentgame.system.EntitySpawnSystem.Companion.HIT_BOX_SENSOR
import ktx.box2d.query
import ktx.math.component1
import ktx.math.component2


@AllOf([AttackComponent::class, PhysicComponent::class, ImageComponent::class])
@NoneOf([DisarmComponent::class])
class AttackSystem(
    private val attackCmps: ComponentMapper<AttackComponent>,
    private val physicCmps: ComponentMapper<PhysicComponent>,
    private val imageCmps: ComponentMapper<ImageComponent>,
    private val phWorld: com.badlogic.gdx.physics.box2d.World,
    private val lifeCmps: ComponentMapper<LifeComponent>,
    private val lootCmps: ComponentMapper<LootComponent>,
    private val dialogCmps: ComponentMapper<io.github.clentgame.component.DialogComponent>,
    private val animationCmps: ComponentMapper<AnimationComponent>,
    private val playerCmps: ComponentMapper<PlayerComponent>,
    private val stage: com.badlogic.gdx.scenes.scene2d.Stage,
):IteratingSystem() {
    override fun onTickEntity(entity: Entity) {
        val attackCmp = attackCmps[entity]

        if(attackCmp.isReady && attackCmp.doAttack){
            // entity does not want to attack and is not executing an attack -> do nothing
            return
        }
        if(attackCmp.isPrepared && attackCmp.doAttack){
            // attack intention and is ready to attack -> start attack
            attackCmp.doAttack = false
            attackCmp.state = AttackState.ATTACKING
            attackCmp.delay = attackCmp.maxDelay
            stage.fire(EntityAttackEvent(animationCmps[entity].model))
            return
        }
        attackCmp.delay -= deltaTime
        if (attackCmp.delay <= 0f && attackCmp.isAttacking){
            attackCmp.state = AttackState.DEAL_DAMAGE

            val image = imageCmps[entity].image
            val physicCmp = physicCmps[entity]
            val attackLeft = image.flipX
            val (x, y) = physicCmp.body.position
            val (offX, offY) = physicCmp.offset
            val (w, h) = physicCmp.size
            val halfW = w * 0.5f
            val halfH = h * 0.5f

            if(attackLeft){
                AABB_RECT.set(
                    x + offX - halfW - attackCmp.extraRange,
                    y + offY - halfH,
                    x + offX + halfW,
                    y + offY + halfH
                    )
            }else{
                AABB_RECT.set(
                    x + offX - halfW,
                    y + offY - halfH,
                    x + offX + halfW + attackCmp.extraRange,
                    y + offY + halfH
                )
            }

            phWorld.query(AABB_RECT.x, AABB_RECT.y, AABB_RECT.width, AABB_RECT.height){fixture ->
                if(fixture.userData != HIT_BOX_SENSOR){
                    return@query true
                }
                val fixtureEntity = fixture.entity
                if (fixtureEntity == entity){
                    return@query true
                }

                val isAttackerPlayer = entity in playerCmps
                if (isAttackerPlayer && fixtureEntity in playerCmps){
                    return@query true
                }else if (!isAttackerPlayer && fixtureEntity !in playerCmps){
                    return@query true
                }

                configureEntity(fixtureEntity){
                    lifeCmps.getOrNull(it)?.let{lifeCmp ->
                        lifeCmp.takeDamage += attackCmp.damage * MathUtils.random(0.9f, 1.1f)
                    }
                    if (entity in playerCmps){
                        dialogCmps.getOrNull(it)?.let{ dialogCmp ->
                            dialogCmp.interactEntity = entity

                        }
                        lootCmps.getOrNull(it)?.let{ lootCmp ->
                            lootCmp.interactEntity = entity

                        }
                    }

                }
                return@query true
            }
        }
        val isDone = animationCmps.getOrNull(entity)?.isAnimationDone ?: true
        if(isDone){
            attackCmp.state = AttackState.READY
        }
    }

    companion object{
        val AABB_RECT = com.badlogic.gdx.math.Rectangle()
    }
}
