package com.monstergame.content.model

import com.monstergame.engine.model.*
import kotlinx.serialization.Serializable

// DTO for JSON deserialisation — mirrors engine models but tolerant of missing fields

@Serializable
data class CreatureDto(
    val id: Int,
    val name: String,
    val types: List<String>,
    val baseStats: BaseStatsDto,
    val catchRate: Int = 45,
    val baseExp: Int = 64,
    val learnset: List<LearnEntryDto> = emptyList(),
    val evolutions: List<EvolutionDto> = emptyList(),
    val regionId: String = "KANTO",
    val isStarter: Boolean = false,
    val isLegendary: Boolean = false,
    val description: String = "",
    val habitat: List<String> = emptyList()
)

@Serializable
data class BaseStatsDto(
    val hp: Int, val attack: Int, val defense: Int,
    val specialAttack: Int, val specialDefense: Int, val speed: Int
)

@Serializable
data class LearnEntryDto(val level: Int, val moveId: Int)

@Serializable
data class EvolutionDto(
    val toId: Int,
    val method: String = "LEVEL",
    val param: Int = 0,
    val paramStr: String = ""
)

@Serializable
data class MoveDto(
    val id: Int,
    val name: String,
    val type: String,
    val category: String,
    val power: Int = 0,
    val accuracy: Int = 100,
    val pp: Int = 10,
    val priority: Int = 0,
    val effect: String = "NONE",
    val effectChance: Int = 0,
    val target: String = "SINGLE_OPPONENT",
    val description: String = ""
)

@Serializable
data class ItemDto(
    val id: Int,
    val name: String,
    val category: String,
    val price: Int = 0,
    val description: String = "",
    val healAmount: Int = 0,
    val curesStatus: Boolean = false,
    val curesAll: Boolean = false,
    val revives: Boolean = false,
    val reviveFull: Boolean = false,
    val ballType: String? = null,
    val evolvesSpeciesId: Int? = null,
    val tmMoveId: Int? = null
)

@Serializable
data class AreaDto(
    val id: String,
    val name: String,
    val regionId: String,
    val type: String = "ROUTE",
    val connections: List<String> = emptyList(),
    val encounters: AreaEncountersDto = AreaEncountersDto(),
    val trainers: List<String> = emptyList(),
    val events: List<String> = emptyList(),
    val shopId: String? = null,
    val healingCenter: Boolean = false,
    val pc: Boolean = false
)

@Serializable
data class AreaEncountersDto(
    val grass: List<EncounterEntryDto> = emptyList(),
    val water: List<EncounterEntryDto> = emptyList(),
    val cave: List<EncounterEntryDto> = emptyList()
)

@Serializable
data class EncounterEntryDto(
    val creatureId: Int,
    val minLevel: Int,
    val maxLevel: Int,
    val weight: Int = 10
)

@Serializable
data class TrainerDto(
    val id: String,
    val name: String,
    val trainerClass: String = "TRAINER",
    val areaId: String,
    val reward: Int = 500,
    val badgeId: String? = null,
    val team: List<TeamEntryDto> = emptyList(),
    val aiProfile: String = "BASIC",
    val preBattleText: String = "Let's battle!",
    val postBattleText: String = "Well fought.",
    val repeatable: Boolean = false,
    val items: List<Int> = emptyList()
)

@Serializable
data class TeamEntryDto(
    val creatureId: Int,
    val level: Int,
    val moves: List<Int> = emptyList()
)

@Serializable
data class RegionDto(
    val id: String,
    val name: String,
    val areas: List<String> = emptyList(),
    val startAreaId: String,
    val gymIds: List<String> = emptyList(),
    val eliteFourId: String? = null
)

@Serializable
data class GymDto(
    val id: String,
    val areaId: String,
    val badgeId: String,
    val badgeNumber: Int,
    val regionId: String,
    val leaderId: String,
    val requiredBadges: List<String> = emptyList()
)

@Serializable
data class EliteFourDto(
    val id: String,
    val regionId: String,
    val areaId: String,
    val members: List<String> = emptyList(),
    val championId: String
)

@Serializable
data class EventDto(
    val id: String,
    val trigger: TriggerDto,
    val condition: ConditionDto? = null,
    val notCondition: ConditionDto? = null,
    val actions: List<ActionDto> = emptyList(),
    val repeatable: Boolean = false
)

@Serializable
data class TriggerDto(
    val type: String,
    val areaId: String? = null,
    val x: Int? = null,
    val y: Int? = null,
    val npcId: String? = null,
    val flag: String? = null,
    val flagValue: Boolean? = null
)

@Serializable
data class ConditionDto(val flag: String, val value: Boolean)

@Serializable
data class ActionDto(
    val type: String,
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
data class ShopDto(
    val id: String,
    val name: String,
    val areaId: String,
    val items: List<ShopItemDto> = emptyList()
)

@Serializable
data class ShopItemDto(val itemId: Int, val price: Int? = null)

@Serializable
data class NpcDto(
    val id: String,
    val name: String,
    val areaId: String,
    val x: Int = 0,
    val y: Int = 0,
    val dialogues: List<String> = emptyList(),
    val trainerId: String? = null
)
