package com.tkton.wallet.data

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.room.*
import com.tkton.wallet.tonops.AppLiteClient
import kotlinx.coroutines.delay
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import org.ton.block.AccountInfo
import org.ton.block.Transaction
import org.ton.boc.BagOfCells
import org.ton.mnemonic.Mnemonic
import java.net.URL


@Database(entities = [ Setting::class, Words::class, Wallet::class, RawTransaction::class ],
    version = 1,
    exportSchema = false)
abstract class DataManager : RoomDatabase() {
    abstract fun settings(): SettingsDao
    abstract fun wallets(): WalletDao
    abstract fun words(): WordsDao
    abstract fun transactions(): RawTransactionsDao

    companion object {

        @Volatile
        private var INSTANCE: DataManager? = null

        fun getInstance(context: Context): DataManager {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    val builder = Room.databaseBuilder(
                        context.applicationContext,
                        DataManager::class.java,
                        "tktonwallet.db"
                    )
                    val phrase = Build.BRAND + Build.MANUFACTURER
                    val factory = SupportFactory(SQLiteDatabase.getBytes(phrase.toCharArray()))
                    builder.openHelperFactory(factory)
                    instance = builder.build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }

    fun loadLiteClientConfig(testnet: Boolean = false) : String {
        if (testnet) {
            return URL("https://ton-blockchain.github.io/testnet-global.config.json").readText()
        } else {
            return URL("https://ton.org/global-config.json").readText()
        }
    }

    suspend fun updateLiteClientConfig() {
        var attempts = 10
        while (attempts > 0) {
            try {
                val testnetConfig = loadLiteClientConfig(testnet = true)
                val mainnetConfig = loadLiteClientConfig(testnet = false)
                settings().insert(Setting(SettingType.TESTNET_CONFIG, testnetConfig))
                settings().insert(Setting(SettingType.MAINNET_CONFIG, mainnetConfig))
                break
            } catch (e: Exception) {
                attempts -= 1
                delay(300)
            }
        }
    }

    fun getLiteClientConfig(testnet : Boolean = false) : String? {
        if (testnet) {
            return settings().findByType(SettingType.TESTNET_CONFIG)?.value
        } else {
            return settings().findByType(SettingType.MAINNET_CONFIG)?.value
        }
    }

    suspend fun getLiteClient() : AppLiteClient {
        val isInTestMode = settings().getMode() ?: "N"
        var config : String?
        if (isInTestMode == "Y") {
            config = getLiteClientConfig(testnet = true)
            if (config == null) { updateLiteClientConfig() }

        } else {
            config = getLiteClientConfig()
            if (config == null) { updateLiteClientConfig() }
        }
        return AppLiteClient(config ?: throw java.lang.IllegalArgumentException("no lite client config"))
    }

    suspend fun setCurrentWallet(wallet: Wallet) {
        val address = wallet.address
        if (address == null) { throw java.lang.IllegalArgumentException("no wallet address") }
        val setting = Setting(SettingType.CURRENT_WALLET_ADDRESS, address)
        settings().insert(setting)
    }

    fun findWalletWithBalance() : Wallet {
        val wallets = wallets().getAll()
        if (wallets == null) { throw IllegalArgumentException("no wallets") }
        var currentWallet = wallets.first()
        var biggestBalance = currentWallet.lastBalance?.toDouble() ?: 0.0

        for (wallet in wallets) {
            if (biggestBalance < (wallet.lastBalance?.toDouble() ?: 0.0)) {
                biggestBalance = wallet.lastBalance?.toDouble() ?: 0.0
                currentWallet = wallet
            }
        }
        return currentWallet
    }

    suspend fun changeToWalletWithBiggestBalance() {
        setCurrentWallet(findWalletWithBalance())
    }

    suspend fun changeCurrentWallet(wallet: Wallet) {
        transactions().clean()
        setCurrentWallet(wallet)
        updateData()
    }

    suspend fun createNewWallet(words: Array<String>?) {
        if (words != null) {
            val keyPair = Mnemonic.toKeyPair(words)
            val wallet = getLiteClient().setUpWallets("v4r2", keyPair)
            saveWallet(wallet, words)
            setCurrentWallet(wallet)
        }
    }

    suspend fun deployWallet(address: String?) {
        if (address != null) {
            val wallet = wallets().getByAddress(address)
            if (wallet != null) {
                getLiteClient().deployContract(wallet)
            }
        }
    }

    suspend fun loadWallets(words: Array<String>?) {
        if (words != null) {
            val keyPair = Mnemonic.toKeyPair(words)
            for (contractClass in listOf("v3r1", "v4r2", "v3r2")) {
                val wallet = getLiteClient().setUpWallets(contractClass, keyPair)
                saveWallet(wallet, words)
            }
            updateAccountState()
            changeToWalletWithBiggestBalance()
            loadTransactions()
        }
    }

    suspend fun saveWallet(wallet: Wallet, words: Array<String>) {
        val existingWords = words().find(words.joinToString(separator = "|"))
        val wordsId = existingWords?.wordsId ?: words().insert(Words(wordsList = words))
        wallet.words = wordsId
        wallets().insert(wallet)
    }

    suspend fun updateTransactions()  {
        val wallet = wallets().getCurrentWallet() ?: return
        val lastDbTr = transactions().getFirst()

        if (lastDbTr == null) {
            loadTransactions()
            return
        }

        val address = wallet.address ?: return
        var transactions : List<Transaction>? = null

        var attempts = 1
        while (attempts < 10) {
            try {
                transactions = getLiteClient().getNewTransactions(address, lastDbTr.lt)
                break
            } catch (e: Exception) {
                delay(500)
                attempts++
            }
        }
        insertTransactions(transactions ?: emptyList())
    }

    suspend fun loadTransactions()  {
        val wallet = wallets().getCurrentWallet() ?: return
        val address = wallet.address ?: return

        var transactions : List<Transaction> = emptyList()

        var attempts = 1
        while (attempts < 10) {
            try {
                transactions = getLiteClient().loadAllTransactions(address)
                break
            } catch (e: Exception) {
                delay(500)
                attempts++
            }
        }
        insertTransactions(transactions)
    }

    suspend fun insertTransactions(transactions : List<Transaction>) {
        for (tr in transactions) {
            transactions().insert(RawTransaction(lt=tr.lt.toLong(), hash=BagOfCells(tr.toCell()).toByteArray()))
        }
    }

    suspend fun getAccountInfo(address: String) : AccountInfo? {
        var accountInfo : AccountInfo? = null
        var attempts = 1
        while (attempts < 10) {
            try {
                accountInfo = getLiteClient().getAccountInfo(address)
                break
            } catch (e: Exception) {
                delay(500)
                attempts++
            }
        }
        return accountInfo
    }

    suspend fun updateAccountState() {
        val wallet = wallets().getCurrentWallet() ?: return
        val address = wallet.address ?: return
        val accountInfo = getAccountInfo(address)

        if (accountInfo != null) {
            val balance = accountInfo.storage.balance.coins.toString()
            val accountState = accountInfo.storage.state.toString()
            wallet.lastBalance = balance
            wallet.accountState = accountState
            wallets().update(wallet)
            deployIfUninit(address)
        }
    }

    suspend fun transfer(destinationAddress : String, amount : String, comment : String?) {
        val wallet = wallets().getCurrentWallet() ?: return
        deployIfUninit(wallet.address)
        getLiteClient().transfer(wallet, destinationAddress, amount.toDouble(), comment)
    }

    suspend fun deployIfUninit(address: String?) {
        if (address != null) {
            val wallet = wallets().getByAddress(address) ?: return
            val balance = wallet.lastBalance ?: "0.0"
            if (wallet.accountState == "account_uninit" && balance.toDouble() >= 0.02) {
                deployWallet(wallet.address)
            }
        }
    }

    suspend fun updateData() {
        updateTransactions()
        updateAccountState()
    }

    suspend fun setToTestMode() {
        clean()
        settings().insert(Setting(type = SettingType.TEST_MODE, value = "Y"))
        settings().delete(SettingType.CURRENT_WALLET_ADDRESS)
    }

    suspend fun setToNormalMode() {
        clean()
        settings().insert(Setting(type = SettingType.TEST_MODE, value = "N"))
        settings().delete(SettingType.CURRENT_WALLET_ADDRESS)
    }

    suspend fun clean() {
        transactions().clean()
        words().clean()
        wallets().clean()
    }
}