package com.monstergame.engine.overworld

import com.monstergame.engine.model.*
import kotlin.random.Random

object EncounterGenerator {

    private const val GRASS_ENCOUNTER_RATE = 10  // % per step in tall grass
    private const val CAVE_ENCOUNTER_RATE = 15
    private const val WATER_ENCOUNTER_RATE = 8

    fun shouldEncounter(terrain: EncounterTerrain): Boolean {
        val rate = when (terrain) {
            EncounterTerrain.GRASS -> GRASS_ENCOUNTER_RATE
            EncounterTerrain.CAVE -> CAVE_ENCOUNTER_RATE
            EncounterTerrain.WATER -> WATER_ENCOUNTER_RATE
            EncounterTerrain.NONE -> 0
        }
        return Random.nextInt(100) < rate
    }

    /**
     * Picks a random encounter from the area's encounter table for the given terrain.
     * Returns null if no encounters available.
     */
    fun generateEncounter(
        area: AreaData,
        terrain: EncounterTerrain,
        speciesRepo: Map<Int, CreatureSpecies>
    ): CreatureInstance? {
        val table = when (terrain) {
            EncounterTerrain.GRASS -> area.encounters.grass
            EncounterTerrain.WATER -> area.encounters.water
            EncounterTerrain.CAVE -> area.encounters.cave
            EncounterTerrain.NONE -> return null
        }
        if (table.isEmpty()) return null

        val entry = weightedRandom(table) ?: return null
        val species = speciesRepo[entry.creatureId] ?: return null

        val level = Random.nextInt(entry.minLevel, entry.maxLevel + 1)
        return CreatureFactory.createFromSpecies(species, level)
    }

    private fun weightedRandom(entries: List<EncounterEntry>): EncounterEntry? {
        val total = entries.sumOf { it.weight }
        if (total == 0) return null
        var roll = Random.nextInt(total)
        for (entry in entries) {
            roll -= entry.weight
            if (roll < 0) return entry
        }
        return entries.last()
    }

    /**
     * Generates a full trainer team from their data definitions.
     */
    fun buildTrainerTeam(
        trainer: TrainerData,
        speciesRepo: Map<Int, CreatureSpecies>,
        moveRepo: Map<Int, Move>
    ): List<CreatureInstance> {
        return trainer.team.map { entry ->
            val species = speciesRepo[entry.creatureId]
                ?: return@map CreatureFactory.createFromSpecies(
                    speciesRepo.values.first(), entry.level
                )
            val instance = CreatureFactory.createFromSpecies(species, entry.level)

            // Fill moves: use specified or fall back to learnset
            val moveSlots = if (entry.moves.isNotEmpty()) {
                entry.moves.take(4).mapNotNull { moveId ->
                    val m = moveRepo[moveId] ?: return@mapNotNull null
                    MoveSlot(m.id, m.pp, m.pp)
                }
            } else {
                species.learnset
                    .filter { it.level <= entry.level }
                    .takeLast(4)
                    .mapNotNull { le ->
                        val m = moveRepo[le.moveId] ?: return@mapNotNull null
                        MoveSlot(m.id, m.pp, m.pp)
                    }
            }

            instance.copy(moves = moveSlots)
        }
    }
}
