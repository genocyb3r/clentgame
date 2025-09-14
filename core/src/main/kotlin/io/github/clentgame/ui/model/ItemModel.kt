package io.github.clentgame.ui.model

import io.github.clentgame.component.ItemCategory

class ItemModel(
    val itemEntityId: Int,
    val category: ItemCategory,
    val atlatlasKey: String,
    var slotIdx: Int,
    var equipped: Boolean,
)
