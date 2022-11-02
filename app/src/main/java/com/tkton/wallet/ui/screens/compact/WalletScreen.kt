package com.tkton.wallet.ui.screens.compact

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.tkton.wallet.data.DataManager
import com.tkton.wallet.R
import com.tkton.wallet.data.RawTransaction
import com.tkton.wallet.tonops.getTransactionAddress
import com.tkton.wallet.tonops.getTransactionAmount
import com.tkton.wallet.tonops.transactionFromRaw
import com.tkton.wallet.ui.components.*
import com.tkton.wallet.ui.theme.Green500
import com.tkton.wallet.ui.theme.Red500
import com.tkton.wallet.ui.theme.spacer
import com.tkton.wallet.utils.cutAddress
import com.tkton.wallet.utils.utimeToLocalizedMonth
import com.tkton.wallet.utils.utimeToMonth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ton.block.IntMsgInfo
import org.ton.block.Transaction

@Composable
fun WalletScreen(navController: NavController) {
    val dataManager = DataManager.getInstance(LocalContext.current)
    val transactions = dataManager.transactions().getAllLive().observeAsState()
    var refreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(refreshing) {
        if (refreshing) {
            scope.launch(context = Dispatchers.IO){
                dataManager.updateData()
                refreshing = false
            }
        }
    }
    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing = refreshing),
        onRefresh = { refreshing = true },
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.fillMaxWidth()
        )
        {
            WalletHeader(navController)
            TransactionsList(transactions.value)
        }
    }

}

@Composable
fun WalletHeader(navController: NavController) {
    val dataManager = DataManager.getInstance(LocalContext.current)
    val balance = dataManager.wallets().getCurrentWalletBalanceLive().observeAsState()
    val address = dataManager.wallets().getCurrentWalletAddressLive().observeAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        TkVerticalSpacer()
        TkTitle(title = stringResource(R.string.message_your_balance) + ": ${String.format("%.3f", if (balance.value != null) balance.value?.toDouble() else 0.0)} TON")
        TkVerticalSmallSpacer()
        Text(stringResource(R.string.wallet_navigation) + ": " + cutAddress(address.value), style = MaterialTheme.typography.subtitle2)
        TkVerticalSpacer()
        Actions(navController)
        TkVerticalSpacer()
    }
}

@Composable
fun TransactionCard(transaction: Transaction){
    val inMsgInfo = transaction.in_msg.value?.info as? IntMsgInfo

    var showDetails by remember { mutableStateOf(false) }
    if (showDetails) {
        TransactionDetails(transaction, onDismissRequest = { showDetails = false })
    }

    ClickableCard(
        onClick = { showDetails = true }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        )
        {
            if (inMsgInfo != null) {
                Icon(
                    Icons.Rounded.ArrowBack,
                    contentDescription = null,
                    tint = Green500
                )
            } else {
                Icon(
                    Icons.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = Red500
                )
            }
            Column {
                TransactionAddress(getTransactionAddress(transaction))
                TransactionTime(transaction.now)
            }
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(75.dp)
            ) {
                TransactionAmount( String.format("%.3f", getTransactionAmount(transaction).toDouble())  )
            }
        }
    }
}


@Composable
fun TransactionsList(transactions: List<RawTransaction>?) {
    var currentMonth = 0
    LazyColumn()
    {
        if (transactions.isNullOrEmpty()) {
            item {
                TkVerticalSpacer()
                TkSubtitle("No transactions yet")
                TkVerticalHugeSpacer()
            }
        }
        for (raw in transactions ?: emptyList()) {
            val transaction = transactionFromRaw(raw)

            if (currentMonth != utimeToMonth(transaction.now.toLong())) {
                currentMonth = utimeToMonth(transaction.now.toLong())
                item {
                    DateTitle(utimeToLocalizedMonth(transaction.now.toLong()))
                }
            }
            item {
                key(raw.lt) {
                    TransactionCard(transaction)
                }
            }
        }
    }
}

@Composable
fun Actions(navController: NavController) {
    Row(
        horizontalArrangement = Arrangement.Center
    )
    {
        TkActionButton(
            text = stringResource(R.string.button_send),
            onClick = { navController.navigate("SendScreen") }
        )
        Spacer(modifier = Modifier.width(spacer * 2))
        TkActionButton(
            text = stringResource(R.string.button_get),
            onClick = { navController.navigate("GetScreen") }
        )
    }
}

