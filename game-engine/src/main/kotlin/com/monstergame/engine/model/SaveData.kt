package com.monstergame.engine.model

import kotlinx.serialization.Serializable

@Serializable
data class SaveData(
    val version: Int = SAVE_VERSION,
    val playerName: String,
    val money: Int = 3000,
    val playtimeSeconds: Long = 0L,
    val position: Position,
    val party: List<CreatureInstance> = emptyList(),
    val pc: PCStorage = PCStorage(),
    val badges: Set<String> = emptySet(),
    val defeatedTrainers: Set<String> = emptySet(),
    val bag: Bag = Bag(),
    val flags: Map<String, Boolean> = emptyMap(),
    val activeRegion: String = "KANTO",
    val rivalName: String = "Rival",
    val seenCreatures: Set<Int> = emptySet(),
    val caughtCreatures: Set<Int> = emptySet()
) {
    companion object {
        const val SAVE_VERSION = 1
    }

    fun hasFlag(flag: String): Boolean = flags[flag] == true
    fun withFlag(flag: String, value: Boolean = true): SaveData =
        copy(flags = flags + (flag to value))
    fun withBadge(badgeId: String): SaveData = copy(badges = badges + badgeId)
    fun hasDefeated(trainerId: String): Boolean = trainerId in defeatedTrainers
    fun withDefeated(trainerId: String): SaveData =
        copy(defeatedTrainers = defeatedTrainers + trainerId)
}

@Serializable
data class PCStorage(
    val boxes: List<PCBox> = List(30) { PCBox(it, emptyList()) }
) {
    fun store(creature: CreatureInstance): PCStorage {
        for ((i, box) in boxes.withIndex()) {
            if (box.creatures.size < 30) {
                val newBoxes = boxes.toMutableList()
                newBoxes[i] = box.copy(creatures = box.creatures + creature)
                return copy(boxes = newBoxes)
            }
        }
        return this // full (shouldn't happen in normal play)
    }

    fun withdraw(uid: Long): Pair<PCStorage, CreatureInstance?> {
        for ((boxIdx, box) in boxes.withIndex()) {
            val idx = box.creatures.indexOfFirst { it.uid == uid }
            if (idx >= 0) {
                val creature = box.creatures[idx]
                val newBoxes = boxes.toMutableList()
                newBoxes[boxIdx] = box.copy(creatures = box.creatures.toMutableList().also { it.removeAt(idx) })
                return copy(boxes = newBoxes) to creature
            }
        }
        return this to null
    }

    fun allCreatures(): List<CreatureInstance> = boxes.flatMap { it.creatures }
}

@Serializable
data class PCBox(
    val index: Int,
    val creatures: List<CreatureInstance>,
    val name: String = "Box ${index + 1}"
)
