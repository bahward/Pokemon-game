package com.monstergame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.monstergame.engine.model.CreatureSpecies
import com.monstergame.engine.model.ElementType
import com.monstergame.ui.components.CreatureSprite
import com.monstergame.ui.components.TypeChip
import com.monstergame.ui.components.typeColor
import com.monstergame.ui.theme.*
import com.monstergame.viewmodel.GameViewModel

@Composable
fun StarterSelectScreen(
    onConfirm: () -> Unit,
    vm: GameViewModel = hiltViewModel()
) {
    val starters = remember { vm.repo.starterPool() }
    val selected = remember { mutableStateListOf<Int>() }

    Box(
        modifier = Modifier.fillMaxSize().background(DarkBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Spacer(Modifier.height(16.dp))
            Text(
                "CHOOSE YOUR PARTNERS",
                style = MaterialTheme.typography.titleLarge,
                color = AccentGold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                "Select 2 companions to begin your journey",
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Region tabs hint
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                listOf("Verdania", "Solara", "Aquoria").forEach { region ->
                    Text(
                        region,
                        fontSize = 10.sp,
                        color = AccentBlue.copy(alpha = 0.7f),
                        modifier = Modifier
                            .border(1.dp, AccentBlue.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            Text(
                "Selected: ${selected.size}/2",
                color = if (selected.size == 2) AccentGreen else TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(starters, key = { it.id }) { species ->
                    StarterCard(
                        species = species,
                        isSelected = species.id in selected,
                        onClick = {
                            if (species.id in selected) {
                                selected.remove(species.id)
                            } else if (selected.size < 2) {
                                selected.add(species.id)
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    if (selected.size == 2) {
                        vm.giveStarterPair(selected[0], selected[1])
                        onConfirm()
                    }
                },
                enabled = selected.size == 2,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selected.size == 2) AccentGold else DarkCard,
                    contentColor   = if (selected.size == 2) DarkBackground else TextSecondary
                )
            ) {
                Text(
                    if (selected.size == 2) "START ADVENTURE!" else "Select 2 companions (${selected.size}/2)",
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun StarterCard(
    species: CreatureSpecies,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) AccentGold else DarkCard
    val bgColor = if (isSelected) DarkCard.copy(alpha = 0.9f) else DarkSurface

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val dummyInstance = remember(species.id) {
                com.monstergame.engine.model.CreatureFactory.createFromSpecies(species, 5)
            }
            CreatureSprite(
                creature = dummyInstance,
                primaryType = species.types.first(),
                size = 64.dp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = species.name,
                color = if (isSelected) AccentGold else TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                species.types.forEach { TypeChip(it) }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = species.regionId,
                color = TextSecondary,
                fontSize = 9.sp
            )
            if (isSelected) {
                Text("✓", color = AccentGold, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}
