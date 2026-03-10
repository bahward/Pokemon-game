package com.monstergame.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.monstergame.engine.model.ItemCategory
import com.monstergame.ui.theme.*
import com.monstergame.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BagScreen(
    onBack: () -> Unit,
    vm: GameViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    val bag = state.saveData?.bag
    var selectedCategory by remember { mutableStateOf(ItemCategory.MEDICINE) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BAG", color = AccentGold, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TextPrimary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Category tabs
            ScrollableTabRow(
                selectedTabIndex = ItemCategory.entries.indexOf(selectedCategory),
                containerColor = DarkSurface,
                contentColor = AccentBlue
            ) {
                ItemCategory.entries.forEach { cat ->
                    Tab(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        text = {
                            Text(cat.name, fontSize = 11.sp,
                                color = if (selectedCategory == cat) AccentBlue else TextSecondary)
                        }
                    )
                }
            }

            val items = bag?.items
                ?.mapNotNull { (id, qty) ->
                    val item = vm.repo.getItem(id) ?: return@mapNotNull null
                    Pair(item, qty)
                }
                ?.filter { (item, _) -> item.category == selectedCategory }
                ?.sortedBy { (item, _) -> item.name }
                ?: emptyList()

            if (items.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No items in this pocket", color = TextSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(items) { (item, qty) ->
                        Surface(
                            color = DarkCard,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(item.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(item.description, color = TextSecondary, fontSize = 10.sp)
                                }
                                Text("×$qty", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
