package com.tkton.wallet.tonops

import com.tkton.wallet.data.RawTransaction
import org.ton.block.*
import org.ton.boc.BagOfCells
import org.ton.cell.Cell
import org.ton.tlb.parse

fun transactionFromRaw(raw: RawTransaction) : Transaction {
    return BagOfCells(raw.hash).roots.first().parse(Transaction)
}

fun parseComment(cell : Cell) : String {
    if (cell.isEmpty()) {
        return ""
    }

    val slice = cell.beginParse()
    slice.loadBits(32)
    val comment = slice.loadBits(cell.bits.size - 32)
    slice.endParse()
    return comment.toByteArray().decodeToString()
}

fun getTransactionComment(transaction: Transaction) : String? {
    var cell : Cell?
    val intMsgInfo = transaction.in_msg.value?.info as? IntMsgInfo
    if (intMsgInfo != null) {
        cell = transaction.in_msg.value?.body?.x
        if  (cell == null) {
            cell = transaction.in_msg.value?.body?.y
        }
        if (cell != null) {
            return parseComment(cell)
        }
    } else {
        for (msg in transaction.out_msgs.nodes()) {
            cell = msg.second.body.x
            if  (cell == null) {
                cell = msg.second.body.y
            }
            if (cell != null) {
                return parseComment(cell)
            }
        }
    }
    return null
}

fun getTransactionAddress(transaction: Transaction) : String {
    val default = "..."

    val intMsgInfo = transaction.in_msg.value?.info as? IntMsgInfo
    if (intMsgInfo != null) {
        val address = intMsgInfo.src as? AddrStd
        return address?.toString(userFriendly = true) ?: default
    }

    if (!transaction.out_msgs.nodes().none()) {
        val msg = transaction.out_msgs.nodes().first().second.info as? IntMsgInfo
        return if (msg == null) {
            val extMsg = transaction.out_msgs.nodes().first().second.info as? ExtInMsgInfo
            val dest = extMsg?.dest as? AddrStd
            dest?.toString(userFriendly = true) ?: default
        } else {
            val dest = msg.dest as? AddrStd
            dest?.toString(userFriendly = true) ?: default
        }
    }
    return default
}

fun getTransactionAmount(transaction: Transaction) : String {
    val default = "0.000000000"

    val intMsgInfo = transaction.in_msg.value?.info as? IntMsgInfo
    if (intMsgInfo != null) {
        return intMsgInfo.value.coins.toString()
    }

    if (!transaction.out_msgs.nodes().none()) {
        val msg = transaction.out_msgs.nodes().first().second.info as? IntMsgInfo

        return if (msg == null) {
            val extMsg = transaction.out_msgs.nodes().first().second.info as? ExtInMsgInfo
            extMsg?.import_fee.toString()
        } else {
            msg.value.coins.toString()
        }
    }
    return default
}
