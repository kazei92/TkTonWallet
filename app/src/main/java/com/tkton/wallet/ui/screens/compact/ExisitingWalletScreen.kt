package com.tkton.wallet.ui.screens.compact

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.tkton.wallet.R
import com.tkton.wallet.data.addExistingWallet
import com.tkton.wallet.ui.components.*
import com.tkton.wallet.ui.theme.buttonMaxHeight
import com.tkton.wallet.utils.areWordsValid
import com.tkton.wallet.utils.convertToArray
import org.ton.mnemonic.Mnemonic

@Composable
fun WordsGrid(words: MutableMap<Int, MutableState<String>>){
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val order = mutableListOf<Int>()
    for (i in 1..12) {
        order.add(i)
        order.add(i+12)
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.sizeIn(maxHeight = screenHeight - buttonMaxHeight)
    ) {
        for (i in order) {
            item {
                TkSearchableInput(
                    value= words[i]?.value ?: "",
                    onValueChange = { words[i]?.value = it },
                    placeholder = "Word $i", data= Mnemonic.DEFAULT_WORDLIST)
            }
        }
    }
}


@Composable
fun ExistingWalletScreen(navController: NavHostController){
    val words = mutableMapOf<Int, MutableState<String>>()
    val context = LocalContext.current
    var showProgress by rememberSaveable { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var errorMessage by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    if (showError) {
        TkAlertDialog(
            title = stringResource(R.string.title_error_occurred),
            bodyText = { DialogMessage(text = errorMessage) },
            onDismiss = { showError = false },
            dismissButtonText = stringResource(R.string.button_close)
        )
    }

    LaunchedEffect(finished) {
        if (finished) {
            navController.navigate("WalletScreen")
        }
    }

    for (i in 1..24){
        words[i] = rememberSaveable { mutableStateOf("") }
    }


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        WordsGrid(words)
        TkVerticalSpacer()
        TkActionButton(text = stringResource(R.string.button_import),
            showProgress=showProgress,
            onClick = {
                showProgress = true
                if (areWordsValid(words)) {
                    addExistingWallet(
                        convertToArray(words), context, scope,
                        onFinish = {
                            showProgress = false
                            finished = true
                        },
                        onError = {
                            errorMessage = it
                            showError = true
                        }
                    )
                } else {
                    errorMessage = "Invalid words!"
                    showProgress = false
                    showError = true
                }
        })
    }
}