package com.monstergame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.monstergame.ui.components.*
import com.monstergame.ui.theme.*
import com.monstergame.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyScreen(
    onBack: () -> Unit,
    vm: GameViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    val party = state.saveData?.party ?: emptyList()
    var selected by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PARTY", color = AccentGold, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)
        ) {
            itemsIndexed(party) { idx, creature ->
                PartyCard(
                    creature = creature,
                    species = vm.repo.getCreature(creature.speciesId),
                    isSelected = selected == idx,
                    onClick = { selected = if (selected == idx) null else idx },
                    vm = vm
                )
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun PartyCard(
    creature: CreatureInstance,
    species: com.monstergame.engine.model.CreatureSpecies?,
    isSelected: Boolean,
    onClick: () -> Unit,
    vm: GameViewModel
) {
    Surface(
        color = if (isSelected) DarkCard else DarkSurface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CreatureSprite(
                creature = creature,
                primaryType = species?.types?.firstOrNull()
                    ?: com.monstergame.engine.model.ElementType.NEUTRAL,
                size = 56.dp
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(creature.nickname, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Lv.${creature.level}", color = AccentGold, fontSize = 12.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    species?.types?.forEach { TypeChip(it) }
                }
                Spacer(Modifier.height(4.dp))
                HPBar(creature = creature, showNumbers = true, modifier = Modifier.fillMaxWidth())

                if (isSelected) {
                    Spacer(Modifier.height(8.dp))
                    // Moves list
                    Text("Moves:", color = TextSecondary, fontSize = 11.sp)
                    creature.moves.forEach { slot ->
                        val move = vm.repo.getMove(slot.moveId)
                        if (move != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    TypeChip(move.type)
                                    Text(move.name, color = TextPrimary, fontSize = 11.sp)
                                }
                                Text("${slot.currentPp}/${slot.maxPp}",
                                    color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    // Stats
                    val stats = creature.calculatedStats
                    Text("HP:${creature.maxHp}  ATK:${stats.attack}  DEF:${stats.defense}",
                        color = TextSecondary, fontSize = 10.sp)
                    Text("SPATK:${stats.specialAttack}  SPDEF:${stats.specialDefense}  SPD:${stats.speed}",
                        color = TextSecondary, fontSize = 10.sp)
                }
            }
        }
    }
}
