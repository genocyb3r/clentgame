package io.github.clentgame.component

enum class ItemCategory {
    UNDEFINED,
    HELMET,
    ARMOR,
    WEAPON,
    BOOTS;
}

enum class ItemType(
    val category: ItemCategory,
    val atlatlasKey: String,
) {
    UNDEFINED(ItemCategory.UNDEFINED, ""),
    HELMET(ItemCategory.HELMET, "helmet"),
    ARMOR(ItemCategory.ARMOR, "armor"),
    SWORD(ItemCategory.WEAPON, "sword"),
    BIG_SWORD(ItemCategory.WEAPON, "sword2"),
    BOOTS(ItemCategory.BOOTS, "boots");

}


data class ItemComponent(
    var itemType: ItemType = ItemType.UNDEFINED,
    var slotIdx: Int = -1,
    var equipped: Boolean = false,
)
