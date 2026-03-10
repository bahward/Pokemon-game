package com.monstergame.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monstergame.content.loader.ContentRepository
import com.monstergame.engine.battle.BattleEngine
import com.monstergame.engine.model.*
import com.monstergame.engine.overworld.EncounterGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BattleUiState(
    val battleState: BattleState? = null,
    val messages: List<String> = emptyList(),
    val displayedMessages: List<String> = emptyList(),
    val isAnimating: Boolean = false,
    val isPlayerTurn: Boolean = true,
    val outcome: BattleOutcome = BattleOutcome.ONGOING,
    val expGains: List<Pair<String, Long>> = emptyList(),  // name -> exp
    val newBadge: String? = null,
    val caughtCreature: CreatureInstance? = null
)

@HiltViewModel
class BattleViewModel @Inject constructor(
    private val repo: ContentRepository,
    private val gameViewModel: GameViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(BattleUiState())
    val uiState: StateFlow<BattleUiState> = _uiState.asStateFlow()

    private val engine by lazy {
        BattleEngine(repo.creatures, repo.moves, repo.items)
    }

    /**
     * Set up a wild encounter battle.
     */
    fun startWildBattle(wildCreature: CreatureInstance) {
        val save = gameViewModel.save ?: return
        val party = save.party.filter { it.isAlive }.toMutableList()
        if (party.isEmpty()) return

        // Fill move PP from repo if slots have 0 PP
        val filledParty = party.map { c ->
            c.copy(moves = c.moves.map { slot ->
                if (slot.maxPp == 0) {
                    val m = repo.getMove(slot.moveId)
                    slot.copy(currentPp = m?.pp ?: 1, maxPp = m?.pp ?: 1)
                } else slot
            })
        }

        val wildFilled = wildCreature.copy(moves = wildCreature.moves.map { slot ->
            val m = repo.getMove(slot.moveId)
            slot.copy(currentPp = m?.pp ?: 1, maxPp = m?.pp ?: 1)
        })

        gameViewModel.recordSeen(wildCreature.speciesId)

        val state = BattleState(
            battleType = BattleType.WILD,
            player     = BattleSide(filledParty.toMutableList()),
            opponent   = BattleSide(mutableListOf(wildFilled)),
            canRun     = true
        )
        _uiState.update {
            it.copy(
                battleState = state,
                isPlayerTurn = true,
                outcome = BattleOutcome.ONGOING,
                messages = listOf("A wild ${wildCreature.nickname} appeared!"),
                displayedMessages = listOf("A wild ${wildCreature.nickname} appeared!")
            )
        }
    }

    /**
     * Set up a trainer battle.
     */
    fun startTrainerBattle(trainerId: String) {
        val save = gameViewModel.save ?: return
        val trainer = repo.getTrainer(trainerId) ?: return
        val party = save.party.filter { it.isAlive }.toMutableList()
        if (party.isEmpty()) return

        val filledParty = party.map { fillMovePp(it) }

        val trainerTeam = EncounterGenerator.buildTrainerTeam(trainer, repo.creatures, repo.moves)
            .map { fillMovePp(it) }
            .toMutableList()

        trainerTeam.forEach { gameViewModel.recordSeen(it.speciesId) }

        val state = BattleState(
            battleType  = BattleType.TRAINER,
            player      = BattleSide(filledParty.toMutableList()),
            opponent    = BattleSide(trainerTeam),
            trainerData = trainer,
            canRun      = false
        )
        _uiState.update {
            it.copy(
                battleState = state,
                isPlayerTurn = true,
                outcome = BattleOutcome.ONGOING,
                messages = listOf(trainer.preBattleText),
                displayedMessages = listOf(trainer.preBattleText)
            )
        }
    }

    private fun fillMovePp(c: CreatureInstance): CreatureInstance = c.copy(
        moves = c.moves.map { slot ->
            if (slot.maxPp == 0) {
                val m = repo.getMove(slot.moveId)
                slot.copy(currentPp = m?.pp ?: 1, maxPp = m?.pp ?: 1)
            } else slot
        }
    )

    fun playerAction(action: BattleAction) {
        val state = _uiState.value.battleState ?: return
        if (!_uiState.value.isPlayerTurn || _uiState.value.isAnimating) return

        _uiState.update { it.copy(isAnimating = true, isPlayerTurn = false) }

        viewModelScope.launch {
            val result = engine.executeTurn(state, action)

            _uiState.update {
                it.copy(
                    battleState = result.newState,
                    messages = result.messages,
                    displayedMessages = emptyList()
                )
            }

            // Animate messages one by one
            for (msg in result.messages) {
                _uiState.update { it.copy(displayedMessages = it.displayedMessages + msg) }
                delay(600)
            }

            val outcome = result.newState.outcome
            _uiState.update { it.copy(outcome = outcome, isAnimating = false) }

            if (outcome == BattleOutcome.ONGOING) {
                _uiState.update { it.copy(isPlayerTurn = true) }
            } else {
                handleBattleEnd(result.newState, action)
            }
        }
    }

    private fun handleBattleEnd(state: BattleState, lastAction: BattleAction) {
        viewModelScope.launch {
            delay(800)
            val save = gameViewModel.save ?: return@launch
            var newSave = save

            when (state.outcome) {
                BattleOutcome.PLAYER_WIN -> {
                    // EXP distribution
                    val expGains = mutableListOf<Pair<String, Long>>()
                    val defeatedOpponent = state.opponent.active
                    val defeatedSpecies = repo.getCreature(defeatedOpponent.speciesId)

                    if (defeatedSpecies != null) {
                        val participants = state.player.creatures.filter { it.isAlive }
                        val expShare = engine.calculateExpGain(
                            participants.first(), defeatedOpponent, defeatedSpecies,
                            state.battleType == BattleType.WILD
                        ) / participants.size

                        val newParty = save.party.toMutableList()
                        for ((i, c) in save.party.withIndex()) {
                            if (c.isAlive) {
                                val withExp = CreatureFactory.addExp(c, expShare)
                                val species = repo.getCreature(c.speciesId) ?: continue
                                val (leveled, msgs) = engine.tryLevelUp(withExp, species)
                                newParty[i] = leveled
                                if (expShare > 0) expGains.add(c.nickname to expShare)
                            }
                        }
                        newSave = newSave.copy(party = newParty)
                        _uiState.update { it.copy(expGains = expGains) }
                    }

                    // Trainer rewards
                    if (state.trainerData != null) {
                        val reward = state.trainerData.reward
                        newSave = newSave.copy(money = newSave.money + reward)
                        gameViewModel.markTrainerDefeated(state.trainerData.id)

                        // Badge award
                        state.trainerData.badgeId?.let { badgeId ->
                            newSave = newSave.withBadge(badgeId)
                            _uiState.update { it.copy(newBadge = badgeId) }
                        }
                    }
                }
                BattleOutcome.CAUGHT -> {
                    val caught = state.opponent.active
                    gameViewModel.recordCaught(caught.speciesId)
                    newSave = if (newSave.party.size < 6) {
                        newSave.copy(party = newSave.party + caught)
                    } else {
                        newSave.copy(pc = newSave.pc.store(caught))
                    }
                    _uiState.update { it.copy(caughtCreature = caught) }
                }
                BattleOutcome.PLAYER_LOSE -> {
                    // Heal party to half HP, deduct money
                    val penalty = (newSave.money * 0.1).toInt()
                    val healedParty = newSave.party.map { c ->
                        c.copy(currentHp = c.maxHp / 2, statusCondition = StatusCondition.NONE)
                    }
                    newSave = newSave.copy(money = maxOf(0, newSave.money - penalty), party = healedParty)
                }
                else -> {}
            }

            gameViewModel.updateSave(newSave)
        }
    }
}
