package com.tkton.wallet.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.runtime.MutableState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.LocalDateTime

fun cutAddress(address: String?) : String {
    /* Cutting address to ABCD...EFGT */
    if (address == null) {
        return "...'"
    }

    if (address.length < 8) {
        return  address
    }

    if (address.isNotEmpty()) {
        val first4 = address.slice(0..3)
        val last4 = address.slice(address.length-4..address.length-1)
        return "$first4...$last4"
    }
    return address
}


fun utimeToFormattedLocalDateTime(utime: Long?) : String {
    if (utime == null) {
        return ""
    }
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
    val transactionDate: LocalDateTime = Instant.ofEpochSecond(utime).atZone(ZoneId.systemDefault()).toLocalDateTime()
    val formattedTransactionDate: String = transactionDate.format(dateFormatter)
    return formattedTransactionDate
}


fun utimeToLocalDateTime(utime: Long?) : LocalDateTime {
    if (utime == null) {
        return LocalDateTime.MIN
    }
    val transactionDate: LocalDateTime = Instant.ofEpochSecond(utime).atZone(ZoneId.systemDefault()).toLocalDateTime()
    return transactionDate
}


fun utimeToLocalizedMonth(utime: Long?) : String {
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM yyyy")
    if (utime == null) {
        return ""
    }
    val transactionDate: LocalDateTime = Instant.ofEpochSecond(utime).atZone(ZoneId.systemDefault()).toLocalDateTime()
    return transactionDate.format(dateFormatter)
}


fun utimeToMonth(utime: Long?) : Int {
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("M")
    if (utime == null) {
        return 0
    }
    val transactionDate: LocalDateTime = Instant.ofEpochSecond(utime).atZone(ZoneId.systemDefault()).toLocalDateTime()
    return transactionDate.format(dateFormatter).toInt()
}

fun areWordsValid(words: MutableMap<Int, MutableState<String>>) : Boolean {
    var valid = true
    for (state in words.values) {
        if(state.value.isBlank()) {
            valid = false
            break
        }
    }
    return valid
}

fun convertToArray(words: MutableMap<Int, MutableState<String>>) : Array<String> {
    val container = mutableListOf<String>()
    var word : String
    for (i in 1..24){
        word = words[i]?.value ?: ""
        container.add(word)
    }
    return container.toTypedArray()
}

fun isInternetAvailable(context: Context): Boolean {
    var result = false
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkCapabilities = connectivityManager.activeNetwork ?: return false
    val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
    result = when {
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        else -> false
    }
    return result
}