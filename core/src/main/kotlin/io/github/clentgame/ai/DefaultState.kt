package io.github.clentgame.ai

import com.badlogic.gdx.graphics.g2d.Animation
import io.github.clentgame.component.AnimationType

enum class DefaultState: EntityState {
    IDLE{
        override fun enter(entity: AiEntity) {
            entity.animation(AnimationType.IDLE)
        }

        override fun update(entity: AiEntity) {
            when{
                entity.wantsToAttack -> entity.state(ATTACK)
                entity.wantsToRun -> entity.state(RUN)
            }
        }

    },
    RUN{
        override fun enter(entity: AiEntity) {
            entity.animation(AnimationType.RUN)

        }

        override fun update(entity: AiEntity) {
            when{
                entity.wantsToAttack -> entity.state(ATTACK)
                !entity.wantsToRun -> entity.state(IDLE)
            }
        }
    },
    ATTACK{
        override fun enter(entity: AiEntity) {
            entity.animation(AnimationType.ATTACK, Animation.PlayMode.NORMAL)
            entity.root(true)
            entity.startAttack()

        }

        override fun exit(entity: AiEntity) {
            entity.root(false)

        }

        override fun update(entity: AiEntity) {
            val attackCmp = entity.attackCmp
            if(attackCmp.isReady && !attackCmp.doAttack){
                entity.changeToPreviousState()
            }else if(attackCmp.isReady){
                entity.animation(AnimationType.ATTACK, Animation.PlayMode.NORMAL, true)
                entity.startAttack()

            }
        }

    },
    DEAD{
        override fun enter(entity: AiEntity) {
            entity.root(true)
        }

        override fun update(entity: AiEntity) {
            if(!entity.isDead){
                entity.state(RESURRECT)
            }
        }

    },
    RESURRECT{
        override fun enter(entity: AiEntity) {
            entity.enableGlobalState(true)
            entity.animation(AnimationType.DEATH, Animation.PlayMode.REVERSED)
        }

        override fun update(entity: AiEntity) {
            if(entity.isAnimationDone){
                entity.state(IDLE)
                entity.root(false)
            }
        }
    },
}

enum class DefaultGlobalState: EntityState {
    CHECK_ALIVE{
        override fun update(entity: AiEntity) {
            if(entity.isDead){
                entity.enableGlobalState(false)
                entity.state(DefaultState.DEAD, true)

            }
        }
    }
}

