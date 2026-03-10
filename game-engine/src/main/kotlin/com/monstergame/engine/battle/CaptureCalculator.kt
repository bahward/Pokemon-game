package com.monstergame.engine.battle

import com.monstergame.engine.model.*
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random

object CaptureCalculator {

    /**
     * Returns whether the creature was caught and how many shake checks passed (0–4).
     * Uses the formula from the design doc.
     */
    fun attempt(
        creature: CreatureInstance,
        species: CreatureSpecies,
        ball: Item
    ): CaptureResult {
        if (ball.ballType == BallType.MASTER) {
            return CaptureResult(caught = true, shakes = 4, catchValue = 255.0)
        }

        val maxHp = creature.maxHp.toDouble()
        val currentHp = max(1, creature.currentHp).toDouble()

        val hpModifier = (3.0 * maxHp - 2.0 * currentHp) / (3.0 * maxHp)

        val statusMod = when (creature.statusCondition) {
            StatusCondition.SLEEP, StatusCondition.FREEZE -> 2.5
            StatusCondition.PARALYZE, StatusCondition.POISON,
            StatusCondition.BAD_POISON, StatusCondition.BURN -> 1.5
            else -> 1.0
        }

        val ballMod = ballModifier(ball, creature, species)

        val catchRate = species.catchRate.toDouble()
        val catchValue = min(255.0, floor(hpModifier * catchRate * ballMod * statusMod))

        if (catchValue >= 255.0) {
            return CaptureResult(caught = true, shakes = 4, catchValue = catchValue)
        }

        // Check if immediately caught
        if (Random.nextInt(256) < catchValue) {
            return CaptureResult(caught = true, shakes = 4, catchValue = catchValue)
        }

        // Shake checks
        val shakeThreshold = floor(1048560.0 / sqrt(sqrt(16711680.0 / catchValue))).toInt()
        var shakes = 0
        for (i in 0 until 4) {
            if (Random.nextInt(65536) < shakeThreshold) shakes++ else break
        }

        return CaptureResult(
            caught = shakes == 4,
            shakes = shakes,
            catchValue = catchValue
        )
    }

    private fun ballModifier(ball: Item, creature: CreatureInstance, species: CreatureSpecies): Double =
        when (ball.ballType) {
            BallType.BASIC -> 1.0
            BallType.GOOD -> 1.5
            BallType.ULTRA -> 2.0
            BallType.NET -> if (ElementType.AQUA in species.types || ElementType.INSECT in species.types) 3.5 else 1.0
            BallType.DUSK -> 3.5  // Assume cave context handled by caller
            BallType.QUICK -> if (creature.statStages.speed >= 0) 5.0 else 1.0  // first turn
            BallType.TIMER -> 1.0  // simplified; normally increases with turns
            BallType.REPEAT -> 1.0 // simplified
            BallType.MASTER -> 255.0
            null -> 1.0
        }
}

data class CaptureResult(
    val caught: Boolean,
    val shakes: Int,       // 0–4
    val catchValue: Double
)
