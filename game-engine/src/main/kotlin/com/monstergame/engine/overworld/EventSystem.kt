package com.monstergame.engine.overworld

import com.monstergame.engine.model.SaveData
import kotlinx.serialization.Serializable

@Serializable
enum class EventTriggerType {
    AREA_ENTER, TILE_STEP, NPC_INTERACT, ITEM_PICKUP, BATTLE_WIN, FLAG_SET
}

@Serializable
data class EventTrigger(
    val type: EventTriggerType,
    val areaId: String? = null,
    val x: Int? = null,
    val y: Int? = null,
    val npcId: String? = null,
    val flag: String? = null,
    val flagValue: Boolean? = null
)

@Serializable
enum class EventActionType {
    DIALOGUE, BATTLE, SET_FLAG, GIVE_ITEM, GIVE_CREATURE,
    WARP, UNLOCK_AREA, SHOW_TEXT, HEAL_PARTY
}

@Serializable
data class EventAction(
    val type: EventActionType,
    val npcId: String? = null,
    val trainerId: String? = null,
    val flag: String? = null,
    val flagValue: Boolean? = null,
    val itemId: Int? = null,
    val qty: Int = 1,
    val creatureId: Int? = null,
    val creatureLevel: Int? = null,
    val warpAreaId: String? = null,
    val warpX: Int? = null,
    val warpY: Int? = null,
    val text: String? = null,
    val areaId: String? = null
)

@Serializable
data class EventCondition(
    val flag: String,
    val value: Boolean
)

@Serializable
data class GameEvent(
    val id: String,
    val trigger: EventTrigger,
    val condition: EventCondition? = null,
    val notCondition: EventCondition? = null, // must NOT have this flag
    val actions: List<EventAction>,
    val repeatable: Boolean = false
)

/** Checks if an event should fire given current save state. */
object EventSystem {

    fun shouldFire(event: GameEvent, save: SaveData): Boolean {
        if (!event.repeatable && save.hasFlag("event_done_${event.id}")) return false
        event.condition?.let { c -> if (save.hasFlag(c.flag) != c.value) return false }
        event.notCondition?.let { c -> if (save.hasFlag(c.flag) == c.value) return false }
        return true
    }

    fun matchesTrigger(event: GameEvent, triggerType: EventTriggerType, context: EventContext): Boolean {
        if (event.trigger.type != triggerType) return false
        return when (triggerType) {
            EventTriggerType.AREA_ENTER -> event.trigger.areaId == context.areaId
            EventTriggerType.TILE_STEP -> event.trigger.areaId == context.areaId &&
                    event.trigger.x == context.x && event.trigger.y == context.y
            EventTriggerType.NPC_INTERACT -> event.trigger.npcId == context.npcId
            EventTriggerType.FLAG_SET -> event.trigger.flag == context.flag &&
                    event.trigger.flagValue == context.flagValue
            else -> true
        }
    }

    /** Mark event as completed (for non-repeatable events). */
    fun markDone(event: GameEvent, save: SaveData): SaveData =
        if (!event.repeatable) save.withFlag("event_done_${event.id}") else save
}

data class EventContext(
    val areaId: String? = null,
    val x: Int? = null,
    val y: Int? = null,
    val npcId: String? = null,
    val flag: String? = null,
    val flagValue: Boolean? = null
)
