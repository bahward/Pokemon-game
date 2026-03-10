package com.monstergame.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.monstergame.ui.screens.*

sealed class Screen(val route: String) {
    object Title         : Screen("title")
    object NewGame       : Screen("new_game")
    object StarterSelect : Screen("starter_select")
    object Overworld     : Screen("overworld")
    object Battle        : Screen("battle/{battleId}") {
        fun route(battleId: String) = "battle/$battleId"
    }
    object Party         : Screen("party")
    object Bag           : Screen("bag")
    object PC            : Screen("pc")
    object Shop          : Screen("shop/{shopId}") {
        fun route(shopId: String) = "shop/$shopId"
    }
    object Dialogue      : Screen("dialogue/{npcId}") {
        fun route(npcId: String) = "dialogue/$npcId"
    }
    object SaveLoad      : Screen("save_load")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Title.route) {
        composable(Screen.Title.route) {
            TitleScreen(
                onNewGame = { navController.navigate(Screen.NewGame.route) },
                onContinue = { navController.navigate(Screen.Overworld.route) },
                onSaveLoad = { navController.navigate(Screen.SaveLoad.route) }
            )
        }
        composable(Screen.NewGame.route) {
            NewGameScreen(
                onStarterSelect = { navController.navigate(Screen.StarterSelect.route) {
                    popUpTo(Screen.Title.route)
                }}
            )
        }
        composable(Screen.StarterSelect.route) {
            StarterSelectScreen(
                onConfirm = {
                    navController.navigate(Screen.Overworld.route) {
                        popUpTo(Screen.Title.route)
                    }
                }
            )
        }
        composable(Screen.Overworld.route) {
            OverworldScreen(
                onBattle = { id -> navController.navigate(Screen.Battle.route(id)) },
                onOpenParty = { navController.navigate(Screen.Party.route) },
                onOpenBag   = { navController.navigate(Screen.Bag.route) },
                onOpenPC    = { navController.navigate(Screen.PC.route) },
                onOpenShop  = { id -> navController.navigate(Screen.Shop.route(id)) },
                onDialogue  = { id -> navController.navigate(Screen.Dialogue.route(id)) }
            )
        }
        composable(
            Screen.Battle.route,
            arguments = listOf(navArgument("battleId") { type = NavType.StringType })
        ) { back ->
            val battleId = back.arguments?.getString("battleId") ?: "wild"
            BattleScreen(
                battleId = battleId,
                onBattleEnd = { navController.popBackStack() }
            )
        }
        composable(Screen.Party.route) {
            PartyScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Bag.route) {
            BagScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.PC.route) {
            PCScreen(onBack = { navController.popBackStack() })
        }
        composable(
            Screen.Shop.route,
            arguments = listOf(navArgument("shopId") { type = NavType.StringType })
        ) { back ->
            ShopScreen(
                shopId = back.arguments?.getString("shopId") ?: "",
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            Screen.Dialogue.route,
            arguments = listOf(navArgument("npcId") { type = NavType.StringType })
        ) { back ->
            DialogueScreen(
                npcId = back.arguments?.getString("npcId") ?: "",
                onDone = { navController.popBackStack() }
            )
        }
        composable(Screen.SaveLoad.route) {
            SaveLoadScreen(onBack = { navController.popBackStack() })
        }
    }
}
