package com.monstergame.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.monstergame.ui.theme.*
import com.monstergame.viewmodel.GameViewModel
import kotlinx.coroutines.delay

@Composable
fun TitleScreen(
    onNewGame: () -> Unit,
    onContinue: () -> Unit,
    onSaveLoad: () -> Unit,
    vm: GameViewModel = hiltViewModel()
) {
    val uiState by vm.uiState.collectAsState()
    val hasSave = uiState.saveSlots.any { it.exists && !it.corrupt }

    var titleVisible by remember { mutableStateOf(false) }
    var buttonsVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        titleVisible = true
        delay(800)
        buttonsVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF0D0D2B), Color(0xFF0A1628), Color(0xFF0D2B0D)))
            )
    ) {
        // Decorative stars
        StarField()

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            AnimatedVisibility(
                visible = titleVisible,
                enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { -60 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "MONSTER",
                        color = AccentGold,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 8.sp
                    )
                    Text(
                        text = "QUEST",
                        color = AccentBlue,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 6.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Three Regions • One Journey",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(Modifier.height(64.dp))

            // Buttons
            AnimatedVisibility(
                visible = buttonsVisible,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { 40 }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TitleButton("NEW GAME", onClick = onNewGame, primary = true)
                    if (hasSave) {
                        TitleButton("CONTINUE", onClick = {
                            uiState.saveSlots.firstOrNull { it.exists }?.let { vm.loadGame(it.slot) }
                            onContinue()
                        })
                        TitleButton("SAVE / LOAD", onClick = onSaveLoad)
                    }
                }
            }

            Spacer(Modifier.height(48.dp))

            AnimatedVisibility(visible = buttonsVisible) {
                Text(
                    text = "v1.0 • Monster Quest",
                    color = TextSecondary.copy(alpha = 0.5f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun TitleButton(text: String, onClick: () -> Unit, primary: Boolean = false) {
    Button(
        onClick = onClick,
        modifier = Modifier.width(220.dp).height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (primary) AccentGold else DarkCard,
            contentColor   = if (primary) Color.Black else TextPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(4.dp)
    ) {
        Text(text = text, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, fontSize = 14.sp)
    }
}

@Composable
private fun StarField() {
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "star_alpha"
    )
    Box(modifier = Modifier.fillMaxSize().alpha(alpha)) {
        // Placeholder star field — actual rendering via Canvas would be more elaborate
    }
}
