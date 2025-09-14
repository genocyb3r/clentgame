package io.github.clentgame.system

import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import io.github.clentgame.component.InventoryComponent
import io.github.clentgame.component.ItemComponent
import io.github.clentgame.component.ItemType
import io.github.clentgame.event.EntityAddItemEvent
import io.github.clentgame.event.fire


@AllOf([InventoryComponent::class])
class InventorySystem(
    private val inventoryCmps: ComponentMapper<InventoryComponent>,
    private val itemCmps: ComponentMapper<ItemComponent>,
    private val gameStage: Stage,
): IteratingSystem() {

    override fun onTickEntity(entity: Entity) {
        val inventory = inventoryCmps[entity]
        if (inventory.itemsToAdd.isEmpty()){
            return
        }
        inventory.itemsToAdd.forEach { itemType ->
            val slotIdx: Int = emptySlotIndex(inventory)
            if (slotIdx == -1){
                return
            }
            val newItem = spawnItem(itemType, slotIdx)
            inventory.items += newItem
            gameStage.fire(EntityAddItemEvent(entity, newItem))
        }
        inventory.itemsToAdd.clear()
    }
    private fun spawnItem(itemType: ItemType, slotIdx: Int) : Entity {
        return world.entity {
            add<ItemComponent>{
                this.itemType = itemType
                this.slotIdx = slotIdx
            }
        }
    }

    private fun emptySlotIndex(inventory: InventoryComponent): Int {
        for (i in 0 until InventoryComponent.INVENTORY_CAPACITY){
            if (inventory.items.none { itemCmps[it].slotIdx == i }){
                return i
            }
        }
        return -1
    }
}
