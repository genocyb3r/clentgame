package io.github.clentgame.ai

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ai.GdxAI
import com.badlogic.gdx.ai.btree.LeafTask
import com.badlogic.gdx.ai.btree.Task
import com.badlogic.gdx.ai.btree.annotation.TaskAttribute
import com.badlogic.gdx.ai.utils.random.FloatDistribution
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.math.MathUtils
import io.github.clentgame.component.AnimationType
import io.github.clentgame.event.EntityAggroEvent
import ktx.math.vec2

abstract class Action: LeafTask<AiEntity>(){
    val entity: AiEntity
        get() = `object`

    override fun copyTo(task: Task<AiEntity>) = task

}

class IdleTask(
    @JvmField
    @TaskAttribute(required = true)
    var duration: FloatDistribution? = null
): Action(){
    private var currentDuration = 0f
    override fun execute(): Status {
        if(status != Status.RUNNING){
            entity.animation(AnimationType.IDLE)
            currentDuration = duration?.nextFloat()?: 1f
            return Status.RUNNING
        }
        currentDuration -= GdxAI.getTimepiece().deltaTime
        if (currentDuration <= 0f){
            return Status.SUCCEEDED
        }

        return Status.RUNNING
    }

    override fun copyTo(task: Task<AiEntity>): Task<AiEntity> {
        (task as IdleTask).duration = duration
        return task
    }
}

class WanderTask : Action() {

    private val startPos = vec2()
    private val targetPos = vec2()

    private var durationTask = 5f // max duration in seconds
    private var elapsedTime = 0f  // timer tracker

    override fun execute(): Status {
        if (status != Status.RUNNING) {
            // Task just started
            entity.animation(AnimationType.RUN)
            if (startPos.isZero) {
                startPos.set(entity.position)
            }
            targetPos.set(startPos)
            targetPos.x += MathUtils.random(-3f, 3f)
            targetPos.y += MathUtils.random(-3f, 3f)

            entity.moveTo(targetPos)
            entity.slowDown()

            elapsedTime = 0f // reset timer
            return Status.RUNNING
        }
        // Update timer
        elapsedTime += Gdx.graphics.deltaTime
        // Timeout reached
        if (elapsedTime >= durationTask) {
            entity.stopMovement()
            return Status.SUCCEEDED // give up after timeout
        }
        // Reached target normally
        if (entity.inRange(0.5f, targetPos)) {
            entity.stopMovement()
            return Status.SUCCEEDED
        }
        return Status.RUNNING
    }
}

class AttackTask: Action(){
    override fun execute(): Status? {
        if (status != Status.RUNNING) {
            entity.animation(AnimationType.ATTACK, mode = Animation.PlayMode.NORMAL, true)
            entity.fireEvent(EntityAggroEvent(entity.entity))
            entity.doAndStartAttack()
            return Status.RUNNING
        }
        if (entity.isAnimationDone){
            entity.animation(AnimationType.IDLE)
            entity.stopMovement()
            return Status.SUCCEEDED
        }

        return Status.RUNNING
    }
}


