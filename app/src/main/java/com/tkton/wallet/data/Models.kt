package com.tkton.wallet.data

import androidx.room.*
import androidx.room.ForeignKey.CASCADE

enum class SettingType {
    CURRENT_WALLET_ADDRESS, TESTNET_CONFIG, MAINNET_CONFIG, TEST_MODE
}

@Entity(tableName = "settings")
data class Setting(
    @PrimaryKey var type: SettingType,
    @ColumnInfo var value: String
)

@Entity(
    tableName = "wallets",
    foreignKeys = [
        ForeignKey(
            entity = Words::class,
            parentColumns = ["wordsId"],
            childColumns = ["words"],
            onDelete = CASCADE) ],
    indices = [ Index("words") ]
)
data class Wallet(
    @PrimaryKey(autoGenerate = true) var walletId: Long = 0,
    @ColumnInfo(name = "words") var words : Long? = null,
    @ColumnInfo(name = "workchain") var workchain: Int? = null,
    @ColumnInfo(name = "address") var address: String? = null,
    @ColumnInfo(name = "version") var version: String? = null,
    @ColumnInfo(name = "private_key") var privateKey: ByteArray? = null,
    @ColumnInfo(name = "public_key") var publicKey: ByteArray? = null,
    @ColumnInfo(name = "last_balance") var lastBalance: String? = null,
    @ColumnInfo(name = "account_state") var accountState: String? = null
)

class DataConverter {
    @TypeConverter
    fun fromArrayString(words: Array<String>): String {
        return words.joinToString(separator = "|")
    }

    @TypeConverter
    fun toArrayString(data: String) : Array<String> {
        return data.split("|").toTypedArray()
    }
}

@Entity(tableName = "words")
@TypeConverters(DataConverter::class)
data class Words(
    @PrimaryKey(autoGenerate = true) var wordsId: Long = 0,
    @ColumnInfo(name = "words_list") var wordsList : Array<String>
)

@Entity(tableName = "raw_transactions")
data class RawTransaction(
    @PrimaryKey(autoGenerate = true) var lt: Long = 0,
    @ColumnInfo var hash : ByteArray
)