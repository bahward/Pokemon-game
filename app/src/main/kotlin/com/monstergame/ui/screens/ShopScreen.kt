package com.monstergame.ui.screens

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
import com.monstergame.engine.model.Item
import com.monstergame.ui.theme.*
import com.monstergame.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    shopId: String,
    onBack: () -> Unit,
    vm: GameViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    val money = state.saveData?.money ?: 0
    val shop = vm.repo.getShop(shopId)

    var message by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(shop?.name ?: "Shop", color = AccentGold, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TextPrimary) } },
                actions = {
                    Text("${money}G", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                        modifier = Modifier.padding(end = 12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(12.dp)) {
            message?.let { msg ->
                Surface(
                    color = if (msg.startsWith("Bought")) AccentGreen.copy(0.2f) else AccentRed.copy(0.2f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(msg, color = TextPrimary, modifier = Modifier.padding(8.dp))
                }
                Spacer(Modifier.height(8.dp))
                LaunchedEffect(msg) {
                    kotlinx.coroutines.delay(2000)
                    message = null
                }
            }

            if (shop == null) {
                Text("Shop not found: $shopId", color = AccentRed)
                return@Column
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(shop.items) { shopItem ->
                    val item = vm.repo.getItem(shopItem.itemId) ?: return@items
                    val price = shopItem.price ?: item.price
                    ShopItemRow(
                        item    = item,
                        price   = price,
                        canBuy  = money >= price,
                        onBuy   = {
                            if (vm.spendMoney(price)) {
                                vm.addItemToParty(item.id)
                                message = "Bought ${item.name}!"
                            } else {
                                message = "Not enough gold!"
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ShopItemRow(item: Item, price: Int, canBuy: Boolean, onBuy: () -> Unit) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(item.description, color = TextSecondary, fontSize = 10.sp)
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text("${price}G", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Button(
                    onClick = onBuy,
                    enabled = canBuy,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentGold,
                        contentColor = DarkBackground
                    )
                ) { Text("BUY", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold) }
            }
        }
    }
}
