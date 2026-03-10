package com.monstergame.engine.model

import kotlinx.serialization.Serializable

@Serializable
enum class EncounterTerrain { GRASS, WATER, CAVE, NONE }

@Serializable
data class Position(val areaId: String, val x: Int, val y: Int)

@Serializable
data class EncounterEntry(
    val creatureId: Int,
    val minLevel: Int,
    val maxLevel: Int,
    val weight: Int          // relative weight for random selection
)

@Serializable
data class AreaEncounters(
    val grass: List<EncounterEntry> = emptyList(),
    val water: List<EncounterEntry> = emptyList(),
    val cave: List<EncounterEntry> = emptyList()
)

@Serializable
enum class TileType {
    GROUND, WALL, WATER, GRASS, TALL_GRASS, DOOR, WARP,
    LEDGE_DOWN, LEDGE_LEFT, LEDGE_RIGHT, NPC, ITEM_BALL
}

@Serializable
data class Tile(
    val type: TileType,
    val passable: Boolean = type != TileType.WALL && type != TileType.WATER,
    val triggerId: String? = null  // event/warp/npc ID
)

@Serializable
data class TileMap(
    val areaId: String,
    val width: Int,
    val height: Int,
    val tiles: List<List<TileType>>,  // [row][col]
    val connections: Map<String, String> = emptyMap() // direction -> areaId
)

@Serializable
data class WarpPoint(
    val fromArea: String,
    val fromX: Int,
    val fromY: Int,
    val toArea: String,
    val toX: Int,
    val toY: Int
)

@Serializable
data class AreaData(
    val id: String,
    val name: String,
    val regionId: String,
    val type: String,         // "ROUTE", "TOWN", "DUNGEON", "GYM", etc.
    val connections: List<String>,
    val encounters: AreaEncounters,
    val trainers: List<String>,
    val events: List<String>,
    val shopId: String? = null,
    val healingCenter: Boolean = false,
    val pc: Boolean = false
)

@Serializable
data class RegionData(
    val id: String,
    val name: String,         // user-facing original name
    val areas: List<String>,
    val startAreaId: String,
    val gymIds: List<String>,
    val eliteFourId: String? = null
)

@Serializable
data class NpcData(
    val id: String,
    val name: String,
    val areaId: String,
    val x: Int,
    val y: Int,
    val dialogues: List<String>,
    val trainerId: String? = null
)

@Serializable
data class PlayerOverworld(
    val position: Position,
    val facingDirection: Direction = Direction.DOWN,
    val isMoving: Boolean = false
)

@Serializable
enum class Direction { UP, DOWN, LEFT, RIGHT }
