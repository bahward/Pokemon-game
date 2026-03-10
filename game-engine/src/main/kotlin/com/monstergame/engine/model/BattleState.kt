package com.monstergame.engine.model

import kotlinx.serialization.Serializable

@Serializable
enum class BattleType { WILD, TRAINER, GYM_LEADER, ELITE_FOUR, CHAMPION }

@Serializable
enum class WeatherEffect { CLEAR, SUN, RAIN, SANDSTORM, HAIL }

@Serializable
enum class BattleOutcome { ONGOING, PLAYER_WIN, PLAYER_LOSE, RAN_AWAY, CAUGHT }

@Serializable
data class BattleSide(
    val creatures: MutableList<CreatureInstance>,
    var activeIndex: Int = 0,
    var pendingSwitch: Int? = null
) {
    val active: CreatureInstance get() = creatures[activeIndex]
    fun hasAlive(): Boolean = creatures.any { it.isAlive }
    fun firstAliveIndex(): Int = creatures.indexOfFirst { it.isAlive }
}

@Serializable
data class BattleState(
    val battleType: BattleType,
    val player: BattleSide,
    val opponent: BattleSide,
    val trainerData: TrainerData? = null,  // null for wild
    val weather: WeatherEffect = WeatherEffect.CLEAR,
    val weatherTurns: Int = 0,
    val turn: Int = 0,
    val outcome: BattleOutcome = BattleOutcome.ONGOING,
    val log: List<String> = emptyList(),
    val canRun: Boolean = true,
    val runAttempts: Int = 0
)

/** Actions the player (or AI) can take in a turn */
sealed class BattleAction {
    data class UseMove(val moveIndex: Int) : BattleAction()
    data class Switch(val partyIndex: Int) : BattleAction()
    data class UseItem(val itemId: Int, val targetIndex: Int) : BattleAction()
    object Run : BattleAction()
    data class ThrowBall(val itemId: Int) : BattleAction()
}

/** Result of executing one turn */
data class TurnResult(
    val newState: BattleState,
    val messages: List<String>
)
