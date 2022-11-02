package com.tkton.wallet.ui.screens.compact

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tkton.wallet.R
import com.tkton.wallet.data.DataManager
import com.tkton.wallet.data.changeAppMode
import com.tkton.wallet.ui.components.DialogMessage
import com.tkton.wallet.ui.components.SettingsCard
import com.tkton.wallet.ui.components.TkActionButton
import com.tkton.wallet.ui.components.TkAlertDialog
import com.tkton.wallet.utils.OpenInBrowserIntent
import com.tkton.wallet.utils.SendTextCopyIntent
import com.tkton.wallet.utils.cutAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Composable
fun WalletPicker(navController: NavController){
    var showDropDown by remember { mutableStateOf(false) }
    val dataManager = DataManager.getInstance(LocalContext.current)
    val currentWallet = dataManager.wallets().getCurrentWalletLive().observeAsState()
    val wallets = dataManager.wallets().getWallets().observeAsState()

    SettingsCard(
        settingName = stringResource(R.string.title_current_wallet),
        settingValue = cutAddress(currentWallet.value?.address ?: ""),
        onClick = { showDropDown = true },
        content = {
            if (showDropDown) {
                DropdownMenu(
                    expanded = true,
                    onDismissRequest = { showDropDown = false },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    for (wallet in wallets.value!!) {
                        DropdownMenuItem(onClick = {
                            GlobalScope.launch(context = Dispatchers.Default) {
                                dataManager.changeCurrentWallet(wallet)
                            }
                            showDropDown = false
                        }) {
                            Text(" ${cutAddress(wallet.address)}: ${wallet.version}")
                        }
                        Spacer(modifier = Modifier
                            .height(1.dp))
                    }
                    DropdownMenuItem(onClick = {
                        showDropDown = false
                        navController.navigate("LandingScreen")
                    })
                    {
                        Text(" Add another")
                    }
                }
            }
        }
    )
}

@Composable
fun ModeSwitcher(navController: NavController) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val currentModeState = DataManager.getInstance(context).settings().getModeLive().observeAsState()
    val testmode = currentModeState.value == "Y"

    SettingsCard(
        settingName = stringResource(R.string.title_switch_mode),
        settingValue = if (testmode) stringResource(R.string.message_testnet) else stringResource(R.string.message_mainnet),
        onClick = { showConfirmDialog = true },
        content = {
            if (showConfirmDialog) {
                TkAlertDialog(
                    onDismiss = { showConfirmDialog = false },
                    title = stringResource(R.string.message_are_sure),
                    bodyText = { Text(text = stringResource(R.string.message_switch_mode),
                        style = MaterialTheme.typography.subtitle2) },
                    dismissButtonText = stringResource(R.string.button_cancel),
                    confirmButtonText = stringResource(R.string.button_confirm),
                    onConfirm = {
                        showConfirmDialog = false
                        changeAppMode(testmode, context)
                        navController.navigate("LandingScreen")
                    }
                )
            }
        }
    )
}

@Composable
fun BugReports() {
    val context = LocalContext.current
    val link = stringResource(R.string.link_telegram)
    val title = stringResource(R.string.title_openwith)

    SettingsCard(
        settingName = stringResource(R.string.title_bugreport),
        settingValue = "Telegram",
        onClick = { OpenInBrowserIntent(title, link, context) },
    )
}

@Composable
fun DevPage() {
    val context = LocalContext.current
    val link = stringResource(R.string.link_github)
    val title = stringResource(R.string.title_openwith)

    SettingsCard(
        settingName = stringResource(R.string.title_sourcecode),
        settingValue = "Github",
        onClick = { OpenInBrowserIntent(title, link, context) },
    )
}

@Composable
fun DonationAddress() {
    val context = LocalContext.current
    var showConfirmDialog by remember { mutableStateOf(false) }
    val address = stringResource(R.string.donation_address)
    val title = stringResource(R.string.title_share)

    SettingsCard(
        settingName = stringResource(R.string.title_donations),
        settingValue = cutAddress(stringResource(R.string.donation_address)),
        onClick = { showConfirmDialog = true },
        content = {
            if (showConfirmDialog) {
                TkAlertDialog(
                    onDismiss = { showConfirmDialog = false },
                    title = stringResource(R.string.title_donations),
                    bodyText = {
                        Column() {
                            DialogMessage(stringResource(R.string.message_donation))
                            SelectionContainer { DialogMessage(address) }
                        }
                               },
                    dismissButtonText = stringResource(R.string.button_close),
                    confirmButtonText = stringResource(R.string.button_copy),
                    onConfirm = { SendTextCopyIntent(title, address, context) }
                )
            }
        }
    )
}

@Composable
fun UserLogOut(navController: NavController) {
    val database = DataManager.getInstance(LocalContext.current)
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        TkAlertDialog(
            onDismiss = { showConfirmDialog = false },
            title = stringResource(R.string.message_are_sure),
            bodyText = { DialogMessage(text = stringResource(R.string.message_wallet_delete)) },
            dismissButtonText = stringResource(R.string.button_cancel),
            confirmButtonText = stringResource(R.string.button_confirm),
            onConfirm = {
                showConfirmDialog = false
                GlobalScope.launch(context = Dispatchers.IO) { database.clean() }
                navController.navigate("LandingScreen")
            }
        )
    }
    TkActionButton(
        text = stringResource(R.string.button_logout),
        onClick = { showConfirmDialog = true }
    )
}

@Composable
fun SecretWordsExport() {
    val context = LocalContext.current
    var refreshing by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    var words by remember { mutableStateOf<Array<String>?>(null) }

    LaunchedEffect(refreshing) {
        if (refreshing) {
            scope.launch(context = Dispatchers.IO){
                val database = DataManager.getInstance(context)
                val currentWallet = database.wallets().getCurrentWallet()
                if (currentWallet != null) {
                    val wordsId = currentWallet.words
                    if (wordsId != null) {
                        val walletWords = database.words().findById(wordsId)
                        words = walletWords?.wordsList
                    }
                }
                refreshing = false
            }
        }
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .fillMaxSize()
    ) {
        if (refreshing) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(25.dp))
        }
        SecretWordsList(words)

    }

}

@Composable
fun SecretWords(navController: NavController) {
    SettingsCard(
        settingName = stringResource(R.string.title_secretkey),
        settingValue = stringResource(R.string.title_exporter),
        onClick = { navController.navigate("SecretWordsExport") },
    )
}

@Composable
fun SettingsScreen(navController: NavController)
{
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier.fillMaxSize()
    ) {
        WalletPicker(navController)
        SecretWords(navController)
        BugReports()
        DonationAddress()
        DevPage()
        ModeSwitcher(navController)
        UserLogOut(navController)
    }
}