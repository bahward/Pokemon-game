package com.monstergame.engine.overworld

import kotlinx.serialization.Serializable

// ---------------------------------------------------------------------------
// Encounter / area models
// ---------------------------------------------------------------------------

@Serializable
data class EncounterEntry(
    val creatureId: Int,
    val minLevel: Int,
    val maxLevel: Int,
    val weight: Int = 10
)

@Serializable
data class AreaEncounters(
    val grass: List<EncounterEntry> = emptyList(),
    val water: List<EncounterEntry> = emptyList(),
    val cave: List<EncounterEntry> = emptyList()
)

@Serializable
data class AreaData(
    val id: String,
    val name: String,
    val regionId: String,
    val type: String = "ROUTE",
    val connections: List<String> = emptyList(),
    val encounters: AreaEncounters = AreaEncounters(),
    val trainers: List<String> = emptyList(),
    val events: List<String> = emptyList(),
    val shopId: String? = null,
    val healingCenter: Boolean = false,
    val pc: Boolean = false
)

@Serializable
data class RegionData(
    val id: String,
    val name: String,
    val areas: List<String> = emptyList(),
    val startAreaId: String,
    val gymIds: List<String> = emptyList(),
    val eliteFourId: String? = null
)

@Serializable
data class NpcData(
    val id: String,
    val name: String,
    val areaId: String,
    val x: Int = 0,
    val y: Int = 0,
    val dialogues: List<String> = emptyList(),
    val trainerId: String? = null
)

// ---------------------------------------------------------------------------
// Tile / map models (used by OverworldEngine)
// ---------------------------------------------------------------------------

enum class EncounterTerrain { GRASS, WATER, CAVE, NONE }

enum class Direction { UP, DOWN, LEFT, RIGHT }

enum class TileType {
    FLOOR, GRASS, TALL_GRASS, WATER, WALL, DOOR, WARP,
    LEDGE_DOWN, LEDGE_UP, LEDGE_LEFT, LEDGE_RIGHT,
    SAND, SNOW, CAVE_FLOOR
}

data class Tile(
    val type: TileType,
    val triggerId: String? = null
)

data class TileMap(
    val areaId: String,
    val width: Int,
    val height: Int,
    /** Row-major: tiles[y][x] */
    val tiles: List<List<TileType>>,
    val warps: List<WarpPoint> = emptyList()
)

data class Position(
    val areaId: String,
    val x: Int,
    val y: Int
)

data class WarpPoint(
    val fromAreaId: String,
    val fromX: Int,
    val fromY: Int,
    val toAreaId: String,
    val toX: Int,
    val toY: Int
)
