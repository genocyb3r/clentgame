package io.github.clentgame.component

import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Shape2D
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.github.quillraven.fleks.EntityCreateCfg
import io.github.clentgame.ClentGame.Companion.TILE_SIZE
import io.github.clentgame.ClentGame.Companion.UNIT_SCALE
import io.github.clentgame.system.CollisionSpawnSystem.Companion.SPAWN_AREA_SIZE
import io.github.clentgame.system.EntitySpawnSystem.Companion.HIT_BOX_SENSOR
import ktx.box2d.BodyDefinition
import ktx.box2d.body
import ktx.box2d.box
import ktx.box2d.circle
import ktx.box2d.loop
import ktx.math.vec2


class PhysicComponent {
    val impulse = vec2()
    val prevPos = vec2()
    val offset = vec2()
    val size = vec2()
    lateinit var body: Body

    companion object {

        fun EntityCreateCfg.physicCmpFromShape2D(
            world: com.badlogic.gdx.physics.box2d.World,
            tileX: Int, // tile index in map
            tileY: Int, // tile index in map
            shape: Shape2D,
            isPortal: Boolean = false,
        ): PhysicComponent {
            // Convert tile grid coordinates to pixel offsets
            val tileOffsetX = tileX * TILE_SIZE
            val tileOffsetY = tileY * TILE_SIZE

            when (shape) {
                // ─────────────────────────────────────────────
                // RECTANGLES
                is Rectangle -> {
                    val bodyX = (tileOffsetX + shape.x) * UNIT_SCALE
                    val bodyY = (tileOffsetY + shape.y) * UNIT_SCALE
                    val bodyW = shape.width * UNIT_SCALE
                    val bodyH = shape.height * UNIT_SCALE

                    return add {
                        body = world.body(BodyType.StaticBody) {
                            position.set(bodyX, bodyY)
                            fixedRotation = true
                            allowSleep = false
                            loop(
                                vec2(0f, 0f),
                                vec2(bodyW, 0f),
                                vec2(bodyW, bodyH),
                                vec2(0f, bodyH)
                            ) {
                                filter.categoryBits = LightComponent.environmentCategory
                                this.isSensor = isPortal
                            }

                            if (!isPortal){
                                circle(SPAWN_AREA_SIZE +2f){isSensor = true}
                            }

                        }
                    }
                }

                is Polygon -> {
                    val vertices = shape.transformedVertices
                    val worldVertices = Array(vertices.size / 2) { i ->
                        vec2(
                            (tileOffsetX + vertices[i * 2]) * UNIT_SCALE,
                            (tileOffsetY + vertices[i * 2 + 1]) * UNIT_SCALE
                        )
                    }

                    // Find polygon center
                    val centerX = worldVertices.map { it.x }.average().toFloat()
                    val centerY = worldVertices.map { it.y }.average().toFloat()

                    // Make vertices relative to center
                    val relativeVertices = worldVertices.map { v ->
                        vec2(v.x - centerX, v.y - centerY)
                    }.toTypedArray()

                    return add {
                        body = world.body(BodyType.StaticBody) {
                            position.set(centerX, centerY) // body placed at polygon center
                            fixedRotation = true
                            allowSleep = false
                            loop(*relativeVertices){
                                filter.categoryBits = LightComponent.environmentCategory
                                this.isSensor = isPortal
                            } // collision polygon
                            circle(SPAWN_AREA_SIZE + 2f) { isSensor = true } // sensor now centered
                        }
                    }
                }

//
//                // ─────────────────────────────────────────────
//                // CIRCLES
//                is Circle -> {
//                    val centerX = (tileOffsetX + shape.x) * UNIT_SCALE
//                    val centerY = (tileOffsetY + shape.y) * UNIT_SCALE
//                    val radius = shape.radius * UNIT_SCALE
//
//                    return add {
//                        body = world.body(BodyType.StaticBody) {
//                            position.set(centerX, centerY)
//                            fixedRotation = true
//                            allowSleep = false
//                            circle(radius)
//
//                        }
//                    }
//                }
//
//                // ─────────────────────────────────────────────
                else -> throw IllegalArgumentException("Shape $shape is not supported!")
            }
        }

        fun PhysicComponent.bodyFromImageAndCfg(
            world: com.badlogic.gdx.physics.box2d.World,
            image: Image,
            cfg: SpawnCfg,
        ) : Body {
            val x = image.x
            val y = image.y
            val width = image.width
            val height = image.height


            val phCmp: PhysicComponent = this
            return world.body(cfg.bodyType) {
                // Position at center of the image
                position.set(x + width * 0.5f, y + height * 0.5f)
                fixedRotation = true
                allowSleep = false


                    val w = width*cfg.physicScaling.x
                    val h = height*cfg.physicScaling.y
                    phCmp.offset.set(cfg.physicOffset)
                    phCmp.size.set(w, h)


                    box(w, h, cfg.physicOffset){
                        isSensor = cfg.bodyType != BodyDef.BodyType.StaticBody
                        userData = HIT_BOX_SENSOR
                        filter.categoryBits = cfg.physicCategory
                    }
                    if(cfg.bodyType != BodyDef.BodyType.StaticBody){
                        val collH = h * 0.6f
                        val collOffset = vec2().apply { set(cfg.physicOffset) }
                        collOffset.y -= h*0.5f-collH*0.5f
                        box(w, collH, collOffset){ filter.categoryBits = cfg.physicCategory}
                    }

                }

            }
        }

        class PhysicComponentListener: com.github.quillraven.fleks.ComponentListener<PhysicComponent> {
            override fun onComponentAdded(entity: com.github.quillraven.fleks.Entity, component: PhysicComponent) {
                component.body.userData = entity
            }
            override fun onComponentRemoved(entity: com.github.quillraven.fleks.Entity, component: PhysicComponent) {
                val body = component.body
                body.world.destroyBody(body)
                body.userData = null
            }
        }
    }



