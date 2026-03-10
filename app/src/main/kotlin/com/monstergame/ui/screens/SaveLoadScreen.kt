package com.monstergame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.monstergame.engine.persistence.SaveSlotInfo
import com.monstergame.ui.theme.*
import com.monstergame.viewmodel.GameViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveLoadScreen(
    onBack: () -> Unit,
    vm: GameViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SAVE / LOAD", color = AccentGold, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TextPrimary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            state.message?.let { msg ->
                Text(msg, color = AccentGreen, fontSize = 13.sp)
                LaunchedEffect(msg) { kotlinx.coroutines.delay(2000); vm.clearMessage() }
            }

            state.saveSlots.forEach { slot ->
                SaveSlotCard(
                    slot = slot,
                    canSave = state.saveData != null,
                    onSave = { vm.saveGame(slot.slot) },
                    onLoad = { vm.loadGame(slot.slot) }
                )
            }
        }
    }
}

@Composable
private fun SaveSlotCard(
    slot: SaveSlotInfo,
    canSave: Boolean,
    onSave: () -> Unit,
    onLoad: () -> Unit
) {
    Surface(
        color = DarkCard,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Slot ${slot.slot + 1}", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                if (slot.exists && !slot.corrupt) {
                    Text(slot.playerName, color = TextPrimary, fontSize = 13.sp)
                    Text(slot.activeRegion, color = AccentBlue, fontSize = 11.sp)
                    Text("Badges: ${slot.badges}  •  ${formatTime(slot.playtimeSeconds)}",
                        color = TextSecondary, fontSize = 10.sp)
                    if (slot.lastModified > 0) {
                        val fmt = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
                        Text(fmt.format(Date(slot.lastModified)), color = TextSecondary, fontSize = 10.sp)
                    }
                } else if (slot.corrupt) {
                    Text("CORRUPTED", color = AccentRed, fontSize = 12.sp)
                } else {
                    Text("— Empty —", color = TextSecondary, fontSize = 12.sp)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (canSave) {
                    Button(
                        onClick = onSave,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = DarkBackground)
                    ) { Text("SAVE", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                }
                if (slot.exists && !slot.corrupt) {
                    OutlinedButton(
                        onClick = onLoad,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) { Text("LOAD", fontSize = 12.sp) }
                }
            }
        }
    }
}

private fun formatTime(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    return "${h}h ${m}m"
}
