package com.tkton.wallet.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE type = :settingType")
    fun findByType(settingType: SettingType): Setting?

    @Query("SELECT * FROM settings WHERE type = :settingType")
    fun findByTypeLive(settingType: SettingType): LiveData<Setting?>

    @Query("SELECT value FROM settings WHERE type = :settingType")
    fun getModeLive(settingType: SettingType = SettingType.TEST_MODE): LiveData<String?>

    @Query("SELECT value FROM settings WHERE type = :settingType")
    fun getMode(settingType: SettingType = SettingType.TEST_MODE): String?

    @Query("SELECT value FROM settings WHERE type = :settingType")
    fun getCurrentWalletLive(settingType: SettingType = SettingType.CURRENT_WALLET_ADDRESS): LiveData<String?>

    @Query("SELECT value FROM settings WHERE type = :settingType")
    fun getCurrentWallet(settingType: SettingType = SettingType.CURRENT_WALLET_ADDRESS): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(setting: Setting)

    @Query("DELETE FROM settings WHERE type = :settingType")
    suspend fun delete(settingType: SettingType)
}

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallets")
    fun getAll(): List<Wallet>?

    @Query("SELECT * FROM wallets")
    fun getWallets(): LiveData<List<Wallet>?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wallet: Wallet) : Long

    @Update
    suspend fun update(wallet: Wallet)

    @Delete
    suspend fun delete(wallet: Wallet)

    @Query("SELECT * FROM wallets WHERE address = :address")
    fun getByAddress(address : String) : Wallet?

    @Query("SELECT * FROM wallets WHERE address = :address")
    fun getByAddressLive(address : String) : LiveData<Wallet?>

    @Query("SELECT * FROM wallets " +
            "WHERE address = (SELECT value FROM settings WHERE type = 'CURRENT_WALLET_ADDRESS')")
    fun getCurrentWalletLive(): LiveData<Wallet?>

    @Query("SELECT last_balance FROM wallets " +
            "WHERE address = (SELECT value FROM settings WHERE type = 'CURRENT_WALLET_ADDRESS')")
    fun getCurrentWalletBalanceLive(): LiveData<String?>

    @Query("SELECT account_state FROM wallets " +
            "WHERE address = (SELECT value FROM settings WHERE type = 'CURRENT_WALLET_ADDRESS')")
    fun getCurrentWalletStateLive(): LiveData<String?>

    @Query("SELECT address FROM wallets " +
            "WHERE address = (SELECT value FROM settings WHERE type = 'CURRENT_WALLET_ADDRESS')")
    fun getCurrentWalletAddressLive(): LiveData<String?>

    @Query("SELECT * FROM wallets " +
            "WHERE address = (SELECT value FROM settings WHERE type = 'CURRENT_WALLET_ADDRESS')")
    fun getCurrentWallet(): Wallet?

    @Query("DELETE FROM wallets")
    suspend fun clean()
}

@Dao
interface WordsDao{
    @Query("SELECT * FROM words")
    fun getAll(): List<Words>?

    @Query("SELECT * FROM words ORDER BY wordsId DESC LIMIT 1")
    fun getLast(): LiveData<Words?>

    @Query("SELECT * FROM words " +
            "WHERE words_list == :wordsList")
    fun find(wordsList: String) : Words?

    @Query("SELECT * FROM words " +
            "WHERE wordsId == :wordsId")
    fun findById(wordsId: Long) : Words?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(word: Words) : Long

    @Query("DELETE FROM words")
    fun clean()
}


@Dao
interface RawTransactionsDao {
    @Query("SELECT * FROM raw_transactions ORDER BY lt DESC")
    fun getAllLive(): LiveData<List<RawTransaction>?>

    @Query("SELECT * FROM raw_transactions ORDER BY lt DESC LIMIT 1")
    fun getFirst(): RawTransaction?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: RawTransaction)

    @Query("DELETE FROM raw_transactions")
    suspend fun clean()
}