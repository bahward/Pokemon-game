package com.monstergame.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monstergame.engine.model.*
import com.monstergame.engine.overworld.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OverworldUiState(
    val position: Position = Position("kanto_starttown", 5, 8),
    val direction: Direction = Direction.DOWN,
    val isMoving: Boolean = false,
    val currentArea: AreaData? = null,
    val pendingEncounter: CreatureInstance? = null,
    val pendingTrainerId: String? = null,
    val pendingDialogue: List<String> = emptyList(),
    val pendingEvent: GameEvent? = null,
    val showHealingMenu: Boolean = false,
    val showPCMenu: Boolean = false,
    val nearbyShopId: String? = null,
    val dialogueSpeaker: String = ""
)

@HiltViewModel
class OverworldViewModel @Inject constructor(
    private val gameViewModel: GameViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(OverworldUiState())
    val uiState: StateFlow<OverworldUiState> = _uiState.asStateFlow()

    private val overworldEngine: OverworldEngine get() = gameViewModel.getOverworldEngine()

    init {
        viewModelScope.launch {
            gameViewModel.uiState.collect { gameState ->
                gameState.saveData?.let { save ->
                    val area = gameViewModel.repo.getArea(save.position.areaId)
                    _uiState.update { it.copy(position = save.position, currentArea = area) }
                }
            }
        }
    }

    fun move(direction: Direction) {
        val save = gameViewModel.save ?: return
        if (_uiState.value.isMoving) return

        _uiState.update { it.copy(isMoving = true, direction = direction) }

        viewModelScope.launch {
            delay(120) // movement animation duration

            val results = overworldEngine.move(save.position, direction, save)

            for (result in results) {
                when (result) {
                    is OverworldEngine.OverworldResult.Moved -> {
                        gameViewModel.updateSave(save.copy(position = result.newPos))
                        _uiState.update { it.copy(position = result.newPos) }
                    }
                    is OverworldEngine.OverworldResult.WildEncounter -> {
                        _uiState.update { it.copy(pendingEncounter = result.creature) }
                        return@launch
                    }
                    is OverworldEngine.OverworldResult.WarpTriggered -> {
                        val newPos = Position(result.warp.toArea, result.warp.toX, result.warp.toY)
                        gameViewModel.updateSave(save.copy(position = newPos))
                        val newArea = gameViewModel.repo.getArea(newPos.areaId)
                        _uiState.update { it.copy(position = newPos, currentArea = newArea) }
                        handleAreaEnter(newPos.areaId)
                    }
                    is OverworldEngine.OverworldResult.EventTriggered -> {
                        processEvent(result.event)
                    }
                    is OverworldEngine.OverworldResult.Blocked -> { /* no-op */ }
                    else -> {}
                }
            }

            _uiState.update { it.copy(isMoving = false) }
        }
    }

    fun interact() {
        val save = gameViewModel.save ?: return
        val result = overworldEngine.interact(save.position, _uiState.value.direction, save)
        if (result != null) {
            viewModelScope.launch { handleInteractResult(result) }
        }
    }

    private fun handleInteractResult(result: OverworldEngine.OverworldResult) {
        when (result) {
            is OverworldEngine.OverworldResult.NpcInteract -> {
                val npc = result.npc
                if (npc.trainerId != null) {
                    val save = gameViewModel.save ?: return
                    if (!save.hasDefeated(npc.trainerId)) {
                        _uiState.update { it.copy(pendingTrainerId = npc.trainerId) }
                        return
                    }
                }
                _uiState.update {
                    it.copy(pendingDialogue = npc.dialogues, dialogueSpeaker = npc.name)
                }
            }
            is OverworldEngine.OverworldResult.EventTriggered -> processEvent(result.event)
            else -> {}
        }
    }

    private fun processEvent(event: GameEvent) {
        var save = gameViewModel.save ?: return
        for (action in event.actions) {
            when (action.type) {
                EventActionType.DIALOGUE -> {
                    val npc = action.npcId?.let { gameViewModel.repo.getNpc(it) }
                    _uiState.update {
                        it.copy(
                            pendingDialogue = npc?.dialogues ?: listOf(action.text ?: ""),
                            dialogueSpeaker = npc?.name ?: ""
                        )
                    }
                }
                EventActionType.BATTLE -> {
                    action.trainerId?.let { tid ->
                        _uiState.update { it.copy(pendingTrainerId = tid) }
                    }
                }
                EventActionType.SET_FLAG -> {
                    action.flag?.let { flag ->
                        save = save.withFlag(flag, action.flagValue ?: true)
                    }
                }
                EventActionType.GIVE_ITEM -> {
                    action.itemId?.let { itemId ->
                        save.bag.add(itemId, action.qty)
                    }
                }
                EventActionType.HEAL_PARTY -> gameViewModel.healParty()
                EventActionType.WARP -> {
                    val warpArea = action.warpAreaId
                    val warpX = action.warpX ?: 5
                    val warpY = action.warpY ?: 8
                    if (warpArea != null) {
                        val newPos = Position(warpArea, warpX, warpY)
                        save = save.copy(position = newPos)
                        val newArea = gameViewModel.repo.getArea(warpArea)
                        _uiState.update { it.copy(position = newPos, currentArea = newArea) }
                    }
                }
                else -> {}
            }
        }
        gameViewModel.updateSave(EventSystem.markDone(event, save))
    }

    private fun handleAreaEnter(areaId: String) {
        val save = gameViewModel.save ?: return
        val area = gameViewModel.repo.getArea(areaId)
        _uiState.update { it.copy(currentArea = area) }

        if (area?.healingCenter == true) {
            _uiState.update { it.copy(showHealingMenu = true) }
        }
        if (area?.pc == true) {
            _uiState.update { it.copy(showPCMenu = true) }
        }

        val events = overworldEngine.onAreaEnter(areaId, save)
        events.firstOrNull()?.let { processEvent(it) }
    }

    fun clearPendingEncounter() = _uiState.update { it.copy(pendingEncounter = null) }
    fun clearPendingTrainer()   = _uiState.update { it.copy(pendingTrainerId = null) }
    fun clearDialogue()         = _uiState.update { it.copy(pendingDialogue = emptyList()) }
    fun clearHealingMenu()      = _uiState.update { it.copy(showHealingMenu = false) }
    fun clearPCMenu()           = _uiState.update { it.copy(showPCMenu = false) }

    fun healAtCenter() {
        gameViewModel.healParty()
        clearHealingMenu()
    }
}
