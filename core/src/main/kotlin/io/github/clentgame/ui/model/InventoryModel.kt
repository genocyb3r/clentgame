package io.github.clentgame.ui.model


import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.Stage
import io.github.clentgame.component.InventoryComponent
import io.github.clentgame.component.ItemComponent
import io.github.clentgame.component.PlayerComponent
import io.github.clentgame.event.EntityAddItemEvent
import ktx.log.logger


class InventoryModel(
    world: com.github.quillraven.fleks.World,
    gameStage: Stage
): PropertyChangeSource(), com.badlogic.gdx.scenes.scene2d.EventListener {

    private val playerCmps = world.mapper<PlayerComponent>()
    private val inventoryCmps = world.mapper<InventoryComponent>()
    private val itemCmps = world.mapper<ItemComponent>()
    private val playerEntities = world.family(allOf = arrayOf(PlayerComponent::class))

    var playerItems by propertyNotifier(listOf<ItemModel>())

    private val playerInventoryCmp: InventoryComponent
        get() = inventoryCmps[playerEntities.first()]

    init {
        gameStage.addListener(this)
    }

    override fun handle(event: Event?): Boolean {

        when(event){
            is EntityAddItemEvent -> {
                if (event.entity in playerCmps){
                    playerItems = inventoryCmps[event.entity].items.map {
                        val itemCmp = itemCmps[it]
                        ItemModel(
                            it.id,
                            itemCmp.itemType.category,
                            itemCmp.itemType.atlatlasKey,
                            itemCmp.slotIdx,
                            itemCmp.equipped,

                        )
                    }
                }
            }
            else -> return false
        }
        return true
    }

    private fun debugInventory(){
        log.debug { "\nInventory" }
        playerInventoryCmp.items.forEach { item ->
            log.debug { "${itemCmps[item]}" }
        }
        log.debug { "\n" }

    }

    fun equip(itemModel: ItemModel, equip: Boolean){
        playerItemByModel(itemModel).equipped = equip
        itemModel.equipped = equip
        debugInventory()
    }

    private fun playerItemByModel(itemModel: ItemModel): ItemComponent{
        return itemCmps[playerInventoryCmp.items.first { it.id == itemModel.itemEntityId }]
    }



    fun inventoryItem(slotIdx: Int, itemModel: ItemModel){
        playerItemByModel(itemModel).slotIdx = slotIdx
        itemModel.slotIdx = slotIdx
        debugInventory()

    }

    companion object{
        private val log = logger<InventoryModel>()
    }

}
