package com.monstergame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.monstergame.engine.model.Direction
import com.monstergame.ui.components.MapRenderer
import com.monstergame.ui.components.generatePlaceholderMap
import com.monstergame.ui.theme.*
import com.monstergame.viewmodel.GameViewModel
import com.monstergame.viewmodel.OverworldViewModel

@Composable
fun OverworldScreen(
    onBattle: (String) -> Unit,
    onOpenParty: () -> Unit,
    onOpenBag: () -> Unit,
    onOpenPC: () -> Unit,
    onOpenShop: (String) -> Unit,
    onDialogue: (String) -> Unit,
    gameVm: GameViewModel  = hiltViewModel(),
    vm: OverworldViewModel = hiltViewModel()
) {
    val uiState    by vm.uiState.collectAsState()
    val gameState  by gameVm.uiState.collectAsState()
    val save = gameState.saveData

    // Handle pending encounters / trainers
    LaunchedEffect(uiState.pendingEncounter) {
        uiState.pendingEncounter?.let { creature ->
            onBattle("wild_${creature.speciesId}_${creature.level}")
            vm.clearPendingEncounter()
        }
    }
    LaunchedEffect(uiState.pendingTrainerId) {
        uiState.pendingTrainerId?.let { id ->
            onBattle("trainer_$id")
            vm.clearPendingTrainer()
        }
    }

    // Dialogue overlay
    if (uiState.pendingDialogue.isNotEmpty()) {
        DialogueOverlay(
            lines = uiState.pendingDialogue,
            speaker = uiState.dialogueSpeaker,
            onDone = { vm.clearDialogue() }
        )
        return
    }

    // Healing centre menu
    if (uiState.showHealingMenu) {
        HealingCentreDialog(
            onHeal = {
                vm.healAtCenter()
                gameVm.saveGame(0)
            },
            onDismiss = { vm.clearHealingMenu() }
        )
    }

    // Get or generate tile map
    val tileMap = remember(uiState.position.areaId) {
        generatePlaceholderMap(uiState.position.areaId)
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        // Map
        MapRenderer(
            tileMap = tileMap,
            playerPos = uiState.position,
            playerDirection = uiState.direction,
            modifier = Modifier.fillMaxSize()
        )

        // HUD overlay — top bar
        TopHUD(
            playerName  = save?.playerName ?: "",
            money       = save?.money ?: 0,
            areaName    = uiState.currentArea?.name ?: uiState.position.areaId,
            badgeCount  = save?.badges?.size ?: 0,
            modifier    = Modifier.align(Alignment.TopCenter).fillMaxWidth()
        )

        // Menu buttons — top right
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 56.dp, end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SmallMenuButton(Icons.Default.Pets, "Party", onOpenParty)
            SmallMenuButton(Icons.Default.Backpack, "Bag", onOpenBag)
            save?.let { s ->
                if (s.hasFlag("pc_unlocked") || s.position.areaId.contains("city") || s.position.areaId.contains("town"))
                    SmallMenuButton(Icons.Default.Storage, "PC", onOpenPC)
            }
        }

        // D-Pad — bottom left
        DPad(
            onMove = { dir -> vm.move(dir) },
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
        )

        // Action button — bottom right
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            ActionButton(onPress = { vm.interact() })
        }

        // Auto-save indicator
        if (gameState.message != null) {
            Surface(
                color = DarkCard.copy(alpha = 0.9f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 72.dp)
            ) {
                Text(
                    gameState.message!!,
                    color = AccentGreen,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }
            LaunchedEffect(gameState.message) {
                kotlinx.coroutines.delay(2000)
                gameVm.clearMessage()
            }
        }
    }
}

@Composable
private fun TopHUD(
    playerName: String,
    money: Int,
    areaName: String,
    badgeCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DarkBackground.copy(alpha = 0.85f),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(areaName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(playerName, color = TextSecondary, fontSize = 10.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("⬡ $badgeCount", color = AccentGold, fontSize = 12.sp)
                Text("${money}G", color = AccentGold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun SmallMenuButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        color = DarkCard.copy(alpha = 0.9f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.clickable(onClick = onClick).size(40.dp)
    ) {
        Icon(icon, contentDescription = label,
            tint = TextPrimary, modifier = Modifier.padding(8.dp))
    }
}

@Composable
private fun DPad(onMove: (Direction) -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(120.dp)) {
        val btnSize = 38.dp
        val btnMod = Modifier.size(btnSize)
            .clip(RoundedCornerShape(6.dp))
            .background(DarkCard.copy(alpha = 0.85f))
            .border(1.dp, TextSecondary.copy(alpha = 0.3f), RoundedCornerShape(6.dp))

        // Up
        Box(
            modifier = btnMod
                .align(Alignment.TopCenter)
                .clickable { onMove(Direction.UP) },
            contentAlignment = Alignment.Center
        ) { Text("▲", color = TextPrimary, fontSize = 14.sp) }

        // Down
        Box(
            modifier = btnMod
                .align(Alignment.BottomCenter)
                .clickable { onMove(Direction.DOWN) },
            contentAlignment = Alignment.Center
        ) { Text("▼", color = TextPrimary, fontSize = 14.sp) }

        // Left
        Box(
            modifier = btnMod
                .align(Alignment.CenterStart)
                .clickable { onMove(Direction.LEFT) },
            contentAlignment = Alignment.Center
        ) { Text("◀", color = TextPrimary, fontSize = 14.sp) }

        // Right
        Box(
            modifier = btnMod
                .align(Alignment.CenterEnd)
                .clickable { onMove(Direction.RIGHT) },
            contentAlignment = Alignment.Center
        ) { Text("▶", color = TextPrimary, fontSize = 14.sp) }

        // Center
        Box(
            modifier = Modifier
                .size(30.dp)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(4.dp))
                .background(DarkCard.copy(alpha = 0.6f))
        )
    }
}

@Composable
private fun ActionButton(onPress: () -> Unit) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(AccentBlue.copy(alpha = 0.85f))
            .border(2.dp, AccentBlue, CircleShape)
            .clickable(onClick = onPress),
        contentAlignment = Alignment.Center
    ) {
        Text("A", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
    }
}

@Composable
fun DialogueOverlay(lines: List<String>, speaker: String, onDone: () -> Unit) {
    var lineIndex by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f))
    ) {
        Surface(
            color = DarkCard,
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clickable {
                    if (lineIndex < lines.size - 1) lineIndex++
                    else onDone()
                }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (speaker.isNotEmpty()) {
                    Text(speaker, color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(Modifier.height(4.dp))
                }
                Text(lines.getOrElse(lineIndex) { "" }, color = TextPrimary, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    if (lineIndex < lines.size - 1) "Tap to continue…" else "Tap to close",
                    color = TextSecondary, fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun HealingCentreDialog(onHeal: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Healing Centre", color = AccentGold) },
        text = { Text("Would you like us to restore your companions to full health?", color = TextPrimary) },
        confirmButton = {
            TextButton(onClick = onHeal) { Text("YES PLEASE!", color = AccentGreen) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("No thanks", color = TextSecondary) }
        },
        containerColor = DarkCard
    )
}
