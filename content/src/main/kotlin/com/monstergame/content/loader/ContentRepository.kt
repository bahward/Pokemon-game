package com.monstergame.content.loader

import com.monstergame.content.model.ShopDto
import com.monstergame.engine.model.*
import com.monstergame.engine.overworld.GameEvent

/**
 * Aggregated in-memory content repository.
 * Populated by ContentLoader from JSON assets.
 */
class ContentRepository {

    private val _creatures = mutableMapOf<Int, CreatureSpecies>()
    private val _moves = mutableMapOf<Int, Move>()
    private val _items = mutableMapOf<Int, Item>()
    private val _areas = mutableMapOf<String, AreaData>()
    private val _trainers = mutableMapOf<String, TrainerData>()
    private val _regions = mutableMapOf<String, RegionData>()
    private val _events = mutableMapOf<String, GameEvent>()
    private val _npcs = mutableMapOf<String, NpcData>()
    private val _shops = mutableMapOf<String, ShopDto>()

    val creatures: Map<Int, CreatureSpecies> get() = _creatures
    val moves: Map<Int, Move> get() = _moves
    val items: Map<Int, Item> get() = _items
    val areas: Map<String, AreaData> get() = _areas
    val trainers: Map<String, TrainerData> get() = _trainers
    val regions: Map<String, RegionData> get() = _regions
    val events: Map<String, GameEvent> get() = _events
    val npcs: Map<String, NpcData> get() = _npcs
    val shops: Map<String, ShopDto> get() = _shops

    fun addCreatures(map: Map<Int, CreatureSpecies>) = _creatures.putAll(map)
    fun addMoves(map: Map<Int, Move>) = _moves.putAll(map)
    fun addItems(map: Map<Int, Item>) = _items.putAll(map)
    fun addAreas(map: Map<String, AreaData>) = _areas.putAll(map)
    fun addTrainers(map: Map<String, TrainerData>) = _trainers.putAll(map)
    fun addRegion(r: RegionData) { _regions[r.id] = r }
    fun addEvents(map: Map<String, GameEvent>) = _events.putAll(map)
    fun addNpcs(map: Map<String, NpcData>) = _npcs.putAll(map)
    fun addShops(list: List<ShopDto>) = list.forEach { _shops[it.id] = it }

    fun getCreature(id: Int): CreatureSpecies? = _creatures[id]
    fun getMove(id: Int): Move? = _moves[id]
    fun getItem(id: Int): Item? = _items[id]
    fun getArea(id: String): AreaData? = _areas[id]
    fun getTrainer(id: String): TrainerData? = _trainers[id]
    fun getRegion(id: String): RegionData? = _regions[id]
    fun getEvent(id: String): GameEvent? = _events[id]
    fun getNpc(id: String): NpcData? = _npcs[id]
    fun getShop(id: String): ShopDto? = _shops[id]

    fun starterPool(): List<CreatureSpecies> =
        _creatures.values.filter { it.isStarter }

    fun allLoaded(): Boolean =
        _creatures.isNotEmpty() && _moves.isNotEmpty() && _items.isNotEmpty() &&
        _areas.isNotEmpty() && _trainers.isNotEmpty() && _regions.isNotEmpty()

    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        _creatures.values.forEach { sp ->
            sp.learnset.forEach { le ->
                if (le.moveId !in _moves) errors.add("Creature ${sp.id}: unknown move ${le.moveId}")
            }
            sp.evolutions.forEach { ev ->
                if (ev.toId !in _creatures) errors.add("Creature ${sp.id}: evolves to unknown ${ev.toId}")
            }
        }
        _trainers.values.forEach { t ->
            t.team.forEach { te ->
                if (te.creatureId !in _creatures) errors.add("Trainer ${t.id}: unknown creature ${te.creatureId}")
                te.moves.forEach { mid ->
                    if (mid !in _moves) errors.add("Trainer ${t.id}: unknown move $mid")
                }
            }
        }
        _areas.values.forEach { area ->
            (area.encounters.grass + area.encounters.water + area.encounters.cave).forEach { e ->
                if (e.creatureId !in _creatures) errors.add("Area ${area.id}: unknown encounter creature ${e.creatureId}")
            }
            area.trainers.forEach { tid ->
                if (tid !in _trainers) errors.add("Area ${area.id}: unknown trainer $tid")
            }
        }
        return errors
    }
}
