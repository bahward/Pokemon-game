package com.monstergame.engine.model

import kotlinx.serialization.Serializable
import kotlin.math.floor
import kotlin.math.max
import kotlin.random.Random

@Serializable
data class BaseStats(
    val hp: Int,
    val attack: Int,
    val defense: Int,
    val specialAttack: Int,
    val specialDefense: Int,
    val speed: Int
)

@Serializable
data class CreatureSpecies(
    val id: Int,
    val name: String,
    val types: List<ElementType>,
    val baseStats: BaseStats,
    val catchRate: Int,         // 0–255
    val baseExp: Int,
    val learnset: List<LearnEntry>,
    val evolutions: List<EvolutionEntry>,
    val regionId: String,
    val isStarter: Boolean = false,
    val isLegendary: Boolean = false,
    val description: String = "",
    val habitat: List<String> = emptyList()
)

@Serializable
data class LearnEntry(val level: Int, val moveId: Int)

@Serializable
enum class EvolutionMethod { LEVEL, ITEM, HAPPINESS }

@Serializable
data class EvolutionEntry(
    val toId: Int,
    val method: EvolutionMethod,
    val param: Int = 0,  // level threshold or item id
    val paramStr: String = ""
)

@Serializable
enum class StatusCondition { NONE, BURN, POISON, BAD_POISON, PARALYZE, SLEEP, FREEZE, FAINT }

@Serializable
data class InstanceStats(
    val hp: Int,
    val attack: Int,
    val defense: Int,
    val specialAttack: Int,
    val specialDefense: Int,
    val speed: Int
)

/** A live creature instance in the player's party or PC. */
@Serializable
data class CreatureInstance(
    val uid: Long,               // unique save ID
    val speciesId: Int,
    val nickname: String,
    val level: Int,
    val currentHp: Int,
    val maxHp: Int,
    val ivs: BaseStats,          // Individual Values 0–31
    val calculatedStats: InstanceStats,
    val moves: List<MoveSlot>,   // up to 4
    val exp: Long,
    val expToNextLevel: Long,
    val statusCondition: StatusCondition = StatusCondition.NONE,
    val statusTurns: Int = 0,    // turns remaining for SLP/FRZ
    val badPoisonCounter: Int = 0,
    val caughtInArea: String = "",
    val isShiny: Boolean = false,
    // volatile battle stats (not persisted between battles)
    val statStages: StatStages = StatStages()
) {
    val isAlive: Boolean get() = currentHp > 0 && statusCondition != StatusCondition.FAINT
    val hpPercent: Float get() = if (maxHp == 0) 0f else currentHp.toFloat() / maxHp.toFloat()
}

@Serializable
data class MoveSlot(
    val moveId: Int,
    val currentPp: Int,
    val maxPp: Int
)

/** In-battle stat stage modifiers (-6 to +6). NOT persisted between battles. */
@Serializable
data class StatStages(
    val attack: Int = 0,
    val defense: Int = 0,
    val specialAttack: Int = 0,
    val specialDefense: Int = 0,
    val speed: Int = 0,
    val accuracy: Int = 0,
    val evasion: Int = 0
)

fun StatStages.stageMult(stage: Int): Double = when {
    stage >= 6 -> 4.0
    stage == 5 -> 3.5
    stage == 4 -> 3.0
    stage == 3 -> 2.5
    stage == 2 -> 2.0
    stage == 1 -> 1.5
    stage == 0 -> 1.0
    stage == -1 -> 0.667
    stage == -2 -> 0.5
    stage == -3 -> 0.4
    stage == -4 -> 0.333
    stage == -5 -> 0.285
    else -> 0.25
}

object CreatureFactory {
    fun createFromSpecies(
        species: CreatureSpecies,
        level: Int,
        uid: Long = System.nanoTime() + Random.nextLong(0, 1_000_000),
        ivOverride: BaseStats? = null
    ): CreatureInstance {
        val ivs = ivOverride ?: BaseStats(
            hp = Random.nextInt(0, 32),
            attack = Random.nextInt(0, 32),
            defense = Random.nextInt(0, 32),
            specialAttack = Random.nextInt(0, 32),
            specialDefense = Random.nextInt(0, 32),
            speed = Random.nextInt(0, 32)
        )
        val stats = calculateStats(species.baseStats, ivs, level)
        val maxHp = calculateHp(species.baseStats.hp, ivs.hp, level)

        // Build moveset: last 4 learnable moves at this level
        val learnableMoves = species.learnset
            .filter { it.level <= level }
            .takeLast(4)
            .map { MoveSlot(it.moveId, 0, 0) } // pp filled by content layer

        val expToNext = expForLevel(level + 1) - expForLevel(level)

        return CreatureInstance(
            uid = uid,
            speciesId = species.id,
            nickname = species.name,
            level = level,
            currentHp = maxHp,
            maxHp = maxHp,
            ivs = ivs,
            calculatedStats = stats,
            moves = learnableMoves,
            exp = expForLevel(level),
            expToNextLevel = expToNext
        )
    }

    fun calculateHp(baseHp: Int, iv: Int, level: Int): Int =
        floor((2.0 * baseHp + iv) * level / 100.0 + level + 10).toInt()

    fun calculateStats(base: BaseStats, ivs: BaseStats, level: Int): InstanceStats {
        fun stat(b: Int, iv: Int) =
            floor((floor((2.0 * b + iv) * level / 100.0) + 5)).toInt()
        return InstanceStats(
            hp = calculateHp(base.hp, ivs.hp, level),
            attack = stat(base.attack, ivs.attack),
            defense = stat(base.defense, ivs.defense),
            specialAttack = stat(base.specialAttack, ivs.specialAttack),
            specialDefense = stat(base.specialDefense, ivs.specialDefense),
            speed = stat(base.speed, ivs.speed)
        )
    }

    /** Medium-fast EXP curve: n^3 */
    fun expForLevel(level: Int): Long = level.toLong() * level.toLong() * level.toLong()

    fun withHp(c: CreatureInstance, newHp: Int): CreatureInstance =
        c.copy(currentHp = max(0, newHp))

    fun heal(c: CreatureInstance, amount: Int): CreatureInstance =
        c.copy(currentHp = minOf(c.maxHp, c.currentHp + amount), statusCondition = StatusCondition.NONE, statusTurns = 0, badPoisonCounter = 0)

    fun fullHeal(c: CreatureInstance): CreatureInstance =
        c.copy(currentHp = c.maxHp, statusCondition = StatusCondition.NONE, statusTurns = 0, badPoisonCounter = 0,
               statStages = StatStages(),
               moves = c.moves.map { it.copy(currentPp = it.maxPp) })

    fun addExp(c: CreatureInstance, gain: Long): CreatureInstance =
        c.copy(exp = c.exp + gain)
}
