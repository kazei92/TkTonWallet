package com.tkton.wallet.data

import android.content.Context
import kotlinx.coroutines.*
import org.ton.block.Transaction

fun addNewWallet(
    words: Array<String>,
    context: Context,
    scope: CoroutineScope,
    onFinish: () -> Unit = {},
    onError: (String) -> Unit = {}
)
{
    val dataManager = DataManager.getInstance(context)
    try {
        scope.launch (Dispatchers.IO) {
            val currentWallet = dataManager.settings().getCurrentWallet()
            val loggedIn = currentWallet != null
            if (loggedIn == true) {
                dataManager.words().insert(Words(wordsList = words))
                dataManager.createNewWallet(words)
                dataManager.transactions().clean()
            } else {
                dataManager.words().insert(Words(wordsList = words))
                dataManager.createNewWallet(words)
            }
            onFinish()
        }
    } catch (e: Exception) {
        onError(e.toString())
    }

}

fun addExistingWallet(
    words: Array<String>,
    context: Context,
    scope: CoroutineScope,
    onFinish: () -> Unit = {},
    onError: (String) -> Unit = {}
)
{
    val dataManager = DataManager.getInstance(context)
    scope.launch (Dispatchers.IO) {
        try {
            dataManager.loadWallets(words)
            onFinish()
        } catch (e: Exception) {
            onError(e.toString())
        }
    }
}

fun transferCoins(
    address: String,
    amount: String,
    comment: String,
    context: Context,
    scope: CoroutineScope,
    onFinish: () -> Unit = {},
    onError: (String) -> Unit = {}
)
{
    val dataManager = DataManager.getInstance(context)
    scope.launch(context = Dispatchers.IO) {
        try {
            dataManager.transfer(address, amount, comment)
            onFinish()
        } catch (e: Exception) {
            onError(e.toString())
        }
    }
}

fun changeAppMode(
    isInTestMode: Boolean,
    context: Context)
{
    GlobalScope.launch(Dispatchers.IO) {
        val database = DataManager.getInstance(context)
        if (isInTestMode) {
            database.setToNormalMode()
        } else {
            database.setToTestMode()
        }
    }
}

fun load10Transactions(
    address: String,
    context: Context,
    scope: CoroutineScope,
    onFinish: (List<Transaction>?) -> Unit = {},
    onError: (String) -> Unit = {}
) {
    scope.launch(Dispatchers.Default) {
        try {
            val liteClient = DataManager.getInstance(context).getLiteClient()
            val transactions = liteClient.loadLast10Transactions(address)
            onFinish(transactions)
        } catch (e: Exception) {
            onError(e.toString())
        }

    }
}

fun loadAccountInfo(
    address: String,
    context: Context,
    scope: CoroutineScope,
    onFinish: (String) -> Unit = {},
    onError: (String) -> Unit = {}
) {
    scope.launch(Dispatchers.Default) {
        try {
            val liteClient = DataManager.getInstance(context).getLiteClient()
            val accountInfo = liteClient.getAccountInfo(address)
            val accountBalance = accountInfo?.storage?.balance?.coins.toString()
            onFinish(accountBalance)
        } catch (e: Exception) {
            onError(e.toString())
        }
    }
}