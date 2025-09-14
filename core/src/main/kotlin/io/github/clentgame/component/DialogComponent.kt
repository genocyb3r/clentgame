package io.github.clentgame.component

import com.github.quillraven.fleks.Entity
import io.github.clentgame.dialog.Dialog


enum class DialogId {
    NONE,
    BLOB;
}

data class DialogComponent(
    var dialogId: DialogId = DialogId.NONE,

) {
    var interactEntity: Entity? = null
    var currentDialog: Dialog? = null
}
