package com.tkton.wallet.ui.screens.compact

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.tkton.wallet.R
import com.tkton.wallet.ui.components.TkActionButton

@Composable
fun LandingScreen(navController: NavHostController) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    )
    {
        Text(text = stringResource(R.string.message_question_wallet),
            style = MaterialTheme.typography.h4,
            textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.Center)
        {
            TkActionButton(
                text = stringResource(R.string.button_create_new),
                onClick = { navController.navigate("NewWalletScreen") })
            Spacer(modifier = Modifier.size(10.dp))
            TkActionButton(
                text = stringResource(R.string.button_import_existing),
                onClick = { navController.navigate("ExistingWalletScreen") })
        }
    }
}