package com.tkton.wallet.ui.components

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun DialogMessage(text: String) {
    Text(text, style = MaterialTheme.typography.body1)
}