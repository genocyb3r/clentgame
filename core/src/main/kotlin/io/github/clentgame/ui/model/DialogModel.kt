package io.github.clentgame.ui.model

import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import io.github.clentgame.dialog.Dialog
import io.github.clentgame.event.EntityDialogEvent

class DialogModel(
    stage: Stage,
): PropertyChangeSource(), EventListener {
    private lateinit var dialog: Dialog
    var text by propertyNotifier("")
    var options by propertyNotifier(listOf<DialogOptionModel>())
    var completed by propertyNotifier(false)

    init {
        stage.addListener(this)
    }
    fun triggerOption(optionIdx: Int){
        dialog.triggerOption(optionIdx)
        updateTextAndOptions()
    }
    private fun updateTextAndOptions() {
        completed = dialog.isComplete()
        if (!completed){
            text = dialog.currentNode.text
            options = dialog.currentNode.options.map {DialogOptionModel(it.id, it.text)}

        }
    }

    override fun handle(event: Event?): Boolean {
        when (event){
            is EntityDialogEvent -> {
                this.dialog = event.dialog
                updateTextAndOptions()
            }
            else -> return false
        }
        return true
    }


}
