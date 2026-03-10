package com.monstergame.engine.model

import kotlinx.serialization.Serializable

@Serializable
enum class TrainerClass {
    GYM_LEADER, ELITE_FOUR, CHAMPION, RIVAL, TRAINER, BOSS
}

@Serializable
enum class AIProfile { RANDOM, BASIC, SMART, CHAMPION }

@Serializable
data class TrainerTeamEntry(
    val creatureId: Int,
    val level: Int,
    val moves: List<Int> = emptyList()  // move IDs; empty = auto from learnset
)

@Serializable
data class TrainerData(
    val id: String,
    val name: String,
    val trainerClass: TrainerClass,
    val areaId: String,
    val reward: Int,
    val team: List<TrainerTeamEntry>,
    val aiProfile: AIProfile = AIProfile.BASIC,
    val badgeId: String? = null,
    val preBattleText: String = "Let's battle!",
    val postBattleText: String = "Well fought.",
    val repeatable: Boolean = false,
    val items: List<Int> = emptyList()  // healing items the trainer can use
)

/** A gym definition */
@Serializable
data class GymData(
    val id: String,
    val areaId: String,
    val badgeId: String,
    val badgeNumber: Int,
    val regionId: String,
    val leaderId: String,
    val requiredBadges: List<String> = emptyList()  // badges required to enter
)

/** An Elite Four / Champion sequence */
@Serializable
data class EliteFourData(
    val id: String,
    val regionId: String,
    val areaId: String,
    val members: List<String>,  // trainer IDs in order
    val championId: String
)
