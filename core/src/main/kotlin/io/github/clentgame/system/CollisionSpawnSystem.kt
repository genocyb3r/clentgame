package io.github.clentgame.system

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import io.github.clentgame.component.CollisionComponent
import io.github.clentgame.component.PhysicComponent
import io.github.clentgame.component.PhysicComponent.Companion.physicCmpFromShape2D
import io.github.clentgame.component.TiledComponent
import io.github.clentgame.event.CollisionDespawnEvent
import io.github.clentgame.event.MapChangeEvent
import ktx.box2d.body
import ktx.box2d.loop
import ktx.collections.GdxArray
import ktx.math.component1
import ktx.math.component2
import ktx.math.vec2
import ktx.tiled.height
import ktx.tiled.isEmpty
import ktx.tiled.shape
import ktx.tiled.width


@AllOf([PhysicComponent::class, CollisionComponent::class])
class CollisionSpawnSystem(
    private val phWorld: com.badlogic.gdx.physics.box2d.World,
    private val physicCmps: ComponentMapper<PhysicComponent>,
) :EventListener, IteratingSystem() {

    private val tiledLayers = GdxArray<TiledMapTileLayer>()
    private val processedCells = mutableSetOf<TiledMapTileLayer.Cell>()

    private fun TiledMapTileLayer.forEachCell(
        startX: Int,
        startY: Int,
        size: Int,
        action: (TiledMapTileLayer.Cell, Int, Int) -> Unit
    ){
        for (x in startX-size until startX + size) {
            for (y in startY - size until startY + size) {
                this.getCell(x, y)?.let { action(it, x, y) }
            }
        }
    }

    override fun onTickEntity(entity: Entity) {
        val (entityX, entityY) = physicCmps[entity].body.position

        tiledLayers.forEach {layer ->
            layer.forEachCell(entityX.toInt(), entityY.toInt(),SPAWN_AREA_SIZE){cell, x, y ->
                if(cell.tile.objects.isEmpty()) {
                    // cell is not link to any collision object
                    return@forEachCell
                }
                if(cell in processedCells) {
                    return@forEachCell
                }
                processedCells.add(cell)
                cell.tile.objects.forEach {mapObject ->
                    world.entity(){
                        physicCmpFromShape2D(phWorld, x, y, mapObject.shape)
                        add<TiledComponent>{
                            this.cell = cell
                            nearbyEntities.add(entity)
                        }
                    }

                }
            }

        }


    }

    override fun handle(event: Event?): Boolean {
        when (event) {
            is MapChangeEvent -> {
                event.map.layers.getByType(TiledMapTileLayer::class.java, tiledLayers)

                world.entity {
                    val w = event.map.width
                    val h = event.map.height

                    add<PhysicComponent> {
                        body = phWorld.body(BodyDef.BodyType.StaticBody) {
                            position.set(0f, 0f)
                            fixedRotation = true
                            allowSleep = false

                            loop(
                                vec2(0f, 0f),
                                vec2(w.toFloat(), 0f),
                                vec2(w.toFloat(), h.toFloat()),
                                vec2(0f, h.toFloat())
                            )

                        }
                    }

                }
                return true


            }
            is CollisionDespawnEvent -> {
                processedCells.remove(event.cell)
                return true
            }
            else -> return false
        }

    }
    companion object{
        const val SPAWN_AREA_SIZE = 7
    }
}
