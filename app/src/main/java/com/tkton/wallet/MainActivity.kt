package com.tkton.wallet

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import com.tkton.wallet.data.launchUpdateJob

import com.tkton.wallet.ui.screens.MainAppInterface
import com.tkton.wallet.ui.screens.WindowSizeClass
import com.tkton.wallet.ui.theme.TKTonTheme
import kotlinx.coroutines.Job


class MainActivity : ComponentActivity() {
    private var updateJob : Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        updateJob = launchUpdateJob(this)
        Log.i("DEVICE", Build.DEVICE)

        setContent {
            TKTonTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainAppInterface(WindowSizeClass.COMPACT)
                }
            }
        }
    }

    override fun onDestroy() {
        if (updateJob != null) updateJob?.cancel()
        super.onDestroy()
    }
}