package com.monstergame.engine.overworld

import com.monstergame.engine.model.*

/**
 * Pure Kotlin overworld logic engine.
 * The Android layer calls into this; no Android deps here.
 */
class OverworldEngine(
    private val areaRepo: Map<String, AreaData>,
    private val tileMapRepo: Map<String, TileMap>,
    private val speciesRepo: Map<Int, CreatureSpecies>,
    private val npcRepo: Map<String, NpcData>,
    private val eventRepo: Map<String, GameEvent>
) {

    sealed class OverworldResult {
        data class Moved(val newPos: Position, val newDirection: Direction) : OverworldResult()
        data class Blocked(val reason: String = "") : OverworldResult()
        data class WildEncounter(val creature: CreatureInstance, val terrain: EncounterTerrain) : OverworldResult()
        data class WarpTriggered(val warp: WarpPoint) : OverworldResult()
        data class EventTriggered(val event: GameEvent) : OverworldResult()
        data class NpcInteract(val npc: NpcData) : OverworldResult()
    }

    fun move(
        pos: Position,
        direction: Direction,
        save: SaveData
    ): List<OverworldResult> {
        val results = mutableListOf<OverworldResult>()
        val tileMap = tileMapRepo[pos.areaId] ?: return listOf(OverworldResult.Blocked("No map"))

        val (nx, ny) = when (direction) {
            Direction.UP -> pos.x to pos.y - 1
            Direction.DOWN -> pos.x to pos.y + 1
            Direction.LEFT -> pos.x - 1 to pos.y
            Direction.RIGHT -> pos.x + 1 to pos.y
        }

        // Bounds check
        if (nx < 0 || ny < 0 || ny >= tileMap.tiles.size || nx >= (tileMap.tiles.getOrNull(ny)?.size ?: 0)) {
            return listOf(OverworldResult.Blocked("Edge of map"))
        }

        val targetTileType = tileMap.tiles[ny][nx]
        val targetTile = Tile(targetTileType)

        // Ledge check (only jump down)
        if (targetTileType == TileType.LEDGE_DOWN && direction != Direction.DOWN) {
            return listOf(OverworldResult.Blocked())
        }

        if (!canWalkOn(targetTileType, save)) {
            return listOf(OverworldResult.Blocked())
        }

        val newPos = Position(pos.areaId, nx, ny)
        results.add(OverworldResult.Moved(newPos, direction))

        // Check for warp
        val area = areaRepo[pos.areaId]
        if (targetTileType == TileType.WARP || targetTileType == TileType.DOOR) {
            val warpId = targetTile.triggerId
            // Warp points are encoded as "toArea:toX:toY"
            warpId?.split(":")?.let { parts ->
                if (parts.size >= 3) {
                    val warp = WarpPoint(pos.areaId, nx, ny, parts[0], parts[1].toIntOrNull() ?: 0, parts[2].toIntOrNull() ?: 0)
                    results.add(OverworldResult.WarpTriggered(warp))
                    return results
                }
            }
        }

        // Wild encounter check
        val terrain = when (targetTileType) {
            TileType.TALL_GRASS, TileType.GRASS -> EncounterTerrain.GRASS
            TileType.WATER -> EncounterTerrain.WATER
            else -> if (area?.type == "DUNGEON" || area?.type == "CAVE") EncounterTerrain.CAVE else EncounterTerrain.NONE
        }

        if (terrain != EncounterTerrain.NONE && area != null) {
            if (EncounterGenerator.shouldEncounter(terrain)) {
                val encounter = EncounterGenerator.generateEncounter(area, terrain, speciesRepo)
                if (encounter != null) {
                    results.add(OverworldResult.WildEncounter(encounter, terrain))
                    return results
                }
            }
        }

        // Tile-step events
        if (area != null) {
            for (eventId in area.events) {
                val event = eventRepo[eventId] ?: continue
                if (EventSystem.shouldFire(event, save) &&
                    EventSystem.matchesTrigger(event, EventTriggerType.TILE_STEP,
                        EventContext(areaId = newPos.areaId, x = nx, y = ny))) {
                    results.add(OverworldResult.EventTriggered(event))
                }
            }
        }

        return results
    }

    fun interact(pos: Position, direction: Direction, save: SaveData): OverworldResult? {
        val (fx, fy) = when (direction) {
            Direction.UP -> pos.x to pos.y - 1
            Direction.DOWN -> pos.x to pos.y + 1
            Direction.LEFT -> pos.x - 1 to pos.y
            Direction.RIGHT -> pos.x + 1 to pos.y
        }
        // Find NPC at face position
        val npc = npcRepo.values.firstOrNull {
            it.areaId == pos.areaId && it.x == fx && it.y == fy
        }
        if (npc != null) return OverworldResult.NpcInteract(npc)

        // Check area enter events
        val area = areaRepo[pos.areaId] ?: return null
        for (eventId in area.events) {
            val event = eventRepo[eventId] ?: continue
            if (EventSystem.shouldFire(event, save)) {
                return OverworldResult.EventTriggered(event)
            }
        }
        return null
    }

    fun onAreaEnter(areaId: String, save: SaveData): List<GameEvent> {
        val area = areaRepo[areaId] ?: return emptyList()
        return area.events.mapNotNull { eventRepo[it] }
            .filter { event ->
                EventSystem.shouldFire(event, save) &&
                EventSystem.matchesTrigger(event, EventTriggerType.AREA_ENTER,
                    EventContext(areaId = areaId))
            }
    }

    private fun canWalkOn(tile: TileType, save: SaveData): Boolean = when (tile) {
        TileType.WALL -> false
        TileType.WATER -> save.hasFlag("has_surf") // need Surf ability
        TileType.LEDGE_DOWN -> true // can jump down
        else -> true
    }
}
