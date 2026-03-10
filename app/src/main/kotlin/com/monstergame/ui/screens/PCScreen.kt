package com.monstergame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.monstergame.engine.model.CreatureInstance
import com.monstergame.engine.model.ElementType
import com.monstergame.ui.components.CreatureSprite
import com.monstergame.ui.components.HPBar
import com.monstergame.ui.components.TypeChip
import com.monstergame.ui.theme.*
import com.monstergame.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PCScreen(
    onBack: () -> Unit,
    vm: GameViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    val save = state.saveData
    var currentBox by remember { mutableStateOf(0) }
    var selectedCreature by remember { mutableStateOf<CreatureInstance?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PC STORAGE", color = AccentGold, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TextPrimary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(12.dp)) {
            // Box selector
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = { if (currentBox > 0) currentBox-- }) {
                    Text("◀", color = AccentBlue)
                }
                Text("Box ${currentBox + 1}: ${save?.pc?.boxes?.getOrNull(currentBox)?.name ?: ""}",
                    color = TextPrimary, fontWeight = FontWeight.Bold)
                TextButton(onClick = { if (currentBox < 29) currentBox++ }) {
                    Text("▶", color = AccentBlue)
                }
            }

            val box = save?.pc?.boxes?.getOrNull(currentBox)
            val boxCreatures = box?.creatures ?: emptyList()

            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(boxCreatures) { creature ->
                    val species = vm.repo.getCreature(creature.speciesId)
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkCard)
                            .clickable { selectedCreature = creature },
                        contentAlignment = Alignment.Center
                    ) {
                        CreatureSprite(
                            creature = creature,
                            primaryType = species?.types?.firstOrNull() ?: ElementType.NEUTRAL,
                            size = 44.dp
                        )
                    }
                }
                // Empty slots
                repeat(30 - boxCreatures.size) {
                    item {
                        Box(
                            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp))
                                .background(DarkSurface)
                        )
                    }
                }
            }

            // Selected creature details
            selectedCreature?.let { c ->
                val species = vm.repo.getCreature(c.speciesId)
                Spacer(Modifier.height(8.dp))
                Surface(color = DarkCard, shape = RoundedCornerShape(12.dp)) {
                    Row(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                        CreatureSprite(c, species?.types?.firstOrNull() ?: ElementType.NEUTRAL, 64.dp)
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(c.nickname, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Lv.${c.level}", color = AccentGold, fontSize = 12.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                species?.types?.forEach { TypeChip(it) }
                            }
                            HPBar(c, modifier = Modifier.fillMaxWidth())
                        }
                        // Withdraw button
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Button(
                                onClick = {
                                    val newSave = save ?: return@Button
                                    if (newSave.party.size < 6) {
                                        val (newPc, withdrawn) = newSave.pc.withdraw(c.uid)
                                        if (withdrawn != null) {
                                            vm.updateSave(newSave.copy(
                                                pc = newPc,
                                                party = newSave.party + withdrawn
                                            ))
                                            selectedCreature = null
                                        }
                                    }
                                },
                                enabled = (save?.party?.size ?: 6) < 6,
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) { Text("Take", fontSize = 11.sp) }
                        }
                    }
                }
            }
        }
    }
}
