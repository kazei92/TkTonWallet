package com.tkton.wallet.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat

fun SendTextCopyIntent(title: String, text: String, context: Context) {
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "text/plain"
    intent.putExtra(Intent.EXTRA_SUBJECT, "Ton Wallet Address")
    intent.putExtra(Intent.EXTRA_TEXT, text)

    ContextCompat.startActivity(
        context,
        Intent.createChooser(intent, title),
        null
    )
}

fun OpenInBrowserIntent(title: String, url: String, context: Context) {
    val webpage: Uri = Uri.parse(url)
    val intent = Intent(Intent.ACTION_VIEW, webpage)

    ContextCompat.startActivity(
        context,
        Intent.createChooser(intent, title),
        null
    )
}