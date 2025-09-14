package io.github.clentgame.system

import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import io.github.clentgame.component.DialogComponent
import io.github.clentgame.component.DialogId
import io.github.clentgame.component.MoveComponent
import io.github.clentgame.dialog.Dialog
import io.github.clentgame.dialog.dialog
import io.github.clentgame.event.EntityDialogEvent
import io.github.clentgame.event.fire
import ktx.app.gdxError


@AllOf([DialogComponent::class])
class DialogSystem(
    private val dialogCmps: ComponentMapper<DialogComponent>,
    private val moveCmps: ComponentMapper<MoveComponent>,
    private val disarmCmps: ComponentMapper<io.github.clentgame.component.DisarmComponent>,
    private val stage: Stage,
): IteratingSystem() {

    private val dialogCache = mutableMapOf<DialogId, Dialog>()

    override fun onTickEntity(entity: Entity) {
        with(dialogCmps[entity]){
            val triggerEntity = interactEntity
            if (triggerEntity == null) {
                return
            }

            var dialog: Dialog? =  currentDialog

            if (dialog != null ) {
                if (dialog.isComplete()){
                    moveCmps.getOrNull(triggerEntity)?.let { it.root = false }
                    configureEntity(triggerEntity){disarmCmps.remove(it)}
                    currentDialog = null
                    interactEntity = null
                    return
                }
                return
            }

            dialog = getDialog(dialogId).also{it.start()}
            currentDialog = dialog
            moveCmps.getOrNull(triggerEntity)?.let { it.root = false }
            configureEntity(triggerEntity){disarmCmps.add(it)}
            stage.fire(EntityDialogEvent(dialog))

        }

    }
    private fun getDialog(dialogId: DialogId): Dialog {
        return dialogCache.getOrPut(dialogId){
            when (dialogId) {
                DialogId.BLOB -> dialog(dialogId.name) {
                    node(0, "Hello adventurer! Can you please take care of my crazy blue brothers?") {
                        option("But why?") {
                            action = { this@dialog.goToNode(1) }
                        }
                    }
                    node(1, "A dark magic has possessed them. There is no cure - KILL EM ALL!!!") {
                        option("Again?") {
                            action = { this@dialog.goToNode(0) }
                        }

                        option("Ok, ok") {
                            action = { this@dialog.end() }
                        }
                    }
                }

                else -> gdxError("No dialog configured for ${dialogId}.")
            }
        }
    }


}
