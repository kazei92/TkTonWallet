package com.tkton.wallet.ui.screens.compact

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.tkton.wallet.R
import com.tkton.wallet.data.DataManager
import com.tkton.wallet.data.transferCoins
import com.tkton.wallet.ui.components.*
import kotlinx.coroutines.*

@Composable
fun SendScreen(navController: NavController){
    var address by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    val context = LocalContext.current
    val dataManager = DataManager.getInstance(context)
    val currentWalletBalance = dataManager.wallets().getCurrentWalletBalanceLive().observeAsState()
    var showProgress by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    val successMessage = stringResource(R.string.message_transfer_success)
    var dialogMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    if (showDialog) {
        TkAlertDialog(
            onDismiss = { showDialog = false },
            title = stringResource(R.string.title_result),
            bodyText = { DialogMessage(dialogMessage) },
            dismissButtonText = stringResource(R.string.button_close)
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        TkTitle(title = stringResource(R.string.message_your_balance) + ": ${String.format("%.3f", currentWalletBalance.value?.toDouble())} TON")
        TkVerticalSmallSpacer()
        TkInput(
            value = address,
            onValueChange = { address = it },
            placeholder = stringResource(R.string.placeholder_enter_address)
        )
        TkVerticalSmallSpacer()
        TkInput(
            value = amount,
            onValueChange = { amount = it },
            placeholder = stringResource(R.string.placeholder_enter_amount),
            isNumberInput = true
        )
        TkVerticalSmallSpacer()
        TkInput(
            value = comment,
            onValueChange = { comment = it },
            placeholder = stringResource(R.string.placeholder_enter_comment)
        )
        TkVerticalSmallSpacer()
        TkActionButton(
            text = stringResource(R.string.button_send),
            showProgress = showProgress,
            onClick = {
                showProgress = true
                transferCoins(
                    address = address,
                    amount = amount,
                    comment = comment,
                    context = context,
                    scope = scope,
                    onFinish = {
                        dialogMessage = successMessage + ": $amount TON -> $address"
                        showDialog = true
                        address = ""
                        amount = ""
                        comment = ""
                        showProgress = false
                    },
                    onError = {
                        dialogMessage = it
                        showDialog = true
                        showProgress = false
                    }
                )
            },
        )
    }
}
