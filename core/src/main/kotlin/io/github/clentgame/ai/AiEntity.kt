package io.github.clentgame.ai



import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import io.github.clentgame.component.AnimationComponent
import io.github.clentgame.component.AnimationType
import io.github.clentgame.component.AttackComponent
import io.github.clentgame.component.MoveComponent
import io.github.clentgame.component.StateComponent
import io.github.clentgame.event.fire
import ktx.math.component1
import ktx.math.component2
import ktx.math.vec2


private val TMP_RECT = Rectangle()
data class AiEntity(
    val entity: Entity,
    private val world: World,
    private val stage: Stage,
    private val moveCmps: ComponentMapper<MoveComponent> = world.mapper(),
    private val attackCmps: ComponentMapper<AttackComponent> = world.mapper(),
    private val stateCmps: ComponentMapper<StateComponent> = world.mapper(),
    private val animationCmps: ComponentMapper<AnimationComponent> = world.mapper(),
    private val lifeCmps: ComponentMapper<io.github.clentgame.component.LifeComponent> = world.mapper(),
    private val physiCmps: ComponentMapper<io.github.clentgame.component.PhysicComponent> = world.mapper(),
    private val aiCmps: ComponentMapper<io.github.clentgame.component.AIComponent> = world.mapper(),
    private val playerCmps: ComponentMapper<io.github.clentgame.component.PlayerComponent> = world.mapper(),

    ) {
    val position: com.badlogic.gdx.math.Vector2
        get() = physiCmps[entity].body.position

    val wantsToRun: Boolean
        get() {
            val moveCmp = moveCmps[entity]
            return moveCmp.cos != 0f || moveCmp.sin != 0f
        }

    val wantsToAttack: Boolean
        get() = attackCmps.getOrNull(entity)?.doAttack ?: false

    val attackCmp: AttackComponent
        get() = attackCmps[entity]

    val isAnimationDone: Boolean
        get() = animationCmps[entity].isAnimationDone

    val isDead: Boolean
        get() = lifeCmps[entity].isDead


    fun animation(
        type: AnimationType,
        mode: Animation.PlayMode = Animation.PlayMode.LOOP,
        resetAnimation: Boolean = false
    ) {
        with(animationCmps[entity]) {
            nextAnimation(type)
            playMode = mode
            if (resetAnimation) {
                stateTime = 0f
            }


        }

    }

    fun state(next: EntityState, immediateChange: Boolean = false) {
        with(stateCmps[entity]) {
            nextState = next
            if (immediateChange) {
                stateMachine.changeState(nextState)
            }
        }
    }
    fun enableGlobalState(enable: Boolean) {
        with(stateCmps[entity]) {
            if (enable) {
                stateMachine.globalState = DefaultGlobalState.CHECK_ALIVE
            } else {
                stateMachine.globalState = null
            }
        }
    }

    fun root(enable: Boolean) {
        with(moveCmps[entity]) { root = enable }

    }

    fun startAttack() {
        with(attackCmps[entity]) { startAttack() }

    }

    fun changeToPreviousState() {
        with(stateCmps[entity]) { nextState = stateMachine.previousState }
    }

    fun doAndStartAttack(){
        with(attackCmps[entity]){
            doAttack = true
            startAttack()
        }
    }

    fun moveTo(target: Vector2) {
        val (targetX, targetY) = target
        val physicCmp = physiCmps[entity]
        val (sourceX, sourceY) = physicCmp.body.position
        with(moveCmps[entity]) {
            val angleRad = MathUtils.atan2(targetY-sourceY, targetX-sourceX)
            cos = MathUtils.cos(angleRad)
            sin = MathUtils.sin(angleRad)
        }
    }

    fun inRange(range: Float, target: Vector2): Boolean {
        val physicCmp = physiCmps[entity]
        val (sourceX, sourceY) = physicCmp.body.position
        val (offsetX, offsetY) = physicCmp.offset
        var (sizeX, sizeY) = physicCmp.size
        sizeX += range
        sizeY += range

        TMP_RECT.set(
            sourceX + offsetX - sizeX * 0.5f,
            sourceY + offsetY - sizeY * 0.5f,
            sizeX,
            sizeY
        )
        return TMP_RECT.contains(target)

    }

    fun stopMovement() {
        with(moveCmps[entity]) {
            cos = 0f
            sin = 0f
        }
    }
    fun slowDown(factor: Float = 0.9f, threshold: Float = 0.01f) {
        with(moveCmps[entity]) {
            cos *= factor
            sin *= factor

            if (cos * cos + sin * sin < threshold * threshold) {
                cos = 0f
                sin = 0f
            }
        }
    }
    fun canAttack(): Boolean {
        val attackCmp = attackCmps.getOrNull(entity) ?: return false
        if (!attackCmp.isReady) return false

        val enemy = nearbyEnemies().firstOrNull() ?: return false
        val enemyPhysicCmp = physiCmps[enemy]
        val (sourceX, sourceY) = enemyPhysicCmp.body.position
        val (offsetX, offsetY) = enemyPhysicCmp.offset
        return inRange(1.5f, vec2(sourceX + offsetX, sourceY + offsetY))
    }

    private fun nearbyEnemies(): List<Entity> {
        val aiCmp = aiCmps[entity]
        return aiCmp.nearbyEntities
            .filter {  it in playerCmps && !lifeCmps[it].isDead }
    }
    fun hasEnemyNearby() = nearbyEnemies().isNotEmpty()
    fun fireEvent(event: Event){
        stage.fire(event)
    }

}

