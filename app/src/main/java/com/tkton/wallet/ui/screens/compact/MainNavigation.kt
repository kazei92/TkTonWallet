package com.tkton.wallet.ui.screens.compact

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.tkton.wallet.R
import com.tkton.wallet.data.DataManager


@Composable
fun BottomNavigationMenu(navController: NavController)
{

    BottomNavigation {
        val current = navController.currentBackStackEntryAsState()

        BottomNavigationItem(
            selected = current.value?.destination?.route == "WalletScreen",
            onClick = { navController.navigate("WalletScreen") },
            label = { Text(stringResource(R.string.wallet_navigation)) },
            icon = { Icon(Icons.Filled.Home, contentDescription = null) }
        )
        BottomNavigationItem(
            selected = current.value?.destination?.route == "ExplorerScreen",
            onClick = { navController.navigate("ExplorerScreen") },
            label = { Text(stringResource(R.string.explorer_navigation)) },
            icon = { Icon(Icons.Filled.Search, contentDescription = null) }
        )
        BottomNavigationItem(
            selected = current.value?.destination?.route == "SettingsScreen",
            onClick = { navController.navigate("SettingsScreen") },
            label = { Text(stringResource(R.string.settings_navigation)) },
            icon = { Icon(Icons.Filled.Settings, contentDescription = null) }
        )

    }
}

@Composable
fun MainNavigation()
{
    val scaffoldState = rememberScaffoldState()
    val navController = rememberNavController()
    var showBottomBar by remember { mutableStateOf(true) }
    val database = DataManager.getInstance(LocalContext.current)
    val currentWallet = database.settings().getCurrentWalletLive().observeAsState()
    var startDestination = "LandingScreen"
    if (currentWallet.value != null) {
        startDestination = "WalletScreen"
    }

    Scaffold(
        scaffoldState = scaffoldState,
        bottomBar = { if (showBottomBar) BottomNavigationMenu(navController) },
    ) { paddingValues ->
        Column(modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding()))

        {
            NavHost(navController, startDestination){
                composable("WalletScreen") {
                    showBottomBar = true
                    WalletScreen(navController)
                }
                composable("SettingsScreen") {
                    showBottomBar = true
                    SettingsScreen(navController)
                }
                composable("ExplorerScreen") {
                    showBottomBar = true
                    ExplorerScreen(navController)
                }
                composable("SendScreen") {
                    showBottomBar = false
                    SendScreen(navController) }
                composable("GetScreen") {
                    showBottomBar = false
                    GetScreen()
                }
                composable("SpyAddressScreen") {
                    showBottomBar = false
                    SpyAddressScreen()
                }
                composable("SpyAddressTransaction") {
                    showBottomBar = false
                    SpyAddressTransaction()
                }
                composable("SecretWordsExport") {
                    showBottomBar = false
                    SecretWordsExport()
                }
                composable("LandingScreen") {
                    showBottomBar = false
                    LandingScreen(navController)
                }
                composable("NewWalletScreen") {
                    showBottomBar = false
                    NewWalletScreen(navController)
                }
                composable("SecretWordsScreen") {
                    showBottomBar = false
                    SecretWordsScreen(navController)
                }
                composable("ExistingWalletScreen") {
                    showBottomBar = false
                    ExistingWalletScreen(navController)
                }
            }
        }
    }
}