package com.monstergame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.monstergame.ui.theme.*
import com.monstergame.viewmodel.GameViewModel

@Composable
fun DialogueScreen(
    npcId: String,
    onDone: () -> Unit,
    vm: GameViewModel = hiltViewModel()
) {
    val npc = vm.repo.getNpc(npcId)
    val lines = npc?.dialogues ?: listOf("…")

    DialogueOverlay(lines = lines, speaker = npc?.name ?: "", onDone = onDone)
}
