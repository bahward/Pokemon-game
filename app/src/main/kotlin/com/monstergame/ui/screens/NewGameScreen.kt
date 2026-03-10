package com.monstergame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.monstergame.ui.theme.*
import com.monstergame.viewmodel.GameViewModel

@Composable
fun NewGameScreen(
    onStarterSelect: () -> Unit,
    vm: GameViewModel = hiltViewModel()
) {
    var playerName by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize().background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text("NEW ADVENTURE", style = MaterialTheme.typography.displayMedium,
                color = AccentGold)
            Spacer(Modifier.height(8.dp))
            Text("What is your name, trainer?",
                style = MaterialTheme.typography.bodyLarge, color = TextSecondary)

            OutlinedTextField(
                value = playerName,
                onValueChange = { playerName = it.take(12); nameError = false },
                label = { Text("Your Name") },
                singleLine = true,
                isError = nameError,
                supportingText = if (nameError) {{ Text("Name required (max 12 chars)") }} else null,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    if (playerName.isNotBlank()) {
                        vm.startNewGame(playerName.trim())
                        onStarterSelect()
                    } else nameError = true
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentBlue,
                    focusedLabelColor  = AccentBlue
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    if (playerName.isNotBlank()) {
                        vm.startNewGame(playerName.trim())
                        onStarterSelect()
                    } else nameError = true
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = DarkBackground)
            ) {
                Text("BEGIN JOURNEY", fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold)
            }
        }
    }
}
