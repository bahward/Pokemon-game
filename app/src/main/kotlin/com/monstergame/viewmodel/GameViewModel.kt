package com.monstergame.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monstergame.content.loader.ContentRepository
import com.monstergame.engine.model.*
import com.monstergame.engine.overworld.*
import com.monstergame.engine.persistence.SaveManager
import com.monstergame.engine.persistence.SaveSlotInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GameUiState(
    val saveData: SaveData? = null,
    val isLoading: Boolean = false,
    val saveSlots: List<SaveSlotInfo> = emptyList(),
    val message: String? = null,
    val pendingBattleId: String? = null,
    val pendingDialogueNpcId: String? = null
)

@HiltViewModel
class GameViewModel @Inject constructor(
    val repo: ContentRepository,
    private val saveManager: SaveManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    val save: SaveData? get() = _uiState.value.saveData

    init {
        refreshSlots()
    }

    fun refreshSlots() {
        _uiState.update { it.copy(saveSlots = saveManager.listSlots()) }
    }

    fun startNewGame(playerName: String) {
        val kanto = repo.getRegion("KANTO") ?: return
        val startPos = Position(kanto.startAreaId, 5, 8)
        val newSave = SaveData(
            playerName = playerName,
            money = 3000,
            position = startPos,
            activeRegion = "KANTO",
            bag = buildStarterBag()
        )
        _uiState.update { it.copy(saveData = newSave) }
    }

    private fun buildStarterBag(): Bag {
        val bag = Bag()
        // 5 Basic Balls, 1 Potion
        bag.add(301, 5)  // Basic Ball
        bag.add(101, 1)  // Potion
        return bag
    }

    fun giveStarterPair(first: Int, second: Int) {
        val save = _uiState.value.saveData ?: return
        val sp1 = repo.getCreature(first) ?: return
        val sp2 = repo.getCreature(second) ?: return
        val c1 = buildStarterInstance(sp1, 5)
        val c2 = buildStarterInstance(sp2, 5)
        _uiState.update { it.copy(saveData = save.copy(party = listOf(c1, c2))) }
    }

    private fun buildStarterInstance(species: CreatureSpecies, level: Int): CreatureInstance {
        val instance = CreatureFactory.createFromSpecies(species, level)
        // Fill move PP from repo
        val filledMoves = instance.moves.map { slot ->
            val move = repo.getMove(slot.moveId) ?: return@map slot
            slot.copy(currentPp = move.pp, maxPp = move.pp)
        }
        return instance.copy(moves = filledMoves)
    }

    fun saveGame(slot: Int) {
        val data = _uiState.value.saveData ?: return
        viewModelScope.launch {
            val result = saveManager.save(data, slot)
            _uiState.update {
                it.copy(message = if (result.isSuccess) "Game saved!" else "Save failed.")
            }
            refreshSlots()
        }
    }

    fun loadGame(slot: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = saveManager.load(slot)
            _uiState.update {
                if (result.isSuccess) it.copy(saveData = result.getOrNull(), isLoading = false)
                else it.copy(isLoading = false, message = "Load failed.")
            }
        }
    }

    fun updateSave(newSave: SaveData) {
        _uiState.update { it.copy(saveData = newSave) }
    }

    fun addToParty(creature: CreatureInstance) {
        val save = _uiState.value.saveData ?: return
        if (save.party.size < 6) {
            updateSave(save.copy(party = save.party + creature))
        } else {
            // Send to PC
            updateSave(save.copy(pc = save.pc.store(creature)))
        }
    }

    fun healParty() {
        val save = _uiState.value.saveData ?: return
        val healed = save.party.map { CreatureFactory.fullHeal(it) }
        updateSave(save.copy(party = healed))
    }

    fun spendMoney(amount: Int): Boolean {
        val save = _uiState.value.saveData ?: return false
        if (save.money < amount) return false
        updateSave(save.copy(money = save.money - amount))
        return true
    }

    fun earnMoney(amount: Int) {
        val save = _uiState.value.saveData ?: return
        updateSave(save.copy(money = save.money + amount))
    }

    fun addItemToParty(itemId: Int, qty: Int = 1) {
        val save = _uiState.value.saveData ?: return
        val newBag = save.bag.also { it.add(itemId, qty) }
        updateSave(save.copy(bag = newBag))
    }

    fun useItem(itemId: Int, partyIndex: Int): Boolean {
        val save = _uiState.value.saveData ?: return false
        val target = save.party.getOrNull(partyIndex) ?: return false
        val item = repo.getItem(itemId) ?: return false
        if (!save.bag.has(itemId)) return false

        val newTarget = when {
            item.curesAll   -> CreatureFactory.fullHeal(target)
            item.healAmount > 0 -> CreatureFactory.heal(target, item.healAmount)
            item.revives && target.statusCondition == StatusCondition.FAINT -> {
                val hp = if (item.reviveFull) target.maxHp else target.maxHp / 2
                target.copy(currentHp = hp, statusCondition = StatusCondition.NONE)
            }
            item.curesStatus -> target.copy(statusCondition = StatusCondition.NONE, statusTurns = 0)
            else -> return false
        }

        val newParty = save.party.toMutableList()
        newParty[partyIndex] = newTarget
        val newBag = save.bag
        newBag.remove(itemId)
        updateSave(save.copy(party = newParty, bag = newBag))
        return true
    }

    fun setFlag(flag: String, value: Boolean = true) {
        val save = _uiState.value.saveData ?: return
        updateSave(save.withFlag(flag, value))
    }

    fun awardBadge(badgeId: String) {
        val save = _uiState.value.saveData ?: return
        updateSave(save.withBadge(badgeId))
    }

    fun markTrainerDefeated(trainerId: String) {
        val save = _uiState.value.saveData ?: return
        updateSave(save.withDefeated(trainerId))
    }

    fun recordSeen(speciesId: Int) {
        val save = _uiState.value.saveData ?: return
        if (speciesId !in save.seenCreatures) {
            updateSave(save.copy(seenCreatures = save.seenCreatures + speciesId))
        }
    }

    fun recordCaught(speciesId: Int) {
        val save = _uiState.value.saveData ?: return
        updateSave(save.copy(
            seenCreatures  = save.seenCreatures + speciesId,
            caughtCreatures = save.caughtCreatures + speciesId
        ))
    }

    fun tickPlaytime(seconds: Long) {
        val save = _uiState.value.saveData ?: return
        updateSave(save.copy(playtimeSeconds = save.playtimeSeconds + seconds))
    }

    fun clearMessage() = _uiState.update { it.copy(message = null) }

    fun getOverworldEngine(): OverworldEngine {
        return OverworldEngine(
            areaRepo    = repo.areas,
            tileMapRepo = emptyMap(), // tile maps loaded separately
            speciesRepo = repo.creatures,
            npcRepo     = repo.npcs,
            eventRepo   = repo.events
        )
    }
}
