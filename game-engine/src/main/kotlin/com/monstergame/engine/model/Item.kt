package com.monstergame.engine.model

import kotlinx.serialization.Serializable

@Serializable
enum class ItemCategory {
    MEDICINE, BALL, BATTLE, KEY, EVOLUTION, HELD, TM
}

@Serializable
enum class BallType {
    BASIC, GOOD, ULTRA, MASTER, NET, DUSK, QUICK, TIMER, REPEAT
}

@Serializable
data class Item(
    val id: Int,
    val name: String,
    val category: ItemCategory,
    val price: Int,
    val sellPrice: Int = price / 2,
    val description: String = "",
    val healAmount: Int = 0,       // HP restored (medicine items)
    val curesStatus: Boolean = false,
    val curesAll: Boolean = false,  // Full Restore / Max Potion
    val revives: Boolean = false,   // Revive / Max Revive
    val reviveFull: Boolean = false,
    val ballType: BallType? = null,
    val evolvesSpeciesId: Int? = null,  // for evolution stones
    val tmMoveId: Int? = null
)

@Serializable
data class Bag(
    val items: MutableMap<Int, Int> = mutableMapOf()  // itemId -> quantity
) {
    fun add(itemId: Int, qty: Int = 1) { items[itemId] = (items[itemId] ?: 0) + qty }
    fun remove(itemId: Int, qty: Int = 1): Boolean {
        val cur = items[itemId] ?: 0
        if (cur < qty) return false
        val newQty = cur - qty
        if (newQty == 0) items.remove(itemId) else items[itemId] = newQty
        return true
    }
    fun count(itemId: Int): Int = items[itemId] ?: 0
    fun has(itemId: Int): Boolean = (items[itemId] ?: 0) > 0
}
