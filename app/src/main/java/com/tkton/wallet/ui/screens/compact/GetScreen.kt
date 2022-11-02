package com.tkton.wallet.ui.screens.compact

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tkton.wallet.R
import com.tkton.wallet.data.DataManager
import com.tkton.wallet.ui.components.TkCardItem
import com.tkton.wallet.ui.components.TkDivider
import com.tkton.wallet.utils.SendTextCopyIntent


@Composable
fun CopyableWalletAddress(address: String?) {
    val context = LocalContext.current
    val title = stringResource(R.string.title_share)

    Card {
        Column {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.title_copy_address),
                    style = MaterialTheme.typography.subtitle2,
                    textAlign = TextAlign.Center
                )
            }
            TkDivider(
                modifier = Modifier.padding(start = 12.dp, end = 12.dp)
            )
            TkCardItem("$address",
                { Icon(Icons.Default.Share, contentDescription = null) },
                onIconClick = { SendTextCopyIntent(title,address ?: "", context) })
        }
    }
}

@Composable
fun GetScreen(){
    val database = DataManager.getInstance(LocalContext.current)
    val currentWalletState = database.wallets().getCurrentWalletLive().observeAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        CopyableWalletAddress(currentWalletState.value?.address ?: "")
    }
}