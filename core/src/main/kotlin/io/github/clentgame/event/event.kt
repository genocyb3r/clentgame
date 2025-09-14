package io.github.clentgame.event

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.Entity
import io.github.clentgame.component.AnimationModel
import io.github.clentgame.dialog.Dialog

fun Stage.fire(event: Event){
    this.root.fire(event)
}

data class MapChangeEvent(val map: TiledMap): Event()

class CollisionDespawnEvent(val cell: TiledMapTileLayer.Cell): Event()

class EntityAttackEvent(val model: AnimationModel): Event()

class EntityDeathEvent(val model: AnimationModel): Event()

class EntityLootEvent(val model: AnimationModel): Event()

class EntityDamageEvent(val entity: Entity): Event()

class EntityAggroEvent(val entity: Entity): Event()

class EntityAddItemEvent(val entity: Entity, val item: Entity): Event()

class EntityDialogEvent(val dialog: Dialog): Event()

class GamePauseEvent: Event()
class GameResumeEvent: Event()
