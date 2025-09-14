package io.github.clentgame.system

import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import io.github.clentgame.ClentGame.Companion.UNIT_SCALE
import io.github.clentgame.component.PhysicComponent
import io.github.clentgame.component.PhysicComponent.Companion.bodyFromImageAndCfg
import io.github.clentgame.component.PhysicComponent.Companion.physicCmpFromShape2D
import io.github.clentgame.component.PlayerComponent
import io.github.clentgame.component.PortalComponent
import io.github.clentgame.event.MapChangeEvent
import io.github.clentgame.system.EntitySpawnSystem.Companion.PLAYER_CFG
import ktx.app.gdxError
import ktx.assets.disposeSafely
import ktx.log.logger
import ktx.tiled.height
import ktx.tiled.id
import ktx.tiled.layer
import ktx.tiled.property
import ktx.tiled.shape
import ktx.tiled.width
import ktx.tiled.x
import ktx.tiled.y
import kotlin.math.log


@AllOf([PortalComponent::class])
class PortalSystem(
    private val phWorld: com.badlogic.gdx.physics.box2d.World,
    private val gameStage: Stage,
    private val portalCmps: ComponentMapper<PortalComponent>,
    private val physicCmps: ComponentMapper<PhysicComponent>,
    private val imageCmps: ComponentMapper<io.github.clentgame.component.ImageComponent>,
    private val lightCmps: ComponentMapper<io.github.clentgame.component.LightComponent>,

    ): IteratingSystem(), EventListener {

    private var currentMap : TiledMap? = null

    override fun onTickEntity(entity: Entity) {
        val (id, toMap, toPortal, triggeringEntities) = portalCmps[entity]
        if (triggeringEntities.isNotEmpty()) {
            val triggerEntity = triggeringEntities.first()
            triggeringEntities.clear()

            log.debug { "Entity $triggerEntity entered portal $id" }
            setMap("map/$toMap.tmx", toPortal)
        }

    }

    fun setMap(path: String, targetPortalId: Int = -1){
        currentMap.disposeSafely()
        //destroying non-player entities
        world.family(noneOf = arrayOf(PlayerComponent::class)).forEach { world.remove((it)) }

        val newMap = TmxMapLoader().load(path)
        gameStage.root.fire(MapChangeEvent(newMap))
        currentMap = newMap

        if (targetPortalId >= 0){
            //teleport player to target portal location
            world.family(allOf = arrayOf(PlayerComponent::class)).forEach {playerEntity ->
                val targetPortal = targetPortalById(newMap, targetPortalId)

                val image = imageCmps[playerEntity].image
                image.setPosition(
                    targetPortal.x * UNIT_SCALE - image.width * 0.5f + targetPortal.width * 0.5f * UNIT_SCALE,
                    targetPortal.y * UNIT_SCALE - targetPortal.height * 0.5f * UNIT_SCALE
                )


                configureEntity(playerEntity){
                    physicCmps.remove(it)
                    physicCmps.add(it) {
                        body = bodyFromImageAndCfg(phWorld, image, PLAYER_CFG)
                    }
                    lightCmps[it].light.attachToBody(physicCmps[it].body)

                }
            }
        }

    }

    private fun targetPortalById(map: TiledMap, portalId: Int): MapObject {
        return map.layer("portals").objects.first { it.id == portalId }
            ?: gdxError("There is no portal with id $portalId")
    }

    override fun handle(event: Event?): Boolean {
        if (event is MapChangeEvent){
            val portalLayer: MapLayer = event.map.layer("portals")
            portalLayer.objects.forEach { mapObj ->
                val toMap = mapObj.property("toMap", "")
                val toPortal = mapObj.property("toPortal", -1)

                if (toMap.isBlank()){
                    return@forEach
                }else if (toPortal == -1){
                    gdxError("Portal ${mapObj.id} does not have a toPortal property")
                }

                //spawn the portal
                world.entity {
                    add<PortalComponent>{
                        this.id = mapObj.id
                        this.toMap = toMap
                        this.toPortal = toPortal
                    }
                    physicCmpFromShape2D(phWorld, 0 , 0, mapObj.shape, true)
                }
            }

            return true
        }
        return false
    }
    companion object{
        val log = logger<PortalSystem>()
    }
}
