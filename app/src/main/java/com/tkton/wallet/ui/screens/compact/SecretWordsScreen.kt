package com.tkton.wallet.ui.screens.compact

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.tkton.wallet.R
import com.tkton.wallet.data.DataManager
import com.tkton.wallet.data.Words
import com.tkton.wallet.data.addNewWallet
import com.tkton.wallet.ui.components.DialogMessage
import com.tkton.wallet.ui.components.TkActionButton
import com.tkton.wallet.ui.components.TkAlertDialog
import com.tkton.wallet.ui.components.TkVerticalSpacer
import kotlinx.coroutines.*
import org.ton.mnemonic.Mnemonic
import com.tkton.wallet.ui.theme.*

@Composable
fun SecretWordsList(words: Array<String>?) {
    val order = mutableListOf<Int>()
    for (i in 0..12) {
        order.add(i)
        order.add(i+11)
    }

    if (words == null || words.isEmpty()) {
        CircularProgressIndicator()
    } else {
        SelectionContainer {
            LazyVerticalGrid(
                modifier = Modifier
                    .padding(horizontal = 50.dp)
                    .fillMaxWidth(),
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.Center,
            ) {
                for (i in order) {
                    item {
                        Text("${i + 1}. ${ words[i] } ")
                    }
                }
            }
        }
    }
}

@Composable
fun SecretWordsScreen(navController: NavHostController){
    val context = LocalContext.current
    var words by remember { mutableStateOf(emptyArray<String>()) }
    val scope = rememberCoroutineScope()
    var showProgress by remember { mutableStateOf(false) }
    var generatingWords by remember { mutableStateOf(true) }
    var finished by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    if (showError) {
        TkAlertDialog(
            title = stringResource(R.string.title_error_occurred),
            bodyText = { DialogMessage(text = errorMessage) },
            onDismiss = { showError = false },
            dismissButtonText = stringResource(R.string.button_close)
        )
    }

    LaunchedEffect(generatingWords) {
        scope.launch {
            words = Mnemonic.generate()
            generatingWords = false
        }
    }

    LaunchedEffect (finished) {
        if (finished) {
            try { navController.navigate("WalletScreen") } catch (e: Exception) {}
        }
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = standardPadding)
            .fillMaxSize()
    ) {
        Text(text = stringResource(R.string.message_24secretwords),
            style = MaterialTheme.typography.h4,
            textAlign = TextAlign.Center)
        TkVerticalSpacer()
        Text(text = stringResource(R.string.message_save_words),
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center)
        TkVerticalSpacer()
        Text(text = stringResource(R.string.message_restore_wallet),
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center)
        TkVerticalSpacer()
        SecretWordsList(words)
        TkVerticalSpacer()
        TkActionButton(
            text = stringResource(R.string.button_continue),
            showProgress = showProgress,
            onClick = {
                showProgress = true
                if (words.isNotEmpty()) {
                    addNewWallet(
                        words, context, scope,
                        onFinish = {
                            showProgress = false
                            finished = true },
                        onError = {
                            showProgress = false
                            errorMessage = it
                            showError = true }
                    )
                }
            },
        )
    }
}