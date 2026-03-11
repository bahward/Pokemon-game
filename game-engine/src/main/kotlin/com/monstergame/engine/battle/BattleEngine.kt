package com.monstergame.engine.battle

import com.monstergame.engine.model.*
import kotlin.math.max
import kotlin.random.Random

/**
 * Pure functional battle engine.
 * All state transformations return new BattleState objects.
 */
class BattleEngine(
    private val speciesRepo: Map<Int, CreatureSpecies>,
    private val moveRepo: Map<Int, Move>,
    private val itemRepo: Map<Int, Item>
) {

    private fun ai(state: BattleState): AIController =
        AIController(
            state.trainerData?.aiProfile ?: AIProfile.RANDOM,
            speciesRepo, moveRepo
        )

    /**
     * Execute one full turn given player and (auto-determined) opponent actions.
     * Returns TurnResult with updated state and log messages.
     */
    fun executeTurn(state: BattleState, playerAction: BattleAction): TurnResult {
        if (state.outcome != BattleOutcome.ONGOING) return TurnResult(state, emptyList())

        val messages = mutableListOf<String>()
        var s = state

        // Determine opponent action (wild = random move, trainer = AI)
        val opponentAction: BattleAction = if (state.battleType == BattleType.WILD) {
            val moves = state.opponent.active.moves
            val usable = moves.indices.filter { moves[it].currentPp > 0 }
            BattleAction.UseMove(if (usable.isEmpty()) 0 else usable.random())
        } else {
            ai(state).chooseAction(state)
        }

        // Handle switches first (priority +6)
        if (playerAction is BattleAction.Switch) {
            val (ns, msgs) = doSwitch(s, isPlayer = true, index = playerAction.partyIndex)
            s = ns; messages += msgs
        }
        if (opponentAction is BattleAction.Switch) {
            val (ns, msgs) = doSwitch(s, isPlayer = false, index = opponentAction.partyIndex)
            s = ns; messages += msgs
        }

        // Run attempt
        if (playerAction is BattleAction.Run) {
            val runResult = tryRun(s)
            messages.add(runResult.second)
            if (runResult.first) return TurnResult(s.copy(outcome = BattleOutcome.RAN_AWAY, log = s.log + messages), messages)
            s = s.copy(runAttempts = s.runAttempts + 1)
        }

        // Ball throw
        if (playerAction is BattleAction.ThrowBall) {
            val (ns, msgs, outcome) = doThrowBall(s, playerAction.itemId)
            s = ns; messages += msgs
            if (outcome != null) return TurnResult(s.copy(outcome = outcome, log = s.log + messages), messages)
        }

        // Item use
        if (playerAction is BattleAction.UseItem) {
            val (ns, msgs) = doUseItem(s, isPlayer = true, itemId = playerAction.itemId, targetIndex = playerAction.targetIndex)
            s = ns; messages += msgs
        }
        if (opponentAction is BattleAction.UseItem) {
            val (ns, msgs) = doUseItem(s, isPlayer = false, itemId = opponentAction.itemId, targetIndex = opponentAction.targetIndex)
            s = ns; messages += msgs
        }

        // Determine move order
        val playerMove = if (playerAction is BattleAction.UseMove) playerAction.moveIndex else null
        val opponentMove = if (opponentAction is BattleAction.UseMove) opponentAction.moveIndex else null

        val playerFirst = determineTurnOrder(s, playerMove, opponentMove)

        // Execute moves in order
        if (playerFirst) {
            if (playerMove != null && s.outcome == BattleOutcome.ONGOING) {
                val (ns, msgs) = executeMove(s, isPlayer = true, moveIndex = playerMove)
                s = ns; messages += msgs
            }
            if (opponentMove != null && s.outcome == BattleOutcome.ONGOING) {
                val (ns, msgs) = executeMove(s, isPlayer = false, moveIndex = opponentMove)
                s = ns; messages += msgs
            }
        } else {
            if (opponentMove != null && s.outcome == BattleOutcome.ONGOING) {
                val (ns, msgs) = executeMove(s, isPlayer = false, moveIndex = opponentMove)
                s = ns; messages += msgs
            }
            if (playerMove != null && s.outcome == BattleOutcome.ONGOING) {
                val (ns, msgs) = executeMove(s, isPlayer = true, moveIndex = playerMove)
                s = ns; messages += msgs
            }
        }

        // End of turn: weather, status damage
        if (s.outcome == BattleOutcome.ONGOING) {
            val (ns, msgs) = applyEndOfTurn(s)
            s = ns; messages += msgs
        }

        // Check outcome
        s = checkOutcome(s, messages)

        return TurnResult(s.copy(turn = s.turn + 1, log = s.log + messages), messages)
    }

    // --- Move execution ---

    private fun executeMove(
        state: BattleState,
        isPlayer: Boolean,
        moveIndex: Int
    ): Pair<BattleState, List<String>> {
        val messages = mutableListOf<String>()
        var s = state

        val attackerSide = if (isPlayer) s.player else s.opponent
        val defenderSide = if (isPlayer) s.opponent else s.player
        val attacker = attackerSide.active
        val defender = defenderSide.active

        if (!attacker.isAlive) return s to messages

        // Status check (sleep/freeze/paralysis)
        val (canAct, blockMsg) = StatusEffects.canAct(attacker)
        if (!canAct) {
            messages.add(blockMsg ?: "")
            return s to messages
        }

        val moveSlot = attacker.moves.getOrNull(moveIndex) ?: return s to messages
        if (moveSlot.currentPp <= 0) {
            messages.add("${attacker.nickname} has no PP left for that move!")
            return s to messages
        }
        val move = moveRepo[moveSlot.moveId] ?: return s to messages

        messages.add("${attacker.nickname} used ${move.name}!")

        // Deduct PP
        val newSlots = attacker.moves.toMutableList()
        newSlots[moveIndex] = moveSlot.copy(currentPp = moveSlot.currentPp - 1)
        val newAttacker = attacker.copy(moves = newSlots)
        s = if (isPlayer) s.copy(player = s.player.copy(creatures = s.player.creatures.toMutableList().also { it[s.player.activeIndex] = newAttacker }))
            else s.copy(opponent = s.opponent.copy(creatures = s.opponent.creatures.toMutableList().also { it[s.opponent.activeIndex] = newAttacker }))

        // Accuracy check
        val attackerSide2 = if (isPlayer) s.player else s.opponent
        val defenderSide2 = if (isPlayer) s.opponent else s.player
        val attacker2 = attackerSide2.active

        if (!DamageCalculator.rollAccuracy(move, attacker2.statStages.accuracy, defenderSide2.active.statStages.evasion)) {
            messages.add("${attacker2.nickname}'s attack missed!")
            return s to messages
        }

        // Process by category
        return when (move.category) {
            MoveCategory.STATUS -> applyStatusMove(s, isPlayer, move, messages)
            else -> applyDamageMove(s, isPlayer, move, messages)
        }
    }

    private fun applyDamageMove(
        state: BattleState,
        isPlayer: Boolean,
        move: Move,
        messages: MutableList<String>
    ): Pair<BattleState, List<String>> {
        var s = state
        val attackerSide = if (isPlayer) s.player else s.opponent
        val defenderSide = if (isPlayer) s.opponent else s.player
        val attacker = attackerSide.active
        val defender = defenderSide.active

        val attackerSpecies = speciesRepo[attacker.speciesId] ?: return s to messages
        val defenderSpecies = speciesRepo[defender.speciesId] ?: return s to messages

        val isCrit = DamageCalculator.rollCrit()
        val result = DamageCalculator.calculate(attacker, defender, move, attackerSpecies, defenderSpecies, s.weather, isCrit)

        if (result.isImmune) {
            messages.add(result.effectivenessText)
            return s to messages
        }

        if (isCrit) messages.add("A critical hit!")
        if (result.effectivenessText.isNotEmpty()) messages.add(result.effectivenessText)

        // Apply damage to defender
        val newDefender = CreatureFactory.withHp(defender, defender.currentHp - result.damage)
        val newDefenderSide = defenderSide.copy(
            creatures = defenderSide.creatures.toMutableList().also { it[defenderSide.activeIndex] = newDefender }
        )
        s = if (isPlayer) s.copy(opponent = newDefenderSide) else s.copy(player = newDefenderSide)

        if (newDefender.currentHp <= 0) {
            messages.add("${newDefender.nickname} fainted!")
            val faintedSide = if (isPlayer) s.opponent else s.player
            val fainted = faintedSide.creatures.toMutableList()
            fainted[faintedSide.activeIndex] = newDefender.copy(statusCondition = StatusCondition.FAINT)
            val updatedFaintedSide = faintedSide.copy(creatures = fainted)
            s = if (isPlayer) s.copy(opponent = updatedFaintedSide) else s.copy(player = updatedFaintedSide)
        }

        // Recoil
        val recoilDmg = when (move.effect) {
            MoveEffect.RECOIL_25 -> result.damage / 4
            MoveEffect.RECOIL_33 -> result.damage / 3
            else -> 0
        }
        if (recoilDmg > 0) {
            messages.add("${attacker.nickname} is hit by recoil!")
            val newAttacker = CreatureFactory.withHp(attacker, attacker.currentHp - recoilDmg)
            val attackerSide2 = if (isPlayer) s.player else s.opponent
            val newList = attackerSide2.creatures.toMutableList()
            newList[attackerSide2.activeIndex] = newAttacker
            s = if (isPlayer) s.copy(player = s.player.copy(creatures = newList))
                else s.copy(opponent = s.opponent.copy(creatures = newList))
        }

        // Drain
        if (move.effect == MoveEffect.DRAIN_HALF) {
            val drain = result.damage / 2
            messages.add("${attacker.nickname} drained energy!")
            val side = if (isPlayer) s.player else s.opponent
            val newList = side.creatures.toMutableList()
            val newAtt = CreatureFactory.withHp(side.creatures[side.activeIndex],
                side.creatures[side.activeIndex].currentHp + drain)
            newList[side.activeIndex] = newAtt
            s = if (isPlayer) s.copy(player = s.player.copy(creatures = newList))
                else s.copy(opponent = s.opponent.copy(creatures = newList))
        }

        // Secondary effect
        if (move.effectChance > 0 && Random.nextInt(100) < move.effectChance) {
            s = applySecondaryEffect(s, isPlayer, move.effect, messages)
        }

        return s to messages
    }

    private fun applyStatusMove(
        state: BattleState,
        isPlayer: Boolean,
        move: Move,
        messages: MutableList<String>
    ): Pair<BattleState, List<String>> {
        var s = state
        val defenderSide = if (isPlayer) s.opponent else s.player
        val attackerSide = if (isPlayer) s.player else s.opponent
        val defender = defenderSide.active
        val defenderSpecies = speciesRepo[defender.speciesId] ?: return s to messages
        val attacker = attackerSide.active

        when (move.effect) {
            MoveEffect.BURN -> {
                StatusEffects.tryInflict(defender, defenderSpecies, StatusCondition.BURN)?.let { (c, msg) ->
                    messages.add(msg)
                    s = updateCreature(s, !isPlayer, defenderSide.activeIndex, c)
                } ?: messages.add("It had no effect!")
            }
            MoveEffect.POISON -> {
                StatusEffects.tryInflict(defender, defenderSpecies, StatusCondition.POISON)?.let { (c, msg) ->
                    messages.add(msg)
                    s = updateCreature(s, !isPlayer, defenderSide.activeIndex, c)
                } ?: messages.add("It had no effect!")
            }
            MoveEffect.BAD_POISON -> {
                StatusEffects.tryInflict(defender, defenderSpecies, StatusCondition.BAD_POISON)?.let { (c, msg) ->
                    messages.add(msg)
                    s = updateCreature(s, !isPlayer, defenderSide.activeIndex, c)
                } ?: messages.add("It had no effect!")
            }
            MoveEffect.PARALYZE -> {
                StatusEffects.tryInflict(defender, defenderSpecies, StatusCondition.PARALYZE)?.let { (c, msg) ->
                    messages.add(msg)
                    s = updateCreature(s, !isPlayer, defenderSide.activeIndex, c)
                } ?: messages.add("It had no effect!")
            }
            MoveEffect.SLEEP -> {
                StatusEffects.tryInflict(defender, defenderSpecies, StatusCondition.SLEEP)?.let { (c, msg) ->
                    messages.add(msg)
                    s = updateCreature(s, !isPlayer, defenderSide.activeIndex, c)
                } ?: messages.add("It had no effect!")
            }
            MoveEffect.CONFUSE -> {
                messages.add("${defender.nickname} became confused!")
                // Confusion handled as volatile status (simplified: 50% chance self-damage for 2-5 turns)
            }
            MoveEffect.HEAL_HALF -> {
                val heal = attacker.maxHp / 2
                val healed = CreatureFactory.withHp(attacker, attacker.currentHp + heal)
                s = updateCreature(s, isPlayer, attackerSide.activeIndex, healed)
                messages.add("${attacker.nickname} restored HP!")
            }
            MoveEffect.ATK_UP1 -> {
                val updated = updateStatStage(attacker, "atk", 1)
                s = updateCreature(s, isPlayer, attackerSide.activeIndex, updated.first)
                messages.add(updated.second)
            }
            MoveEffect.DEF_UP1 -> {
                val updated = updateStatStage(attacker, "def", 1)
                s = updateCreature(s, isPlayer, attackerSide.activeIndex, updated.first)
                messages.add(updated.second)
            }
            MoveEffect.SP_ATK_UP1 -> {
                val updated = updateStatStage(attacker, "spatk", 1)
                s = updateCreature(s, isPlayer, attackerSide.activeIndex, updated.first)
                messages.add(updated.second)
            }
            MoveEffect.SPD_UP1 -> {
                val updated = updateStatStage(attacker, "spd", 1)
                s = updateCreature(s, isPlayer, attackerSide.activeIndex, updated.first)
                messages.add(updated.second)
            }
            MoveEffect.ATK_DOWN1 -> {
                val updated = updateStatStage(defender, "atk", -1)
                s = updateCreature(s, !isPlayer, defenderSide.activeIndex, updated.first)
                messages.add(updated.second)
            }
            MoveEffect.DEF_DOWN1 -> {
                val updated = updateStatStage(defender, "def", -1)
                s = updateCreature(s, !isPlayer, defenderSide.activeIndex, updated.first)
                messages.add(updated.second)
            }
            MoveEffect.SP_DEF_DOWN1 -> {
                val updated = updateStatStage(defender, "spdef", -1)
                s = updateCreature(s, !isPlayer, defenderSide.activeIndex, updated.first)
                messages.add(updated.second)
            }
            MoveEffect.ACC_DOWN1 -> {
                val updated = updateStatStage(defender, "acc", -1)
                s = updateCreature(s, !isPlayer, defenderSide.activeIndex, updated.first)
                messages.add(updated.second)
            }
            MoveEffect.WEATHER_SUN -> {
                s = s.copy(weather = WeatherEffect.SUN, weatherTurns = 5)
                messages.add("The sunlight turned harsh!")
            }
            MoveEffect.WEATHER_RAIN -> {
                s = s.copy(weather = WeatherEffect.RAIN, weatherTurns = 5)
                messages.add("It started to rain!")
            }
            MoveEffect.WEATHER_SAND -> {
                s = s.copy(weather = WeatherEffect.SANDSTORM, weatherTurns = 5)
                messages.add("A sandstorm kicked up!")
            }
            MoveEffect.WEATHER_HAIL -> {
                s = s.copy(weather = WeatherEffect.HAIL, weatherTurns = 5)
                messages.add("It started to hail!")
            }
            else -> messages.add("But nothing happened…")
        }

        return s to messages
    }

    private fun applySecondaryEffect(
        state: BattleState,
        isPlayer: Boolean,
        effect: MoveEffect,
        messages: MutableList<String>
    ): BattleState {
        val defenderSide = if (isPlayer) state.opponent else state.player
        val defender = defenderSide.active
        val defenderSpecies = speciesRepo[defender.speciesId] ?: return state

        val statusToInflict = when (effect) {
            MoveEffect.BURN -> StatusCondition.BURN
            MoveEffect.POISON -> StatusCondition.POISON
            MoveEffect.PARALYZE -> StatusCondition.PARALYZE
            MoveEffect.FREEZE -> StatusCondition.FREEZE
            else -> null
        }
        if (statusToInflict != null) {
            StatusEffects.tryInflict(defender, defenderSpecies, statusToInflict)?.let { (c, msg) ->
                messages.add(msg)
                return updateCreature(state, !isPlayer, defenderSide.activeIndex, c)
            }
        }
        return state
    }

    // --- Capture ---

    private fun doThrowBall(
        state: BattleState,
        itemId: Int
    ): Triple<BattleState, List<String>, BattleOutcome?> {
        val messages = mutableListOf<String>()
        if (state.battleType != BattleType.WILD) {
            messages.add("You can't catch trainer creatures!")
            return Triple(state, messages, null)
        }

        val ball = itemRepo[itemId] ?: run {
            messages.add("No such item!")
            return Triple(state, messages, null)
        }

        val target = state.opponent.active
        val species = speciesRepo[target.speciesId] ?: return Triple(state, messages, null)

        messages.add("You threw a ${ball.name}!")
        val result = CaptureCalculator.attempt(target, species, ball)

        repeat(result.shakes) { messages.add("*shake*") }

        return if (result.caught) {
            messages.add("Gotcha! ${target.nickname} was caught!")
            Triple(state.copy(outcome = BattleOutcome.CAUGHT), messages, BattleOutcome.CAUGHT)
        } else {
            messages.add("Oh no! ${target.nickname} broke free!")
            Triple(state, messages, null)
        }
    }

    // --- Switch ---

    private fun doSwitch(
        state: BattleState,
        isPlayer: Boolean,
        index: Int
    ): Pair<BattleState, List<String>> {
        val side = if (isPlayer) state.player else state.opponent
        val incoming = side.creatures.getOrNull(index) ?: return state to listOf("Invalid switch!")
        if (!incoming.isAlive) return state to listOf("${incoming.nickname} can't battle!")

        val messages = listOf("${side.active.nickname} come back!", "Go, ${incoming.nickname}!")
        val newSide = side.copy(
            activeIndex = index,
            creatures = side.creatures.map { it.copy(statStages = StatStages()) }.toMutableList()
        )
        return if (isPlayer) state.copy(player = newSide) to messages
        else state.copy(opponent = newSide) to messages
    }

    // --- Item use ---

    private fun doUseItem(
        state: BattleState,
        isPlayer: Boolean,
        itemId: Int,
        targetIndex: Int
    ): Pair<BattleState, List<String>> {
        val messages = mutableListOf<String>()
        val item = itemRepo[itemId] ?: return state to messages
        val side = if (isPlayer) state.player else state.opponent
        val target = side.creatures.getOrNull(targetIndex) ?: return state to messages

        var newTarget = target
        when {
            item.curesAll -> {
                newTarget = CreatureFactory.fullHeal(target)
                messages.add("${target.nickname} was fully healed!")
            }
            item.healAmount > 0 -> {
                newTarget = CreatureFactory.withHp(target, target.currentHp + item.healAmount)
                messages.add("${target.nickname} recovered ${item.healAmount} HP!")
            }
            item.revives -> {
                if (target.statusCondition == StatusCondition.FAINT) {
                    val reviveHp = if (item.reviveFull) target.maxHp else target.maxHp / 2
                    newTarget = target.copy(currentHp = reviveHp, statusCondition = StatusCondition.NONE)
                    messages.add("${target.nickname} was revived!")
                }
            }
            item.curesStatus -> {
                newTarget = target.copy(statusCondition = StatusCondition.NONE, statusTurns = 0)
                messages.add("${target.nickname}'s status was cured!")
            }
        }

        val newList = side.creatures.toMutableList()
        newList[targetIndex] = newTarget
        val newSide = side.copy(creatures = newList)
        return if (isPlayer) state.copy(player = newSide) to messages
        else state.copy(opponent = newSide) to messages
    }

    // --- End of turn ---

    private fun applyEndOfTurn(state: BattleState): Pair<BattleState, List<String>> {
        val messages = mutableListOf<String>()
        var s = state

        // Weather tick
        if (s.weather != WeatherEffect.CLEAR) {
            val newTurns = s.weatherTurns - 1
            if (newTurns <= 0) {
                s = s.copy(weather = WeatherEffect.CLEAR, weatherTurns = 0)
                messages.add("The weather cleared up.")
            } else {
                s = s.copy(weatherTurns = newTurns)
                when (s.weather) {
                    WeatherEffect.SANDSTORM -> messages.add("The sandstorm rages!")
                    WeatherEffect.HAIL -> messages.add("Hail continues to fall!")
                    else -> {}
                }
            }
        }

        // Status effects
        for (isPlayer in listOf(true, false)) {
            val side = if (isPlayer) s.player else s.opponent
            val (newCreature, msgs) = StatusEffects.applyEndOfTurn(side.active)
            messages += msgs
            s = updateCreature(s, isPlayer, side.activeIndex, newCreature)
        }

        return s to messages
    }

    // --- Outcome check ---

    private fun checkOutcome(state: BattleState, messages: MutableList<String>): BattleState {
        var s = state
        if (!s.opponent.hasAlive()) {
            messages.add("You won the battle!")
            s = s.copy(outcome = BattleOutcome.PLAYER_WIN)
        } else if (!s.player.hasAlive()) {
            messages.add("You blacked out…")
            s = s.copy(outcome = BattleOutcome.PLAYER_LOSE)
        } else {
            // Auto-send next creature
            if (!s.opponent.active.isAlive && s.opponent.hasAlive()) {
                val nextIdx = s.opponent.firstAliveIndex()
                messages.add("Opponent sends out ${s.opponent.creatures[nextIdx].nickname}!")
                s = s.copy(opponent = s.opponent.copy(activeIndex = nextIdx))
            }
        }
        return s
    }

    // --- Turn order ---

    private fun determineTurnOrder(
        state: BattleState,
        playerMoveIdx: Int?,
        oppMoveIdx: Int?
    ): Boolean { // true = player first
        val playerMove = playerMoveIdx?.let { state.player.active.moves.getOrNull(it)?.moveId?.let { id -> moveRepo[id] } }
        val oppMove = oppMoveIdx?.let { state.opponent.active.moves.getOrNull(it)?.moveId?.let { id -> moveRepo[id] } }

        val playerPriority = playerMove?.priority ?: 0
        val oppPriority = oppMove?.priority ?: 0

        if (playerPriority != oppPriority) return playerPriority > oppPriority

        val playerSpd = (state.player.active.calculatedStats.speed *
                state.player.active.statStages.stageMult(state.player.active.statStages.speed)).toInt()
        val oppSpd = (state.opponent.active.calculatedStats.speed *
                state.opponent.active.statStages.stageMult(state.opponent.active.statStages.speed)).toInt()

        return when {
            playerSpd > oppSpd -> true
            oppSpd > playerSpd -> false
            else -> Random.nextBoolean()
        }
    }

    // --- Run attempt ---

    /**
     * Attempts to flee from a wild battle.
     * Returns Pair(success, message).
     * Formula: fleeFactor = (playerSpd * 128 / (opponentSpd + 1) + 30 * attempts) clamped to [0, 255].
     * If fleeFactor >= 255 the escape always succeeds; otherwise a random roll decides.
     */
    private fun tryRun(state: BattleState): Pair<Boolean, String> {
        if (!state.canRun) return false to "You can't escape from this battle!"
        if (state.battleType != BattleType.WILD) return false to "There's no running from a trainer battle!"

        val playerSpd  = state.player.active.calculatedStats.speed
        val opponentSpd = state.opponent.active.calculatedStats.speed
        val attempts   = state.runAttempts + 1

        val fleeFactor = ((playerSpd * 128) / (opponentSpd + 1) + 30 * attempts).coerceIn(0, 255)
        return if (fleeFactor >= 255 || Random.nextInt(256) < fleeFactor) {
            true  to "Got away safely!"
        } else {
            false to "Can't escape!"
        }
    }

    // --- Helpers ---

    private fun updateCreature(
        state: BattleState,
        isPlayer: Boolean,
        index: Int,
        creature: CreatureInstance
    ): BattleState {
        val side = if (isPlayer) state.player else state.opponent
        val newList = side.creatures.toMutableList()
        newList[index] = creature
        val newSide = side.copy(creatures = newList)
        return if (isPlayer) state.copy(player = newSide) else state.copy(opponent = newSide)
    }

    private fun updateStatStage(
        creature: CreatureInstance,
        stat: String,
        delta: Int
    ): Pair<CreatureInstance, String> {
        val stages = creature.statStages
        val (newStages, statName) = when (stat) {
            "atk" -> stages.copy(attack = (stages.attack + delta).coerceIn(-6, 6)) to "Attack"
            "def" -> stages.copy(defense = (stages.defense + delta).coerceIn(-6, 6)) to "Defense"
            "spatk" -> stages.copy(specialAttack = (stages.specialAttack + delta).coerceIn(-6, 6)) to "Sp. Atk"
            "spdef" -> stages.copy(specialDefense = (stages.specialDefense + delta).coerceIn(-6, 6)) to "Sp. Def"
            "spd" -> stages.copy(speed = (stages.speed + delta).coerceIn(-6, 6)) to "Speed"
            "acc" -> stages.copy(accuracy = (stages.accuracy + delta).coerceIn(-6, 6)) to "Accuracy"
            "eva" -> stages.copy(evasion = (stages.evasion + delta).coerceIn(-6, 6)) to "Evasion"
            else -> stages to "?"
        }
        val direction = if (delta > 0) "rose" else "fell"
        val magnitude = when (kotlin.math.abs(delta)) { 1 -> ""; 2 -> " sharply"; else -> " drastically" }
        return creature.copy(statStages = newStages) to "${creature.nickname}'s $statName$magnitude $direction!"
    }

    // --- EXP gain ---

    fun calculateExpGain(
        winner: CreatureInstance,
        loser: CreatureInstance,
        loserSpecies: CreatureSpecies,
        isWild: Boolean
    ): Long {
        val base = loserSpecies.baseExp.toLong()
        val luckyMod = if (!isWild) 1.5 else 1.0
        return ((base * loser.level * luckyMod) / 7).toLong()
    }

    fun tryLevelUp(
        creature: CreatureInstance,
        species: CreatureSpecies
    ): Pair<CreatureInstance, List<String>> {
        var c = creature
        val messages = mutableListOf<String>()
        while (c.level < 100 && c.exp >= CreatureFactory.expForLevel(c.level + 1)) {
            val newLevel = c.level + 1
            val newStats = CreatureFactory.calculateStats(species.baseStats, c.ivs, newLevel)
            val hpGain = CreatureFactory.calculateHp(species.baseStats.hp, c.ivs.hp, newLevel) - c.maxHp
            c = c.copy(
                level = newLevel,
                maxHp = c.maxHp + hpGain,
                currentHp = c.currentHp + hpGain,
                calculatedStats = newStats,
                expToNextLevel = CreatureFactory.expForLevel(newLevel + 1) - CreatureFactory.expForLevel(newLevel)
            )
            messages.add("${c.nickname} grew to level $newLevel!")

            // Check new moves
            val newMoves = species.learnset.filter { it.level == newLevel }
            for (entry in newMoves) {
                val move = moveRepo[entry.moveId]
                if (move != null) {
                    if (c.moves.size < 4) {
                        c = c.copy(moves = c.moves + MoveSlot(move.id, move.pp, move.pp))
                        messages.add("${c.nickname} learned ${move.name}!")
                    } else {
                        messages.add("${c.nickname} wants to learn ${move.name}… (party full)")
                    }
                }
            }
        }
        return c to messages
    }
}
