package com.tkton.wallet.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tkton.wallet.R
import com.tkton.wallet.ui.theme.Grey500
import com.tkton.wallet.utils.cutAddress
import com.tkton.wallet.utils.utimeToFormattedLocalDateTime

@Composable
fun TkTitle(title: String) {
    Text(text = title, style = MaterialTheme.typography.h4, textAlign = TextAlign.Center)
}

@Composable
fun TransactionAddress(address : String) {
    Text(text = cutAddress(address), style = MaterialTheme.typography.subtitle1)
}

@Composable
fun TransactionTime(now: UInt){
    Text(
        text = utimeToFormattedLocalDateTime(now.toLong()),
        style = MaterialTheme.typography.subtitle2
    )
}

@Composable
fun TransactionAmount(amount: String) {
    Text(text = amount, style = MaterialTheme.typography.body1)
}

@Composable
fun DateTitle(text : String) {
    Text(text = text, modifier = Modifier.padding(10.dp), style = MaterialTheme.typography.subtitle1)
}

@Composable
fun TkSubtitle(text: String) {
    Text(text, style = MaterialTheme.typography.subtitle1)
}
