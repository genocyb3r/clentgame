package io.github.clentgame.system

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.ContactImpulse
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.Fixture
import com.badlogic.gdx.physics.box2d.Manifold
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import io.github.clentgame.component.CollisionComponent
import io.github.clentgame.component.ImageComponent
import io.github.clentgame.component.PhysicComponent
import io.github.clentgame.component.TiledComponent
import io.github.clentgame.system.EntitySpawnSystem.Companion.AI_SENSOR
import ktx.log.logger
import ktx.math.component1
import ktx.math.component2

val Fixture.entity: Entity
    get() = this.body.userData as Entity

@AllOf([PhysicComponent::class, ImageComponent::class])
class PhysicSystem(
    private val phWorld: com.badlogic.gdx.physics.box2d.World,
    private val physicCmps: ComponentMapper<PhysicComponent>,
    private val imageCmps: ComponentMapper<ImageComponent>,
    private val tiledCmps: ComponentMapper<TiledComponent>,
    private val collisionCmps: ComponentMapper<CollisionComponent>,
    private val aiCmps: ComponentMapper<io.github.clentgame.component.AIComponent>,
    private val portalCmps: ComponentMapper<io.github.clentgame.component.PortalComponent>,
    private val playerCmps: ComponentMapper<io.github.clentgame.component.PlayerComponent>
):ContactListener, IteratingSystem(interval = Fixed(1/60f)) {

    init{
        phWorld.setContactListener(this)
    }
    override fun onUpdate() {
        if(phWorld.autoClearForces) {
            log.error { "AutoClearForces must be set to false to guarantee a correct physic simulation" }
            phWorld.autoClearForces = false
        }
        super.onUpdate()
        phWorld.clearForces()
    }

    override fun onTick() {
        super.onTick()
        phWorld.step(deltaTime, 6, 2)
    }

    override fun onTickEntity(entity: Entity) {
        val physicCmp = physicCmps[entity]

        physicCmp.prevPos.set(physicCmp.body.position)
        if(!physicCmp.impulse.isZero){
            physicCmp.body.applyLinearImpulse(physicCmp.impulse, physicCmp.body.worldCenter, true)
            physicCmp.impulse.isZero()
        }


    }

    override fun onAlphaEntity(entity: Entity, alpha: Float) {
        val physicCmp = physicCmps[entity]
        val imageCmp = imageCmps[entity]

        val (prevX, prevY) = physicCmp.prevPos
        val (bodyX, bodyY) = physicCmp.body.position
        imageCmp.image.run {
            setPosition(
                MathUtils.lerp(prevX, bodyX, alpha) - width * 0.5f,
                MathUtils.lerp(prevY, bodyY, alpha) - height *0.5f
            )

        }

    }

    override fun beginContact(contact: Contact) {
        val entityA:Entity = contact.fixtureA.entity
        val entityB:Entity = contact.fixtureB.entity
        val isEntityATiledCollisionSensor =  entityA in tiledCmps && contact.fixtureA.isSensor
        val isEntityBCollisionFixture = entityB in collisionCmps && !contact.fixtureB.isSensor
        val isEntityACollisionFixture =  entityA in collisionCmps && !contact.fixtureA.isSensor
        val isEntityBTiledCollisionSensor = entityB in tiledCmps && contact.fixtureB.isSensor
        val isEntityAAiSensor = entityA in aiCmps && contact.fixtureA.isSensor && contact.fixtureA.userData == AI_SENSOR
        val isEntityBAiSensor = entityB in aiCmps && contact.fixtureB.isSensor && contact.fixtureB.userData == AI_SENSOR

        when {
            isEntityATiledCollisionSensor && isEntityBCollisionFixture -> {
                tiledCmps[entityA].nearbyEntities += entityB
            }
            isEntityBTiledCollisionSensor && isEntityACollisionFixture -> {
                tiledCmps[entityB].nearbyEntities += entityA
            }
            isEntityAAiSensor && isEntityBCollisionFixture -> {
                aiCmps[entityA].nearbyEntities += entityB
            }
            isEntityBAiSensor && isEntityACollisionFixture -> {
                aiCmps[entityB].nearbyEntities += entityA
            }

            // portal
            entityA in portalCmps && entityB in playerCmps && !contact.fixtureB.isSensor -> {
                portalCmps[entityA].triggeringEntities += entityB
            }
            entityB in portalCmps && entityA in playerCmps && !contact.fixtureA.isSensor -> {
                portalCmps[entityB].triggeringEntities += entityA
            }



        }

    }

    override fun endContact(contact: Contact) {
        val entityA:Entity = contact.fixtureA.entity
        val entityB:Entity = contact.fixtureB.entity
        val isEntityATiledCollisionSensor =  entityA in tiledCmps && contact.fixtureA.isSensor
        val isEntityBTiledCollisionSensor = entityB in tiledCmps && contact.fixtureB.isSensor
        val isEntityAAiSensor = entityA in aiCmps && contact.fixtureA.isSensor && contact.fixtureA.userData == AI_SENSOR
        val isEntityBAiSensor = entityB in aiCmps && contact.fixtureB.isSensor && contact.fixtureB.userData == AI_SENSOR

        when {
            isEntityATiledCollisionSensor && !contact.fixtureB.isSensor-> {
                tiledCmps[entityA].nearbyEntities -= entityB
            }
            isEntityBTiledCollisionSensor && !contact.fixtureA.isSensor -> {
                tiledCmps[entityB].nearbyEntities -= entityA
            }
            isEntityAAiSensor && !contact.fixtureB.isSensor -> {
                aiCmps[entityA].nearbyEntities -= entityB
            }
            isEntityBAiSensor && !contact.fixtureA.isSensor -> {
                aiCmps[entityB].nearbyEntities -= entityA
            }
        }
    }

    private fun Body.isStaticBody() = this.type == BodyDef.BodyType.StaticBody
    private fun Body.isDynamicBody() = this.type == BodyDef.BodyType.DynamicBody


    override fun preSolve(contact: Contact, oldManifold: Manifold) {
        val bodyA = contact.fixtureA.body
        val bodyB = contact.fixtureB.body

        val isStaticDynamicPair =
            (bodyA.isStaticBody() && bodyB.isDynamicBody()) ||
                (bodyB.isStaticBody() && bodyA.isDynamicBody())

        contact.isEnabled = isStaticDynamicPair
    }

    override fun postSolve(contact: Contact?, impulse: ContactImpulse?) = Unit

    companion object{
        private val log =  logger<PhysicSystem>()
    }


}
