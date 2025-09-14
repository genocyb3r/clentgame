package io.github.clentgame.ui.view

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop
import com.badlogic.gdx.utils.Align
import io.github.clentgame.ui.Drawables
import io.github.clentgame.ui.Labels
import io.github.clentgame.ui.get
import io.github.clentgame.ui.model.InventoryModel
import io.github.clentgame.ui.model.ItemModel
import io.github.clentgame.ui.widget.InventoryDragSource
import io.github.clentgame.ui.widget.InventoryDragTarget
import io.github.clentgame.ui.widget.InventorySlot
import io.github.clentgame.ui.widget.inventorySlot
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.label
import ktx.scene2d.table


class InventoryView(
    private val model: InventoryModel,
    skin: Skin,

): KTable, Table(skin) {

    private val invSlots = mutableListOf<InventorySlot>()
    private val gearSlots = mutableListOf<InventorySlot>()

    init {
        // UI
        val titlePadding = 15f
        setFillParent(true)

        table{inventoryTableCell ->
            background = skin[Drawables.FRAME_BGD]
            label("Inventory", Labels.TITLE.skinKey, skin){
                this.setAlignment(Align.center)
                it.expandX().fill()
                    .pad(8f, titlePadding, 0f, titlePadding)
                    .top()
                    .row()

            }
            table { invSlotTableCell ->
                for (i in 1 .. 18){
                    this@InventoryView.invSlots += inventorySlot(skin=skin){slotCell ->
                        slotCell.padBottom(2f)
                        if (i%6 == 0){
                            slotCell.row()
                        }else{
                            slotCell.padRight(2f)
                        }
                    }

                }
                invSlotTableCell.expand().fill()

            }
            inventoryTableCell.expand().width(150f).height(120f).center()

        }
        table {gearTableCell ->
            background = skin[Drawables.FRAME_BGD]
            label("GEAR", Labels.TITLE.skinKey, skin){
                this.setAlignment(Align.center)
                it.expandX().fill()
                    .pad(8f, titlePadding, 0f, titlePadding)
                    .top()
                    .row()

            }
            table { gearInnerTableCell ->
                this@InventoryView.gearSlots += inventorySlot(Drawables.INVENTORY_SLOT_HELMET, skin){
                    it.padBottom(2f).colspan(2).row()
                }
                this@InventoryView.gearSlots += inventorySlot(Drawables.INVENTORY_SLOT_WEAPON, skin){
                    it.padBottom(2f).padRight(2f)
                }
                this@InventoryView.gearSlots += inventorySlot(Drawables.INVENTORY_SLOT_ARMOR, skin){
                    it.padBottom(2f).row()
                }
                this@InventoryView.gearSlots += inventorySlot(Drawables.INVENTORY_SLOT_BOOTS, skin){
                    it.colspan(2).row()
                }
                gearInnerTableCell.expand().fill()

            }
            gearTableCell.expand().width(90f).height(120f).left().center()

        }
        setUpDragAndDrop()

        // data binding

        model.onPropertyChange(InventoryModel::playerItems){ itemModels ->
            clearInventoryAndGear()
            itemModels.forEach {
                if (it.equipped){
                    gear(it)
                }else{
                    item(it)

                }
            }
        }
    }

    private fun clearInventoryAndGear(){
        invSlots.forEach { it.item(null) }
        gearSlots.forEach { it.item(null) }

    }


    private fun setUpDragAndDrop(){
        val dnd = DragAndDrop()
         dnd.setDragActorPosition(
             InventoryDragSource.DRAG_ACTOR_SIZE*0.5f,
             -InventoryDragSource.DRAG_ACTOR_SIZE *0.5f
         )

        invSlots.forEach { slot ->
            dnd.addSource(InventoryDragSource(slot))
            dnd.addTarget(InventoryDragTarget(slot, ::onItemDropped))
        }

        gearSlots.forEach { slot ->
            dnd.addSource(InventoryDragSource(slot))
            dnd.addTarget(InventoryDragTarget(slot, ::onItemDropped, slot.supportedCategory))
        }
    }

    private fun onItemDropped(sourceSlot: InventorySlot, targetSlot: InventorySlot, itemModel: ItemModel){
        if (sourceSlot == targetSlot) {
            // item dropped on same slot -> do nothing
            return
        }
        //item swap
        sourceSlot.item(targetSlot.itemModel)
        targetSlot.item(itemModel)

        //update model

        val sourceItem = sourceSlot.itemModel
        if (sourceSlot.isGear){
            model.equip(itemModel, false)
            if (sourceItem != null){
                model.equip(sourceItem, true)
            }
        }else if (sourceItem!= null){
            model.inventoryItem(invSlots.indexOf(sourceSlot), sourceItem)
        }

        if (targetSlot.isGear){
            model.equip(itemModel, true)
            if (sourceItem != null){
                model.equip(sourceItem, false)
            }
        }else {
            model.inventoryItem(invSlots.indexOf(sourceSlot), itemModel)
        }





    }



    fun item(itemModel: ItemModel){
        invSlots[itemModel.slotIdx].item(itemModel)
    }

    fun gear(itemModel: ItemModel){
        gearSlots.firstOrNull{it.supportedCategory == itemModel.category}?.item(itemModel)
    }
}

@Scene2dDsl
fun <S> KWidget<S>.inventoryView(
    model: InventoryModel,
    skin: Skin = Scene2DSkin.defaultSkin,
    init: InventoryView.(S) -> Unit = {},
): InventoryView = actor(InventoryView(model, skin), init)
