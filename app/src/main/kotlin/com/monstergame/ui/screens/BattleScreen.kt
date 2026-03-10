package com.monstergame.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.monstergame.engine.model.*
import com.monstergame.ui.components.*
import com.monstergame.ui.theme.*
import com.monstergame.viewmodel.BattleViewModel
import com.monstergame.viewmodel.GameViewModel

enum class BattleMenuTab { FIGHT, BAG, PARTY, RUN }

@Composable
fun BattleScreen(
    battleId: String,
    onBattleEnd: () -> Unit,
    battleVm: BattleViewModel = hiltViewModel(),
    gameVm: GameViewModel = hiltViewModel()
) {
    val uiState by battleVm.uiState.collectAsState()
    var menuTab by remember { mutableStateOf(BattleMenuTab.FIGHT) }

    // Init battle from battleId
    LaunchedEffect(battleId) {
        val save = gameVm.save ?: return@LaunchedEffect
        if (battleId.startsWith("trainer_")) {
            val trainerId = battleId.removePrefix("trainer_")
            battleVm.startTrainerBattle(trainerId)
        } else if (battleId.startsWith("wild_")) {
            // Wild: "wild_{speciesId}_{level}"
            val parts = battleId.split("_")
            val speciesId = parts.getOrNull(1)?.toIntOrNull() ?: 1
            val level     = parts.getOrNull(2)?.toIntOrNull() ?: 5
            val species = gameVm.repo.getCreature(speciesId) ?: return@LaunchedEffect
            val creature = CreatureFactory.createFromSpecies(species, level)
            battleVm.startWildBattle(creature)
        }
    }

    // Battle end handling
    LaunchedEffect(uiState.outcome) {
        when (uiState.outcome) {
            BattleOutcome.PLAYER_WIN, BattleOutcome.RAN_AWAY,
            BattleOutcome.PLAYER_LOSE, BattleOutcome.CAUGHT -> {
                kotlinx.coroutines.delay(1800)
                onBattleEnd()
            }
            else -> {}
        }
    }

    val battleState = uiState.battleState

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF1A2A1A))
    ) {
        if (battleState == null) {
            CircularProgressIndicator(
                color = AccentGold,
                modifier = Modifier.align(Alignment.Center)
            )
            return@Box
    }

        Column(modifier = Modifier.fillMaxSize()) {
            // Opponent info bar
            BattleInfoBar(
                creature = battleState.opponent.active,
                species  = gameVm.repo.getCreature(battleState.opponent.active.speciesId),
                isOpponent = true,
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            )

            // Battle scene
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF2D4A2D))
            ) {
                // Opponent sprite
                val oppSpecies = gameVm.repo.getCreature(battleState.opponent.active.speciesId)
                CreatureSprite(
                    creature    = battleState.opponent.active,
                    primaryType = oppSpecies?.types?.firstOrNull() ?: ElementType.NEUTRAL,
                    size = 96.dp,
                    isOpponent = true,
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 8.dp, end = 24.dp)
                )

                // Player sprite
                val playerSpecies = gameVm.repo.getCreature(battleState.player.active.speciesId)
                CreatureSprite(
                    creature    = battleState.player.active,
                    primaryType = playerSpecies?.types?.firstOrNull() ?: ElementType.NEUTRAL,
                    size = 112.dp,
                    isOpponent = false,
                    modifier = Modifier.align(Alignment.BottomStart).padding(bottom = 8.dp, start = 24.dp)
                )

                // Weather indicator
                if (battleState.weather != WeatherEffect.CLEAR) {
                    Surface(
                        color = DarkCard.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
                    ) {
                        Text(
                            battleState.weather.name,
                            color = AccentBlue,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }

            // Player info bar
            BattleInfoBar(
                creature = battleState.player.active,
                species  = gameVm.repo.getCreature(battleState.player.active.speciesId),
                isOpponent = false,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
            )

            // Message log
            BattleMessageBox(
                messages = uiState.displayedMessages,
                modifier = Modifier.fillMaxWidth().height(70.dp).padding(horizontal = 12.dp)
            )

            Spacer(Modifier.height(4.dp))

            // Battle menu
            if (uiState.outcome == BattleOutcome.ONGOING && uiState.isPlayerTurn && !uiState.isAnimating) {
                BattleMenu(
                    state    = battleState,
                    menuTab  = menuTab,
                    onTabChange = { menuTab = it },
                    onAction = { battleVm.playerAction(it) },
                    gameVm   = gameVm,
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                )
            } else if (uiState.outcome != BattleOutcome.ONGOING) {
                BattleOutcomeBar(uiState.outcome, uiState.newBadge, uiState.expGains,
                    modifier = Modifier.fillMaxWidth().padding(8.dp))
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentGold, strokeWidth = 2.dp)
                }
            }
        }
    }
}

