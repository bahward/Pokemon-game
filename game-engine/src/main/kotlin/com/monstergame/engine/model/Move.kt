package com.monstergame.engine.model

import kotlinx.serialization.Serializable

@Serializable
enum class MoveTarget {
    SINGLE_OPPONENT, ALL_OPPONENTS, SELF, ALLY, ALL_OTHERS, ALL
}

@Serializable
enum class MoveEffect {
    // Status inflicting
    BURN, POISON, BAD_POISON, PARALYZE, SLEEP, FREEZE, CONFUSE,
    // Stat changes
    ATK_UP1, ATK_DOWN1, DEF_UP1, DEF_DOWN1,
    SP_ATK_UP1, SP_ATK_DOWN1, SP_DEF_UP1, SP_DEF_DOWN1,
    SPD_UP1, SPD_DOWN1, ACC_DOWN1, EVA_UP1,
    ATK_UP2, DEF_UP2, SP_ATK_UP2, SP_DEF_UP2, SPD_UP2,
    // Recovery
    HEAL_HALF, DRAIN_HALF,
    // Special mechanics
    RECHARGE, // must recharge next turn
    TWO_HIT, MULTI_HIT,
    FLINCH,
    TRAP, // bind opponent
    CRIT_BOOST, // higher crit rate
    NEVER_MISS,
    WEATHER_SUN, WEATHER_RAIN, WEATHER_SAND, WEATHER_HAIL,
    PRIORITY_PLUS1,
    OHKO,
    FIXED_DAMAGE_40,
    RECOIL_25, RECOIL_33,
    NONE
}

@Serializable
data class Move(
    val id: Int,
    val name: String,
    val type: ElementType,
    val category: MoveCategory,
    val power: Int,           // 0 for status moves
    val accuracy: Int,        // 0–100; 0 = never misses
    val pp: Int,
    val maxPp: Int = pp * 8 / 5,
    val priority: Int = 0,
    val effect: MoveEffect = MoveEffect.NONE,
    val effectChance: Int = 0, // 0–100 % chance to trigger secondary effect
    val target: MoveTarget = MoveTarget.SINGLE_OPPONENT,
    val description: String = ""
)
