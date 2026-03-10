package com.monstergame.engine.model

import kotlinx.serialization.Serializable

/**
 * Clean-room 18-type system.
 * Mirrors the canonical chart but uses entirely original names.
 */
@Serializable
enum class ElementType {
    NEUTRAL, FLAME, AQUA, VOLT, FLORA, FROST,
    BRAWL, VENOM, TERRA, AERO, PSYCHE, INSECT,
    STONE, SHADE, WYRM, MURK, IRON, RADIANT;

    companion object {
        /** Returns the effectiveness multiplier of [attackType] hitting a [defenseType] target. */
        fun effectiveness(attackType: ElementType, defenseType: ElementType): Double =
            TYPE_CHART[attackType]?.get(defenseType) ?: 1.0

        /**
         * Combined effectiveness against a dual-type defender.
         * Multiplies the two individual effectiveness values.
         */
        fun effectiveness(attackType: ElementType, defenseTypes: List<ElementType>): Double =
            defenseTypes.fold(1.0) { acc, t -> acc * effectiveness(attackType, t) }
    }
}

/**
 * Full 18×18 type chart. Values: 0.0, 0.5, 1.0, 2.0
 * Rows = attacking type. Columns = defending type.
 */
val TYPE_CHART: Map<ElementType, Map<ElementType, Double>> = run {
    fun row(vararg pairs: Pair<ElementType, Double>): Map<ElementType, Double> {
        val map = mutableMapOf<ElementType, Double>()
        ElementType.entries.forEach { map[it] = 1.0 }
        pairs.forEach { (t, v) -> map[t] = v }
        return map
    }

    mapOf(
        ElementType.NEUTRAL to row(
            ElementType.STONE to 0.5,
            ElementType.SHADE to 0.0,
            ElementType.IRON to 0.5
        ),
        ElementType.FLAME to row(
            ElementType.FLAME to 0.5,
            ElementType.AQUA to 0.5,
            ElementType.FLORA to 2.0,
            ElementType.FROST to 2.0,
            ElementType.INSECT to 2.0,
            ElementType.STONE to 0.5,
            ElementType.WYRM to 0.5,
            ElementType.IRON to 2.0
        ),
        ElementType.AQUA to row(
            ElementType.FLAME to 2.0,
            ElementType.AQUA to 0.5,
            ElementType.FLORA to 0.5,
            ElementType.TERRA to 2.0,
            ElementType.STONE to 2.0,
            ElementType.WYRM to 0.5
        ),
        ElementType.VOLT to row(
            ElementType.AQUA to 2.0,
            ElementType.VOLT to 0.5,
            ElementType.FLORA to 0.5,
            ElementType.TERRA to 0.0,
            ElementType.AERO to 2.0,
            ElementType.WYRM to 0.5
        ),
        ElementType.FLORA to row(
            ElementType.FLAME to 0.5,
            ElementType.AQUA to 2.0,
            ElementType.FLORA to 0.5,
            ElementType.VENOM to 0.5,
            ElementType.TERRA to 2.0,
            ElementType.AERO to 0.5,
            ElementType.INSECT to 0.5,
            ElementType.STONE to 2.0,
            ElementType.WYRM to 0.5
        ),
        ElementType.FROST to row(
            ElementType.FLAME to 0.5,
            ElementType.AQUA to 0.5,
            ElementType.FLORA to 2.0,
            ElementType.FROST to 0.5,
            ElementType.TERRA to 2.0,
            ElementType.AERO to 2.0,
            ElementType.WYRM to 2.0,
            ElementType.IRON to 0.5
        ),
        ElementType.BRAWL to row(
            ElementType.NEUTRAL to 2.0,
            ElementType.FROST to 2.0,
            ElementType.VENOM to 0.5,
            ElementType.AERO to 0.5,
            ElementType.PSYCHE to 0.5,
            ElementType.INSECT to 0.5,
            ElementType.STONE to 2.0,
            ElementType.SHADE to 0.0,
            ElementType.MURK to 2.0,
            ElementType.IRON to 2.0,
            ElementType.RADIANT to 0.5
        ),
        ElementType.VENOM to row(
            ElementType.FLORA to 2.0,
            ElementType.VENOM to 0.5,
            ElementType.TERRA to 0.5,
            ElementType.STONE to 0.5,
            ElementType.SHADE to 0.5,
            ElementType.IRON to 0.0,
            ElementType.RADIANT to 2.0
        ),
        ElementType.TERRA to row(
            ElementType.FLAME to 2.0,
            ElementType.VOLT to 2.0,
            ElementType.FLORA to 0.5,
            ElementType.VENOM to 2.0,
            ElementType.AERO to 0.0,
            ElementType.STONE to 2.0,
            ElementType.IRON to 2.0
        ),
        ElementType.AERO to row(
            ElementType.VOLT to 0.5,
            ElementType.FLORA to 2.0,
            ElementType.BRAWL to 2.0,
            ElementType.INSECT to 2.0,
            ElementType.STONE to 0.5,
            ElementType.IRON to 0.5
        ),
        ElementType.PSYCHE to row(
            ElementType.BRAWL to 2.0,
            ElementType.VENOM to 2.0,
            ElementType.PSYCHE to 0.5,
            ElementType.MURK to 0.0,
            ElementType.IRON to 0.5
        ),
        ElementType.INSECT to row(
            ElementType.FLAME to 0.5,
            ElementType.FLORA to 2.0,
            ElementType.BRAWL to 0.5,
            ElementType.VENOM to 0.5,
            ElementType.AERO to 0.5,
            ElementType.PSYCHE to 2.0,
            ElementType.SHADE to 0.5,
            ElementType.MURK to 2.0,
            ElementType.IRON to 0.5,
            ElementType.RADIANT to 0.5
        ),
        ElementType.STONE to row(
            ElementType.FLAME to 2.0,
            ElementType.FROST to 2.0,
            ElementType.BRAWL to 0.5,
            ElementType.TERRA to 0.5,
            ElementType.AERO to 2.0,
            ElementType.INSECT to 2.0,
            ElementType.IRON to 0.5
        ),
        ElementType.SHADE to row(
            ElementType.NEUTRAL to 0.0,
            ElementType.PSYCHE to 2.0,
            ElementType.SHADE to 2.0,
            ElementType.MURK to 0.5
        ),
        ElementType.WYRM to row(
            ElementType.WYRM to 2.0,
            ElementType.IRON to 0.5,
            ElementType.RADIANT to 0.0
        ),
        ElementType.MURK to row(
            ElementType.BRAWL to 0.5,
            ElementType.PSYCHE to 2.0,
            ElementType.SHADE to 2.0,
            ElementType.MURK to 0.5,
            ElementType.RADIANT to 0.5
        ),
        ElementType.IRON to row(
            ElementType.FLAME to 0.5,
            ElementType.AQUA to 0.5,
            ElementType.VOLT to 0.5,
            ElementType.FROST to 2.0,
            ElementType.STONE to 2.0,
            ElementType.IRON to 0.5,
            ElementType.RADIANT to 2.0
        ),
        ElementType.RADIANT to row(
            ElementType.BRAWL to 2.0,
            ElementType.VENOM to 0.5,
            ElementType.WYRM to 2.0,
            ElementType.MURK to 2.0,
            ElementType.IRON to 0.5
        )
    )
}

@Serializable
enum class MoveCategory { PHYSICAL, SPECIAL, STATUS }
