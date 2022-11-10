package com.tkton.wallet.ui.screens.compact

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource

import androidx.navigation.NavController
import com.tkton.wallet.R
import com.tkton.wallet.data.load10Transactions
import com.tkton.wallet.data.loadAccountInfo
import com.tkton.wallet.ui.components.*
import com.tkton.wallet.ui.theme.Green500
import com.tkton.wallet.ui.theme.Red500
import org.ton.block.Transaction

@Composable
fun ExplorerScreen(navController: NavController) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top)
    {
        BaseRow(
            color = Red500,
            title = stringResource(R.string.title_address_spy),
            subtitle = stringResource(R.string.message_check_address),
            onClick = { navController.navigate("SpyAddressScreen") })
        BaseRow(
            color = Green500,
            title = stringResource(R.string.title_address_transactions),
            subtitle = stringResource(R.string.message_check_transactions),
            onClick = { navController.navigate("SpyAddressTransaction") })

    }
}

@Composable
fun SpyAddressScreen(){
    var address by remember { mutableStateOf("") }
    var accountBalance by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var showProgress  by remember { mutableStateOf(false) }
    var showError  by remember { mutableStateOf(false) }
    var showResult  by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    if (showError) {
        TkAlertDialog(
            onDismiss = { showError = false },
            title = stringResource(R.string.title_error_occurred),
            bodyText = { DialogMessage(errorMessage) },
            dismissButtonText = stringResource(R.string.button_close)
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        TkHorizontalSpacer()
        TkInput(address, onValueChange = { address = it }, placeholder = stringResource(R.string.placeholder_enter_address))
        TkHorizontalSpacer()
        TkActionButton(
            text = stringResource(R.string.button_search),
            showProgress = showProgress,
            onClick = {
                showProgress = true
                showResult = false
                loadAccountInfo(
                    address, context, scope,
                    onFinish = {
                        accountBalance = "${String.format("%.3f", if (it != "null") it.toDouble() else 0.0)} TON"
                        showProgress = false
                        showResult = true
                    },
                    onError = {
                        errorMessage = it
                        showProgress = false
                        showError = true
                    }
                )
            })
        TkHorizontalSpacer()
        if (showResult) {
            TkTitle(stringResource(R.string.message_external_balance) + ": $accountBalance")
        }
    }

}

@Composable
fun SpyAddressTransaction() {
    var address by remember { mutableStateOf("") }
    var showResult  by remember { mutableStateOf(false) }
    var transactions by remember { mutableStateOf<List<Transaction>?>(emptyList()) }
    var showProgress  by remember { mutableStateOf(false) }
    var errorMessage  by remember { mutableStateOf("") }
    var showError  by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    if (showError) {
        TkAlertDialog(
            onDismiss = { showError = false },
            title = stringResource(R.string.title_error_occurred),
            bodyText = { DialogMessage(errorMessage) },
            dismissButtonText = stringResource(R.string.button_close)
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        TkHorizontalSpacer()
        TkInput(address, onValueChange = { address = it }, placeholder = stringResource(R.string.placeholder_enter_address))
        TkHorizontalSpacer()
        TkActionButton(
            text = stringResource(R.string.button_search),
            showProgress = showProgress,
            onClick = {
                showResult = false
                showProgress = true
                load10Transactions(
                    address, context, scope,
                    onError = {
                        errorMessage = it
                        showProgress = false
                        showError = true
                              },
                    onFinish = {
                        transactions = it
                        showProgress = false
                        showResult = true
                    }
                )
            })
        TkHorizontalSpacer()
        if (showResult) {
            for (transaction in transactions ?: emptyList()) {
                TransactionCard(transaction)
            }
        }
    }
}