package com.tkton.wallet.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tkton.wallet.R
import com.tkton.wallet.tonops.getTransactionAddress
import com.tkton.wallet.tonops.getTransactionAmount
import com.tkton.wallet.tonops.getTransactionComment
import com.tkton.wallet.ui.theme.TKTonThemeDialogThemeOverlay
import com.tkton.wallet.utils.utimeToFormattedLocalDateTime
import org.ton.block.Transaction
import com.tkton.wallet.ui.theme.*

@Composable
fun TkAlertDialog(
    title: String,
    bodyText: @Composable () -> Unit,
    onDismiss: () -> Unit,
    dismissButtonText: String,
    onConfirm: () -> Unit = {},
    confirmButtonText: String? = null
) {
    TKTonThemeDialogThemeOverlay {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = title, textAlign = TextAlign.Center, style = MaterialTheme.typography.h5) },
            text = bodyText,
            modifier = Modifier.fillMaxWidth(),
            buttons = {
                Column {
                    Divider(
                        Modifier.padding(horizontal = 12.dp),
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
                    )
                    TextButton(
                        onClick = onDismiss,
                        shape = RectangleShape,
                        contentPadding = PaddingValues(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(dismissButtonText)
                    }
                    if (confirmButtonText != null) {
                        TextButton(
                            onClick = onConfirm,
                            shape = RectangleShape,
                            contentPadding = PaddingValues(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(confirmButtonText)
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun TransactionDetails(transaction: Transaction, onDismissRequest: () -> Unit) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    TkAlertDialog(
        onDismiss = { onDismissRequest() },
        title = stringResource(R.string.title_transaction_details),
        bodyText = { Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            DetailsRow(
                title = stringResource(R.string.title_address),
                subtitle = getTransactionAddress(transaction),
                onClick = { clipboardManager.setText(AnnotatedString(getTransactionAddress(transaction))) }
            )
            DetailsRow(
                title = stringResource(R.string.title_time),
                subtitle = utimeToFormattedLocalDateTime(transaction.now.toLong()),
                onClick = { clipboardManager.setText(AnnotatedString(utimeToFormattedLocalDateTime(transaction.now.toLong()))) }
            )
            DetailsRow(
                title = stringResource(R.string.title_fee),
                subtitle = transaction.total_fees.coins.toString(),
                onClick = { clipboardManager.setText(AnnotatedString(transaction.total_fees.coins.toString())) }
            )
            DetailsRow(
                title = stringResource(R.string.title_amount),
                subtitle = getTransactionAmount(transaction),
                onClick = { clipboardManager.setText(AnnotatedString(getTransactionAmount(transaction))) }
            )
            DetailsRow(
                title = stringResource(R.string.title_comment),
                subtitle = getTransactionComment(transaction) ?: "",
                onClick = { clipboardManager.setText(AnnotatedString(getTransactionComment(transaction) ?: "")) }
            )
        } },
        dismissButtonText = stringResource(R.string.button_close)
    )
}