@Composable
private fun BattleInfoBar(
    creature: CreatureInstance,
    species: CreatureSpecies?,
    isOpponent: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DarkCard.copy(alpha = 0.92f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(creature.nickname, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    species?.types?.forEach { TypeChip(it) }
                }
                Text("Lv.${creature.level}", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            HPBar(creature = creature, showNumbers = !isOpponent,
                modifier = Modifier.fillMaxWidth())
            if (creature.statusCondition != StatusCondition.NONE) {
                Text(creature.statusCondition.name, color = statusColor(creature.statusCondition), fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun BattleMessageBox(messages: List<String>, modifier: Modifier = Modifier) {
    Surface(color = DarkBackground, shape = RoundedCornerShape(8.dp), modifier = modifier) {
        LazyColumn(
            modifier = Modifier.padding(8.dp),
            reverseLayout = true
        ) {
            items(messages.reversed()) { msg ->
                Text(msg, color = TextPrimary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun BattleMenu(
    state: BattleState,
    menuTab: BattleMenuTab,
    onTabChange: (BattleMenuTab) -> Unit,
    onAction: (BattleAction) -> Unit,
    gameVm: GameViewModel,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Tab row
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            BattleMenuTab.entries.forEach { tab ->
                val isSelected = menuTab == tab
                val canRun = tab != BattleMenuTab.RUN || state.canRun
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) AccentBlue else DarkCard)
                        .border(1.dp, if (isSelected) AccentBlue else Color.Transparent, RoundedCornerShape(6.dp))
                        .then(if (canRun) Modifier.clickable { onTabChange(tab) } else Modifier),
                    contentAlignment = Alignment.Center
                ) {
                    Text(tab.name, color = if (isSelected) Color.Black else if (canRun) TextPrimary else TextSecondary,
                        fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        when (menuTab) {
            BattleMenuTab.FIGHT -> MoveGrid(
                creature = state.player.active,
                gameVm = gameVm,
                onMove = { idx -> onAction(BattleAction.UseMove(idx)) }
            )
            BattleMenuTab.BAG -> BagQuickMenu(
                gameVm = gameVm,
                onUseItem = { itemId -> onAction(BattleAction.ThrowBall(itemId)) },
                isWild = state.battleType == BattleType.WILD
            )
            BattleMenuTab.PARTY -> PartyQuickMenu(
                state = state,
                gameVm = gameVm,
                onSwitch = { idx -> onAction(BattleAction.Switch(idx)) }
            )
            BattleMenuTab.RUN -> {
                if (state.canRun) {
                    Button(
                        onClick = { onAction(BattleAction.Run) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                    ) { Text("RUN AWAY!", fontWeight = FontWeight.ExtraBold, color = Color.White) }
                } else {
                    Text("Can't escape from a trainer battle!", color = AccentRed,
                        modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}

@Composable
private fun MoveGrid(creature: CreatureInstance, gameVm: GameViewModel, onMove: (Int) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth().height(90.dp)
    ) {
        creature.moves.forEachIndexed { idx, slot ->
            val move = gameVm.repo.getMove(slot.moveId)
            val hasPP = slot.currentPp > 0
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (move != null) typeColor(move.type).copy(alpha = 0.3f)
                        else DarkCard
                    )
                    .border(
                        1.dp,
                        if (hasPP && move != null) typeColor(move.type).copy(alpha = 0.7f) else TextSecondary,
                        RoundedCornerShape(8.dp)
                    )
                    .then(if (hasPP && move != null) Modifier.clickable { onMove(idx) } else Modifier),
                contentAlignment = Alignment.Center
            ) {
                if (move != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(move.name, color = if (hasPP) TextPrimary else TextSecondary,
                            fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        TypeChip(move.type)
                        Text("${slot.currentPp}/${slot.maxPp} PP",
                            color = if (slot.currentPp <= 1) AccentRed else TextSecondary,
                            fontSize = 9.sp)
                    }
                } else {
                    Text("—", color = TextSecondary, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun BagQuickMenu(gameVm: GameViewModel, onUseItem: (Int) -> Unit, isWild: Boolean) {
    val save = gameVm.uiState.collectAsState().value.saveData
    val bagItems = save?.bag?.items ?: emptyMap()

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth().height(80.dp)
    ) {
        bagItems.filter { (id, qty) -> qty > 0 }.take(6).forEach { (itemId, qty) ->
            val item = gameVm.repo.getItem(itemId) ?: return@forEach
            val canUse = item.ballType != null && isWild || item.healAmount > 0 || item.curesAll
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (canUse) DarkCard else DarkSurface)
                    .border(1.dp, if (canUse) AccentBlue.copy(0.5f) else Color.Transparent, RoundedCornerShape(8.dp))
                    .then(if (canUse) Modifier.clickable { onUseItem(itemId) } else Modifier),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(item.name, color = if (canUse) TextPrimary else TextSecondary, fontSize = 10.sp)
                    Text("×$qty", color = TextSecondary, fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
private fun PartyQuickMenu(
    state: BattleState,
    gameVm: GameViewModel,
    onSwitch: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth().height(80.dp)
    ) {
        state.player.creatures.forEachIndexed { idx, c ->
            val isActive = idx == state.player.activeIndex
            val canSwitch = !isActive && c.isAlive
            val species = gameVm.repo.getCreature(c.speciesId)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(when { isActive -> AccentBlue.copy(0.2f); c.isAlive -> DarkCard; else -> DarkSurface })
                    .border(1.dp, when { isActive -> AccentBlue; !c.isAlive -> TextSecondary; else -> Color.Transparent }, RoundedCornerShape(8.dp))
                    .then(if (canSwitch) Modifier.clickable { onSwitch(idx) } else Modifier),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(c.nickname, color = if (c.isAlive) TextPrimary else TextSecondary, fontSize = 10.sp)
                    Text("Lv${c.level}", color = AccentGold, fontSize = 9.sp)
                    Box(modifier = Modifier.fillMaxWidth(0.8f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF3A3A3A))) {
                        Box(modifier = Modifier.fillMaxWidth(c.hpPercent).fillMaxHeight()
                            .clip(RoundedCornerShape(2.dp))
                            .background(when { c.hpPercent > 0.5f -> HpHigh; c.hpPercent > 0.2f -> HpMid; else -> HpLow }))
                    }
                }
            }
        }
    }
}

@Composable
private fun BattleOutcomeBar(
    outcome: BattleOutcome,
    newBadge: String?,
    expGains: List<Pair<String, Long>>,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DarkCard,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.height(100.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                when (outcome) {
                    BattleOutcome.PLAYER_WIN -> "Victory!"
                    BattleOutcome.PLAYER_LOSE -> "You blacked out…"
                    BattleOutcome.CAUGHT -> "Caught!"
                    BattleOutcome.RAN_AWAY -> "Got away safely!"
                    else -> ""
                },
                color = when (outcome) {
                    BattleOutcome.PLAYER_WIN, BattleOutcome.CAUGHT -> AccentGold
                    BattleOutcome.PLAYER_LOSE -> AccentRed
                    else -> TextPrimary
                },
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            )
            newBadge?.let {
                Text("Earned badge: $it", color = AccentGold, fontSize = 12.sp)
            }
            expGains.forEach { (name, exp) ->
                Text("$name gained $exp EXP!", color = AccentGreen, fontSize = 11.sp)
            }
        }
    }
}

private fun statusColor(status: StatusCondition): Color = when (status) {
    StatusCondition.BURN    -> Color(0xFFFF7043)
    StatusCondition.POISON, StatusCondition.BAD_POISON -> Color(0xFFAB47BC)
    StatusCondition.PARALYZE -> Color(0xFFFFEE58)
    StatusCondition.SLEEP   -> Color(0xFF78909C)
    StatusCondition.FREEZE  -> Color(0xFF4FC3F7)
    StatusCondition.FAINT   -> AccentRed
    else -> TextSecondary
}
