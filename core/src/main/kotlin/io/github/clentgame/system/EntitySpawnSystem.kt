package io.github.clentgame.system

import box2dLight.PointLight
import box2dLight.RayHandler
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.utils.Scaling
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import io.github.clentgame.ClentGame.Companion.UNIT_SCALE
import io.github.clentgame.actor.FlipImage
import io.github.clentgame.component.AnimationComponent
import io.github.clentgame.component.AnimationModel
import io.github.clentgame.component.AnimationType
import io.github.clentgame.component.AttackComponent
import io.github.clentgame.component.CollisionComponent
import io.github.clentgame.component.DEFAULT_ATTACK_DAMAGE
import io.github.clentgame.component.DEFAULT_LIFE
import io.github.clentgame.component.DEFAULT_SPEED
import io.github.clentgame.component.DialogId
import io.github.clentgame.component.ImageComponent
import io.github.clentgame.component.InventoryComponent
import io.github.clentgame.component.ItemType
import io.github.clentgame.component.LifeComponent
import io.github.clentgame.component.LightComponent
import io.github.clentgame.component.LootComponent
import io.github.clentgame.component.MoveComponent
import io.github.clentgame.component.PhysicComponent
import io.github.clentgame.component.PhysicComponent.Companion.bodyFromImageAndCfg
import io.github.clentgame.component.PlayerComponent
import io.github.clentgame.component.SpawnCfg
import io.github.clentgame.component.SpawnComponent
import io.github.clentgame.component.StateComponent
import io.github.clentgame.event.MapChangeEvent
import ktx.app.gdxError
import ktx.box2d.circle
import ktx.math.vec2
import ktx.tiled.layer
import ktx.tiled.property
import ktx.tiled.type
import ktx.tiled.x
import ktx.tiled.y
import kotlin.math.roundToInt

