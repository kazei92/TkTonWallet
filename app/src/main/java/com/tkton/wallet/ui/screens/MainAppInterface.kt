package com.tkton.wallet.ui.screens

import androidx.compose.runtime.Composable
import com.tkton.wallet.ui.screens.compact.CompactScreenInterface

enum class WindowSizeClass {
    COMPACT, EXPANDED, TABLET
}

@Composable
fun MainAppInterface(windowSizeClass: WindowSizeClass)  {
    when (windowSizeClass) {
        WindowSizeClass.COMPACT -> { CompactScreenInterface() }
        else -> { CompactScreenInterface() }
    }
}