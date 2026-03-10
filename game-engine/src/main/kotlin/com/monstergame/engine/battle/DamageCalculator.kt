package com.monstergame.engine.battle

import com.monstergame.engine.model.*
import kotlin.math.floor
import kotlin.math.max
import kotlin.random.Random

object DamageCalculator {

    /**
     * Calculates damage using the standard formula:
     *   Damage = floor((floor(2*Level/5+2) * Power * A/D) / 50 + 2) * Modifier
     */
    fun calculate(
        attacker: CreatureInstance,
        defender: CreatureInstance,
        move: Move,
        attackerSpecies: CreatureSpecies,
        defenderSpecies: CreatureSpecies,
        weather: WeatherEffect = WeatherEffect.CLEAR,
        isCrit: Boolean = false,
        randomFactor: Double = Random.nextDouble(0.85, 1.01)
    ): DamageResult {
        if (move.category == MoveCategory.STATUS || move.power == 0) {
            return DamageResult(0, 1.0, false, false)
        }

        val level = attacker.level
        val (rawAtk, rawDef) = when (move.category) {
            MoveCategory.PHYSICAL -> {
                val atkStage = attacker.statStages.attack
                val defStage = defender.statStages.defense
                val a = if (isCrit && atkStage < 0) attacker.calculatedStats.attack
                        else (attacker.calculatedStats.attack * attacker.statStages.stageMult(atkStage)).toInt()
                val d = if (isCrit && defStage > 0) defender.calculatedStats.defense
                        else (defender.calculatedStats.defense * attacker.statStages.stageMult(defStage)).toInt()
                a to d
            }
            MoveCategory.SPECIAL -> {
                val atkStage = attacker.statStages.specialAttack
                val defStage = defender.statStages.specialDefense
                val a = if (isCrit && atkStage < 0) attacker.calculatedStats.specialAttack
                        else (attacker.calculatedStats.specialAttack * attacker.statStages.stageMult(atkStage)).toInt()
                val d = if (isCrit && defStage > 0) defender.calculatedStats.specialDefense
                        else (defender.calculatedStats.specialDefense * attacker.statStages.stageMult(defStage)).toInt()
                a to d
            }
            else -> 0 to 1
        }

        val atk = max(1, rawAtk)
        val def = max(1, rawDef)

        // Base damage
        val base = floor(
            floor((2.0 * level / 5.0 + 2.0) * move.power * atk / def.toDouble()) / 50.0 + 2.0
        ).toInt()

        // STAB
        val stab = if (move.type in attackerSpecies.types) 1.5 else 1.0

        // Type effectiveness
        val typeEff = ElementType.effectiveness(move.type, defenderSpecies.types)

        // Critical
        val critMult = if (isCrit) 1.5 else 1.0

        // Burn
        val burnMod = if (attacker.statusCondition == StatusCondition.BURN &&
                         move.category == MoveCategory.PHYSICAL) 0.5 else 1.0

        // Weather
        val weatherMod = when (weather) {
            WeatherEffect.SUN -> when (move.type) {
                ElementType.FLAME -> 1.5
                ElementType.AQUA -> 0.5
                else -> 1.0
            }
            WeatherEffect.RAIN -> when (move.type) {
                ElementType.AQUA -> 1.5
                ElementType.FLAME -> 0.5
                else -> 1.0
            }
            else -> 1.0
        }

        val totalMod = stab * typeEff * critMult * randomFactor * burnMod * weatherMod
        val finalDamage = max(1, floor(base * totalMod).toInt())

        return DamageResult(
            damage = finalDamage,
            typeEffectiveness = typeEff,
            isCritical = isCrit,
            isSuperEffective = typeEff > 1.0,
            isNotVeryEffective = typeEff < 1.0 && typeEff > 0.0,
            isImmune = typeEff == 0.0
        )
    }

    fun rollCrit(critStage: Int = 0): Boolean {
        val threshold = when {
            critStage >= 3 -> 1.0
            critStage == 2 -> 0.5
            critStage == 1 -> 0.125
            else -> 0.0625  // 1/16
        }
        return Random.nextDouble() < threshold
    }

    fun rollAccuracy(move: Move, attackerAccStage: Int, defenderEvaStage: Int): Boolean {
        if (move.accuracy == 0) return true  // never misses
        val accMult = attacker_accuracy_mult(attackerAccStage) / defender_evasion_mult(defenderEvaStage)
        return Random.nextInt(100) < (move.accuracy * accMult).toInt()
    }

    private fun attacker_accuracy_mult(stage: Int): Double = when {
        stage >= 6 -> 3.0; stage == 5 -> 2.666; stage == 4 -> 2.333; stage == 3 -> 2.0
        stage == 2 -> 1.666; stage == 1 -> 1.333; stage == 0 -> 1.0
        stage == -1 -> 0.75; stage == -2 -> 0.6; stage == -3 -> 0.5
        stage == -4 -> 0.43; stage == -5 -> 0.36; else -> 0.33
    }

    private fun defender_evasion_mult(stage: Int): Double = when {
        stage >= 6 -> 3.0; stage == 5 -> 2.666; stage == 4 -> 2.333; stage == 3 -> 2.0
        stage == 2 -> 1.666; stage == 1 -> 1.333; stage == 0 -> 1.0
        stage == -1 -> 0.75; stage == -2 -> 0.6; stage == -3 -> 0.5
        stage == -4 -> 0.43; stage == -5 -> 0.36; else -> 0.33
    }
}

data class DamageResult(
    val damage: Int,
    val typeEffectiveness: Double,
    val isCritical: Boolean,
    val isSuperEffective: Boolean,
    val isNotVeryEffective: Boolean = false,
    val isImmune: Boolean = false
) {
    val effectivenessText: String get() = when {
        isImmune -> "It had no effect!"
        typeEffectiveness >= 4.0 -> "It's devastatingly effective!!"
        typeEffectiveness >= 2.0 -> "It's super effective!"
        typeEffectiveness <= 0.25 -> "It's barely effective..."
        typeEffectiveness < 1.0 -> "It's not very effective..."
        else -> ""
    }
}
