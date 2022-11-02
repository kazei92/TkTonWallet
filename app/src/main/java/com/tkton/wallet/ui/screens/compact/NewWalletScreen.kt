package com.tkton.wallet.ui.screens.compact

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import com.tkton.wallet.R
import com.tkton.wallet.ui.components.TkActionButton
import com.tkton.wallet.ui.components.TkVerticalSpacer

@Composable
fun NewWalletScreen(navController: NavHostController) {

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    )
    {
        Text(text = stringResource(R.string.message_congratulations),
            style = MaterialTheme.typography.h4,
            textAlign = TextAlign.Center)
        TkVerticalSpacer()
        Text(text = stringResource(R.string.message_wallet_created),
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center)
        Text(text = stringResource(R.string.message_control),
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center)
        TkVerticalSpacer()
        Text(text = stringResource(R.string.message_wallet_access),
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center)
        TkVerticalSpacer()
        TkActionButton(text = stringResource(R.string.button_continue), onClick = {
            navController.navigate("SecretWordsScreen")
        })
    }
}