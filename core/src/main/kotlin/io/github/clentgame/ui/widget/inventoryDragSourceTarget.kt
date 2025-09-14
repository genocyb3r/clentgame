package io.github.clentgame.ui.widget

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop
import io.github.clentgame.component.ItemCategory
import io.github.clentgame.ui.model.ItemModel

class InventoryDragSource(
    val inventorySlot: InventorySlot
): DragAndDrop.Source(inventorySlot){
    val isGear: Boolean
        get() = inventorySlot.isGear

    val supportedCategory: ItemCategory
        get()  = inventorySlot.supportedCategory

    override fun dragStart(
        event: InputEvent,
        x: Float,
        y: Float,
        pointer: Int
    ): DragAndDrop.Payload? {
        if (inventorySlot.itemModel == null){
            return null
        }
        return DragAndDrop.Payload().apply {
            `object` = inventorySlot.itemModel
            dragActor = Image(inventorySlot.itemDrawable).apply {
                setSize(DRAG_ACTOR_SIZE, DRAG_ACTOR_SIZE)
            }
            inventorySlot.item(null)
        }

    }

    override fun dragStop(
        event: InputEvent,
        x: Float,
        y: Float,
        pointer: Int,
        payload: DragAndDrop.Payload,
        target: DragAndDrop.Target?
    ) {
        if (target == null){
            val item = payload.`object` as? ItemModel
            item?.let { inventorySlot.item(it) }
        }
    }

    companion object{
        const val DRAG_ACTOR_SIZE = 22F
    }

}


class InventoryDragTarget(
    private val inventorySlot: InventorySlot,
    private val onDrop: (sourceSlot: InventorySlot, targetSlot: InventorySlot, itemModel: ItemModel) -> Unit,
    private val supportedItemCategory: ItemCategory? = null,
): DragAndDrop.Target(inventorySlot){

    private val isGear: Boolean
        get() = supportedItemCategory != null

    private fun isSupported(category: ItemCategory): Boolean = supportedItemCategory == category

    override fun drag(
        source: DragAndDrop.Source,
        payload: DragAndDrop.Payload,
        x: Float,
        y: Float,
        pointer: Int
    ): Boolean {
        val itemModel = payload.`object` as ItemModel
        val dragSource = source as InventoryDragSource
        val srcCategory = dragSource.supportedCategory

        return if (isGear && isSupported(itemModel.category)){
            true
        }else if (!isGear && dragSource.isGear && (inventorySlot.isEmpty || inventorySlot.itemCategory == srcCategory)){
            true
        }else if(!isGear && !dragSource.isGear){
            true
        }else{
            payload.dragActor.color = Color.RED
            false
        }
    }

    override fun reset(source: DragAndDrop.Source, payload: DragAndDrop.Payload) {
       payload.dragActor.color = Color.WHITE
    }

    override fun drop(
        source: DragAndDrop.Source,
        payload: DragAndDrop.Payload,
        x: Float,
        y: Float,
        pointer: Int
    ) {
        onDrop(
            (source as InventoryDragSource).inventorySlot,
            actor as InventorySlot,
            payload.`object` as ItemModel
        )
    }

}

