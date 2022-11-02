package com.tkton.wallet.tonops

import android.util.Log
import com.tkton.wallet.data.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.ton.api.liteclient.config.LiteClientConfigGlobal
import org.ton.api.pk.PrivateKeyEd25519
import org.ton.block.*
import org.ton.boc.BagOfCells
import org.ton.contract.wallet.SeqnoContract
import org.ton.contract.wallet.WalletContract
import org.ton.contract.wallet.v3.ContractV3R2
import org.ton.contract.wallet.v4.ContractV4R2
import org.ton.contract.wallet.v3.ContractV3R1
import org.ton.lite.api.liteserver.LiteServerAccountId
import org.ton.lite.api.liteserver.LiteServerTransactionList
import org.ton.lite.client.LiteClient
import org.ton.tlb.parse

class AppLiteClient(config: String) {
    val liteClient : LiteClient
    var liteClientConfig : LiteClientConfigGlobal

    init {
        liteClientConfig = Json{ ignoreUnknownKeys = true }.decodeFromString(string=config)
        liteClient = LiteClient(liteClientConfig)
    }

    suspend fun getAccountInfo(address: String) : AccountInfo? {
        return liteClient.getAccount(address)
    }

    private suspend fun getLastTransactionLt(address: String) : Long {
        val accountInfo = liteClient.getAccount(address)
        val lastTransTt = accountInfo?.storage?.last_trans_lt
        return lastTransTt?.toLong() ?: 0
    }

    private suspend fun getLastTransactionLtHash(address: String) : Pair<Long, ByteArray>? {
        val account = LiteServerAccountId(address)
        val lastBlockId = liteClient.getLastBlockId()
        val accountState = liteClient.liteApi.getAccountState(lastBlockId, account)
        val trBlockHeader = liteClient.liteApi.lookupBlockByLt(accountState.shard_blk, getLastTransactionLt(address))
        val trBlock = liteClient.liteApi.getBlock(trBlockHeader.id)
        val accountBlocks = trBlock.toBlock().extra.account_blocks
        for (node in accountBlocks.nodes()) {
            if (node.first.account_addr == account.toMsgAddressIntStd().address) {
                val blocks = node.first.transactions
                for (tr in blocks.nodes()) {
                    return Pair(tr.first.lt.toLong(), tr.first.hash())
                }
            }
        }
        return null
    }

    private suspend fun load10transactions(
        address: String,
        lt: Long,
        hash: ByteArray
    ) : LiteServerTransactionList
    {
        return liteClient.liteApi.getTransactions(10, LiteServerAccountId(address), lt, hash)
    }

    suspend fun loadAllTransactions(address: String): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val lastTr = getLastTransactionLtHash(address)
        var lt = lastTr?.first ?: return transactions
        var hash = lastTr.second

        var attempts = 0
        while (true) {
            try {
                val trs = load10transactions(address, lt, hash)
                val cells = BagOfCells(trs.transactions).roots
                var currentTransaction : Transaction? = null
                for (cell in cells) {
                    currentTransaction = cell.parse(Transaction)
                    transactions.add(currentTransaction)
                }
                if (currentTransaction != null) {
                    lt = currentTransaction.prev_trans_lt.toLong()
                    hash = currentTransaction.prev_trans_hash.toByteArray()
                    if (lt.toInt() == 0) { break }
                }
            } catch (e: Exception) {
                Log.e("loadAllTransactions $attempts", e.toString())
                attempts++
                if (attempts > 5) { break }
            }
        }
        return transactions
    }

    suspend fun getNewTransactions(address: String, lastSeenTrLt : Long): List<Transaction>? {
        val transactions = mutableListOf<Transaction>()
        val lastTr = getLastTransactionLtHash(address)
        var lt = lastTr?.first ?: return null
        var hash = lastTr.second
        while (true) {
            val trs = liteClient.liteApi.getTransactions(10, LiteServerAccountId(address), lt, hash)
            val cells = BagOfCells(trs.transactions).roots
            for (cell in cells) {
                val current = cell.parse(Transaction)

                if (current.lt.toLong() == lastSeenTrLt) {
                    return transactions
                }

                transactions.add(current)
                lt = current.prev_trans_lt.toLong()
                hash = current.prev_trans_hash.toByteArray()
            }
        }

    }

    fun setUpWallets(contractClass: String, keyPair: Pair<ByteArray, ByteArray>) : Wallet {
        val wallet = Wallet()
        wallet.publicKey = keyPair.first
        wallet.privateKey = keyPair.second
        when (contractClass) {
            "v3r1" -> {
                val walletContract = ContractV3R1(liteClient.liteApi, PrivateKeyEd25519.of(keyPair.second))
                wallet.address = walletContract.address().toString(userFriendly = true)
                wallet.version = walletContract.name
            }
            "v4r2" -> {
                val walletContract = ContractV4R2(liteClient.liteApi, PrivateKeyEd25519.of(keyPair.second))
                wallet.address = walletContract.address().toString(userFriendly = true)
                wallet.version = walletContract.name
            }
            "v3r2" -> {
                val walletContract = ContractV3R2(liteClient.liteApi, PrivateKeyEd25519.of(keyPair.second))
                wallet.address = walletContract.address().toString(userFriendly = true)
                wallet.version = walletContract.name
            }
        }

        return wallet
    }

    private fun getContractInstance(wallet: Wallet, privateKey: ByteArray) : WalletContract {
        when (wallet.version) {
            "v3r1" -> { return ContractV3R1(liteClient.liteApi, PrivateKeyEd25519.of(privateKey)) }
            "v3r2" -> { return ContractV3R2(liteClient.liteApi, PrivateKeyEd25519.of(privateKey)) }
            "v4r2" -> { return ContractV4R2(liteClient.liteApi, PrivateKeyEd25519.of(privateKey)) }
            else -> { throw IllegalArgumentException("unsupported wallet version") }
        }
    }

    suspend fun deployContract(wallet : Wallet) {
        val privateKey = wallet.privateKey
        if (privateKey != null) {
            val contract  = getContractInstance(wallet, privateKey)
            contract.deploy()
        }
    }

    suspend fun transfer(wallet: Wallet, destinationAddress : String, coins : Double, comment : String?) {
        val destAccountInfo = getAccountInfo(destinationAddress)
        val bounce = destAccountInfo != null

        val privateKey = wallet.privateKey
        if (privateKey != null) {
            val contract = getContractInstance(wallet, privateKey)
            val seqnoContract = contract as? SeqnoContract
            val seqno = seqnoContract?.seqno(liteClient.getLastBlockId())
            contract.transfer(
                bounce = bounce,
                dest = MsgAddressInt.parse(destinationAddress),
                coins = Coins.of(coins),
                comment = comment,
                seqno = seqno ?: 1
            )
        }
    }

    suspend fun loadLast10Transactions(address: String): List<Transaction>? {
        val transactions = mutableListOf<Transaction>()
        val lastTr = getLastTransactionLtHash(address)
        val lt = lastTr?.first ?: return null
        val hash = lastTr.second
        val trs = liteClient.liteApi.getTransactions(10, LiteServerAccountId(address), lt, hash)
        val cells = BagOfCells(trs.transactions).roots
        var currentTransaction : Transaction?
        for (cell in cells) {
            currentTransaction = cell.parse(Transaction)
            transactions.add(currentTransaction)
        }

        return transactions
    }
}