@AllOf([SpawnComponent::class])
class EntitySpawnSystem(
    private val phWorld: World,
    private val atlas: TextureAtlas,
    private val spawnCpms:ComponentMapper<SpawnComponent>,
    private val rayHandler: RayHandler,
) : EventListener, IteratingSystem(){
    private val cachedCfgs = mutableMapOf<String, SpawnCfg>()
    private val cachedSizes = mutableMapOf<AnimationModel, Vector2>()
    private val playerEntities = world.family(allOf = arrayOf(PlayerComponent::class))

    override fun onTickEntity(entity: Entity) {
        with(spawnCpms[entity]) {
            val cfg = spawnCfg(type)
            val relativeSize = size(cfg.model)

            world.entity {
               val imageCmp = add<ImageComponent> {
                    image = FlipImage().apply {
                        setPosition(location.x, location.y)
                        setSize(relativeSize.x, relativeSize.y)
                        setScaling(Scaling.fill)
                        color = this@with.color
                    }
                }
                add<AnimationComponent> {
                    nextAnimation(cfg.model, AnimationType.IDLE)
                }

                val physicCmp = add<PhysicComponent>{
                    body = bodyFromImageAndCfg(phWorld, imageCmp.image, cfg)
                }

                if (cfg.dialogId != DialogId.NONE){
                    add<io.github.clentgame.component.DialogComponent>{
                        dialogId = cfg.dialogId
                    }
                }

                if (cfg.hasLight){
                    add<LightComponent>{
                        distance = 5f..6f
                        light = PointLight(rayHandler, 64, LightComponent.lightColor, distance.endInclusive, 0f,0f).apply{
                            this.attachToBody(physicCmp.body)
                        }
                        light.setSoftnessLength(3.5f)

                    }
                }

                if(cfg.speedScaling > 0f){
                    add<MoveComponent>{
                        speed = DEFAULT_SPEED * cfg.speedScaling
                    }
                }
                if(cfg.canAttack){
                    add<AttackComponent>{
                        maxDelay = cfg.attackDelay
                        damage = (DEFAULT_ATTACK_DAMAGE * cfg.attackScaling).roundToInt()
                        extraRange = cfg.attackExtraRange
                    }
                }
                if (cfg.lifeScaling > 0 ){
                    add<LifeComponent>{
                        maxLife = DEFAULT_LIFE * cfg.lifeScaling
                        life = maxLife

                    }
                }

                if(type == "Player"){
                    add<PlayerComponent>()
                    add<StateComponent>()
                    add<InventoryComponent>(){
                        itemsToAdd += ItemType.SWORD
                        itemsToAdd += ItemType.BIG_SWORD
                        itemsToAdd += ItemType.HELMET
                        itemsToAdd += ItemType.ARMOR
                        itemsToAdd += ItemType.BOOTS
                    }
                }

                if(cfg.lootable){
                    add<LootComponent>()
                }

                if(cfg.bodyType != BodyDef.BodyType.StaticBody){
                    add<CollisionComponent>()
                }

                if (cfg.aiTreePath.isNotBlank()){
                    add<io.github.clentgame.component.AIComponent>{
                        treePath = cfg.aiTreePath
                    }
                    physicCmp.body.circle(4f){
                        isSensor = true
                        userData = AI_SENSOR

                    }
                }
            }
        }
        world.remove(entity)
    }

    private fun spawnCfg(type: String): SpawnCfg = cachedCfgs.getOrPut(type){
        when (type){
            "Player" -> PLAYER_CFG
            "Slime" -> SpawnCfg(
                AnimationModel.SLIME,
                lifeScaling = 0.5f,
                canAttack = true,
                speedScaling = 0.75f,
                attackScaling = 0.75f,
                physicScaling = vec2(0.3f, 0.3f),
                physicOffset = vec2(0f, -2f * UNIT_SCALE),
                aiTreePath = "ai/slime.tree")

            "Blob" -> SpawnCfg(
                AnimationModel.SLIME,
                lifeScaling = 0f,
                physicScaling = vec2(0.3f, 0.3f),
                physicOffset = vec2(0f, -2f * UNIT_SCALE),
                dialogId = DialogId.BLOB,
             )

            "Chest" -> SpawnCfg(
                AnimationModel.CHEST,
                canAttack = false,
                lootable = true,
                bodyType = BodyDef.BodyType.StaticBody,
                physicCategory = LightComponent.environmentCategory,
                lifeScaling = 0f,
            )

            else -> gdxError("Type $type has no SpawnCfg setup!")
        }

    }

    private fun size(model: AnimationModel)= cachedSizes.getOrPut(model){
        val regions = atlas.findRegions("${model.atlasKey}/${AnimationType.IDLE.atlasKey}")
        if (regions.isEmpty){
            gdxError("There are no regions for ${model.atlasKey}/${AnimationType.IDLE.atlasKey}")
        }

        val firstFrame = regions.first()
        vec2(firstFrame.originalWidth * UNIT_SCALE, firstFrame.originalHeight * UNIT_SCALE)

    }

    override fun handle(event: Event): Boolean {
        when (event) {
            is MapChangeEvent -> {
                val entityLayer = event.map.layer("entities")
                entityLayer.objects.forEach { mapObj ->
                    val type = mapObj.type ?: gdxError("Map object $mapObj does not have a type")
                    if (type == "Player" && playerEntities.isNotEmpty){
                        return@forEach
                    }
                    world.entity {
                        add<SpawnComponent> {
                            this.type = type
                            this.location.set(mapObj.x * UNIT_SCALE, mapObj.y * UNIT_SCALE)
                            this.color = mapObj.property("color", Color.WHITE)
                        }
                    }
                }
                return true
            }
        }
        return false
    }
    companion object{
        const val HIT_BOX_SENSOR = "HitBox"
        const val AI_SENSOR = "AiSensor"
        val PLAYER_CFG = SpawnCfg(
            AnimationModel.PLAYER,
            lifeScaling = 0.5f,
            attackExtraRange = 0.6f,
            attackScaling = 1.25f,
            physicScaling = vec2(0.3f, 0.3f),
            physicOffset = vec2(0f, -10f * UNIT_SCALE),
            hasLight = true,
            physicCategory = LightComponent.playerCategory,
        )
        val log = ktx.log.logger<EntitySpawnSystem>()

    }
}
