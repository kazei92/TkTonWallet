package com.tkton.wallet.data

import androidx.activity.ComponentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

fun launchUpdateJob(context: ComponentActivity) : Job {
    val dataManager = DataManager.getInstance(context)
    return GlobalScope.launch(Dispatchers.IO) {
        dataManager.updateLiteClientConfig()
        val currentWallet = dataManager.settings().getCurrentWallet()
        val loggedIn = currentWallet != null
        if (loggedIn) { dataManager.updateData() }
    }
}