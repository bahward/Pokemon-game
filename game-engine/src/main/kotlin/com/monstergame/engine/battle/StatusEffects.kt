package com.monstergame.engine.battle

import com.monstergame.engine.model.*
import kotlin.math.max
import kotlin.random.Random

object StatusEffects {

    /** Apply end-of-turn status damage/effects. Returns updated creature + messages. */
    fun applyEndOfTurn(creature: CreatureInstance): Pair<CreatureInstance, List<String>> {
        val messages = mutableListOf<String>()
        var c = creature

        when (c.statusCondition) {
            StatusCondition.BURN -> {
                val dmg = max(1, c.maxHp / 8)
                c = CreatureFactory.withHp(c, c.currentHp - dmg)
                messages.add("${c.nickname} is hurt by its burn!")
                if (c.currentHp <= 0) c = c.copy(statusCondition = StatusCondition.FAINT)
            }
            StatusCondition.POISON -> {
                val dmg = max(1, c.maxHp / 8)
                c = CreatureFactory.withHp(c, c.currentHp - dmg)
                messages.add("${c.nickname} is hurt by poison!")
                if (c.currentHp <= 0) c = c.copy(statusCondition = StatusCondition.FAINT)
            }
            StatusCondition.BAD_POISON -> {
                val counter = c.badPoisonCounter + 1
                val dmg = max(1, c.maxHp * counter / 16)
                c = CreatureFactory.withHp(c, c.currentHp - dmg)
                    .copy(badPoisonCounter = counter)
                messages.add("${c.nickname} is badly hurt by poison!")
                if (c.currentHp <= 0) c = c.copy(statusCondition = StatusCondition.FAINT)
            }
            StatusCondition.SLEEP -> {
                val turns = c.statusTurns - 1
                c = c.copy(statusTurns = turns)
                if (turns <= 0) {
                    c = c.copy(statusCondition = StatusCondition.NONE, statusTurns = 0)
                    messages.add("${c.nickname} woke up!")
                } else {
                    messages.add("${c.nickname} is fast asleep.")
                }
            }
            StatusCondition.FREEZE -> {
                if (Random.nextFloat() < 0.2f) {
                    c = c.copy(statusCondition = StatusCondition.NONE)
                    messages.add("${c.nickname} thawed out!")
                } else {
                    messages.add("${c.nickname} is frozen solid!")
                }
            }
            else -> {}
        }

        return c to messages
    }

    /** Returns true if the creature can act this turn (PAR/SLP/FRZ checks). */
    fun canAct(creature: CreatureInstance): Pair<Boolean, String?> {
        return when (creature.statusCondition) {
            StatusCondition.SLEEP -> {
                false to "${creature.nickname} is fast asleep."
            }
            StatusCondition.FREEZE -> {
                false to "${creature.nickname} is frozen solid!"
            }
            StatusCondition.PARALYZE -> {
                if (Random.nextFloat() < 0.25f) {
                    false to "${creature.nickname} is fully paralysed!"
                } else true to null
            }
            else -> true to null
        }
    }

    /** Try to inflict a new status condition. Returns updated creature + message, or null if failed. */
    fun tryInflict(
        target: CreatureInstance,
        targetSpecies: CreatureSpecies,
        status: StatusCondition
    ): Pair<CreatureInstance, String>? {
        // Already has a primary status
        if (target.statusCondition != StatusCondition.NONE) return null

        // Type immunities
        when (status) {
            StatusCondition.BURN ->
                if (ElementType.FLAME in targetSpecies.types) return null
            StatusCondition.FREEZE ->
                if (ElementType.FROST in targetSpecies.types) return null
            StatusCondition.POISON, StatusCondition.BAD_POISON ->
                if (ElementType.VENOM in targetSpecies.types ||
                    ElementType.IRON in targetSpecies.types) return null
            StatusCondition.PARALYZE ->
                if (ElementType.VOLT in targetSpecies.types) return null
            else -> {}
        }

        val sleepTurns = if (status == StatusCondition.SLEEP) Random.nextInt(1, 4) else 0
        val newCreature = target.copy(
            statusCondition = status,
            statusTurns = sleepTurns,
            badPoisonCounter = if (status == StatusCondition.BAD_POISON) 0 else target.badPoisonCounter
        )

        val msg = when (status) {
            StatusCondition.BURN -> "${target.nickname} was burned!"
            StatusCondition.POISON -> "${target.nickname} was poisoned!"
            StatusCondition.BAD_POISON -> "${target.nickname} was badly poisoned!"
            StatusCondition.PARALYZE -> "${target.nickname} is paralysed!"
            StatusCondition.SLEEP -> "${target.nickname} fell asleep!"
            StatusCondition.FREEZE -> "${target.nickname} was frozen solid!"
            else -> ""
        }

        return newCreature to msg
    }
}
