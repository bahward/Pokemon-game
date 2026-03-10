package com.monstergame.engine.battle

import com.monstergame.engine.model.*
import kotlin.random.Random

/**
 * AI decision-making for trainer opponents.
 * Returns a BattleAction for the opponent's current turn.
 */
class AIController(
    private val profile: AIProfile,
    private val speciesRepo: Map<Int, CreatureSpecies>,
    private val moveRepo: Map<Int, Move>
) {

    fun chooseAction(state: BattleState): BattleAction {
        val opp = state.opponent.active
        val player = state.player.active
        val playerSpecies = speciesRepo[player.speciesId] ?: return randomMove(opp)

        return when (profile) {
            AIProfile.RANDOM -> randomMove(opp)
            AIProfile.BASIC -> basicMove(opp, player, playerSpecies)
            AIProfile.SMART -> smartAction(state, opp, player, playerSpecies)
            AIProfile.CHAMPION -> championAction(state, opp, player, playerSpecies)
        }
    }

    private fun randomMove(opp: CreatureInstance): BattleAction {
        val usableMoves = opp.moves.indices.filter { opp.moves[it].currentPp > 0 }
        return if (usableMoves.isEmpty()) BattleAction.UseMove(0)
        else BattleAction.UseMove(usableMoves.random())
    }

    private fun basicMove(opp: CreatureInstance, player: CreatureInstance, playerSpecies: CreatureSpecies): BattleAction {
        // Pick highest power move with PP remaining
        val best = opp.moves.indices
            .filter { opp.moves[it].currentPp > 0 }
            .maxByOrNull { idx ->
                val move = moveRepo[opp.moves[idx].moveId] ?: return@maxByOrNull 0
                move.power
            }
        return BattleAction.UseMove(best ?: 0)
    }

    private fun smartAction(
        state: BattleState,
        opp: CreatureInstance,
        player: CreatureInstance,
        playerSpecies: CreatureSpecies
    ): BattleAction {
        val oppSpecies = speciesRepo[opp.speciesId]

        // Try to use healing item if HP < 25%
        if (opp.hpPercent < 0.25f && state.trainerData != null) {
            val potion = state.trainerData.items.firstOrNull()
            if (potion != null) return BattleAction.UseItem(potion, state.opponent.activeIndex)
        }

        // Consider switching if we have a bad matchup
        if (opp.hpPercent < 0.5f && state.opponent.creatures.any {
                it !== opp && it.isAlive && hasTypeAdvantage(it, playerSpecies)
            }) {
            val betterIdx = state.opponent.creatures.indexOfFirst {
                it !== opp && it.isAlive && hasTypeAdvantage(it, playerSpecies)
            }
            if (betterIdx >= 0) return BattleAction.Switch(betterIdx)
        }

        // Use most effective move
        val superEffectiveIdx = opp.moves.indices
            .filter { opp.moves[it].currentPp > 0 }
            .maxByOrNull { idx ->
                val move = moveRepo[opp.moves[idx].moveId] ?: return@maxByOrNull 0.0
                if (move.category == MoveCategory.STATUS) 0.0
                else {
                    val eff = ElementType.effectiveness(move.type, playerSpecies.types)
                    val stab = if (oppSpecies != null && move.type in oppSpecies.types) 1.5 else 1.0
                    move.power * eff * stab
                }
            }
        return BattleAction.UseMove(superEffectiveIdx ?: 0)
    }

    private fun championAction(
        state: BattleState,
        opp: CreatureInstance,
        player: CreatureInstance,
        playerSpecies: CreatureSpecies
    ): BattleAction {
        val oppSpecies = speciesRepo[opp.speciesId]

        // Use healing item at 50% HP
        if (opp.hpPercent < 0.5f && state.trainerData != null) {
            val healItem = state.trainerData.items.firstOrNull()
            if (healItem != null) return BattleAction.UseItem(healItem, state.opponent.activeIndex)
        }

        // Status spreading — try to inflict if player has no status and we have a status move
        if (player.statusCondition == StatusCondition.NONE) {
            val statusMoveIdx = opp.moves.indices.firstOrNull { idx ->
                val move = moveRepo[opp.moves[idx].moveId] ?: return@firstOrNull false
                move.category == MoveCategory.STATUS && move.effect in listOf(
                    MoveEffect.BURN, MoveEffect.POISON, MoveEffect.PARALYZE, MoveEffect.SLEEP
                ) && opp.moves[idx].currentPp > 0
            }
            if (statusMoveIdx != null && Random.nextFloat() < 0.4f) {
                return BattleAction.UseMove(statusMoveIdx)
            }
        }

        // Same as smart but with wider switching logic
        return smartAction(state, opp, player, playerSpecies)
    }

    private fun hasTypeAdvantage(creature: CreatureInstance, opponent: CreatureSpecies): Boolean {
        val species = speciesRepo[creature.speciesId] ?: return false
        return species.types.any { t ->
            creature.moves.any { slot ->
                val move = moveRepo[slot.moveId] ?: return@any false
                move.type == t && ElementType.effectiveness(t, opponent.types) > 1.0
            }
        }
    }
}
