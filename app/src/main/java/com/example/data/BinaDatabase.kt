package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AssetEntity::class,
        TransactionEntity::class,
        MultiSigProposalEntity::class,
        TradingBotEntity::class,
        TradeOrderEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BinaDatabase : RoomDatabase() {
    abstract fun binaDao(): BinaDao

    companion object {
        @Volatile
        private var INSTANCE: BinaDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): BinaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BinaDatabase::class.java,
                    "bina_crypto_database"
                )
                    .addCallback(BinaDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class BinaDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.binaDao())
                }
            }
        }

        private suspend fun populateInitialData(dao: BinaDao) {
            // Seed Assets
            val initialAssets = listOf(
                AssetEntity(
                    id = "myr",
                    symbol = "MYR",
                    name = "Ringgit Malaysia",
                    priceUsd = 1.0,
                    change24h = 0.0,
                    balance = 35000.0,
                    networkName = "DUITNOW_FPX",
                    hexColor = 0xFF0052CC,
                    sparklineCsv = "1.0,1.0,1.0,1.0,1.0,1.0,1.0"
                ),
                AssetEntity(
                    id = "btc",
                    symbol = "BTC",
                    name = "Bitcoin",
                    priceUsd = 421950.0,
                    change24h = 3.42,
                    balance = 0.425,
                    networkName = "BITCOIN",
                    hexColor = 0xFFF7931A,
                    sparklineCsv = "405000,410000,408000,415000,414000,419000,421950"
                ),
                AssetEntity(
                    id = "bch",
                    symbol = "BCH",
                    name = "Bitcoin Cash",
                    priceUsd = 2160.0,
                    change24h = 4.15,
                    balance = 8.50,
                    networkName = "BITCOIN_CASH",
                    hexColor = 0xFF0AC18E,
                    sparklineCsv = "2040,2070,2100,2080,2120,2150,2160"
                ),
                AssetEntity(
                    id = "sol",
                    symbol = "SOL",
                    name = "Solana",
                    priceUsd = 971.80,
                    change24h = 5.80,
                    balance = 48.0,
                    networkName = "SOLANA",
                    hexColor = 0xFF14F195,
                    sparklineCsv = "880,910,898,935,945,958,971.8"
                ),
                AssetEntity(
                    id = "xmr",
                    symbol = "XMR",
                    name = "Monero",
                    priceUsd = 768.10,
                    change24h = 2.90,
                    balance = 16.20,
                    networkName = "MONERO",
                    hexColor = 0xFFFF6600,
                    sparklineCsv = "735,748,740,755,750,762,768.1"
                ),
                AssetEntity(
                    id = "ltc",
                    symbol = "LTC",
                    name = "Litecoin",
                    priceUsd = 481.70,
                    change24h = 3.80,
                    balance = 35.0,
                    networkName = "LITECOIN",
                    hexColor = 0xFF345D9D,
                    sparklineCsv = "450,458,454,467,472,476,481.7"
                ),
                AssetEntity(
                    id = "eth",
                    symbol = "ETH",
                    name = "Ethereum",
                    priceUsd = 15355.0,
                    change24h = 2.15,
                    balance = 3.65,
                    networkName = "ETHEREUM",
                    hexColor = 0xFF627EEA,
                    sparklineCsv = "14750,14950,14860,15150,15100,15260,15355"
                ),
                AssetEntity(
                    id = "rcr",
                    symbol = "RCR",
                    name = "RCrypto Token",
                    priceUsd = 66.10,
                    change24h = 12.45,
                    balance = 2500.0,
                    networkName = "BINA_L2",
                    hexColor = 0xFF00F0A0,
                    sparklineCsv = "57.0,58.8,58.3,61.8,63.2,64.1,66.1"
                ),
                AssetEntity(
                    id = "usdt",
                    symbol = "USDT",
                    name = "Tether USD",
                    priceUsd = 4.45,
                    change24h = 0.02,
                    balance = 12000.0,
                    networkName = "ETHEREUM",
                    hexColor = 0xFF26A17B,
                    sparklineCsv = "4.45,4.45,4.45,4.45,4.45,4.45,4.45"
                )
            )
            dao.insertAssets(initialAssets)

            // Seed Initial Transactions
            val initialTxs = listOf(
                TransactionEntity(
                    id = "tx_101",
                    type = "RECEIVE",
                    assetSymbol = "BTC",
                    amount = 0.15,
                    amountUsd = 14223.0,
                    feeUsd = 2.10,
                    txHash = "0x9f8c...3b1d4e",
                    timestamp = System.currentTimeMillis() - 3600000 * 2,
                    status = "CONFIRMED",
                    destinationAddress = "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq",
                    note = "External deposit from Coldcard",
                    cosignersSigned = 1,
                    requiredSignatures = 1
                ),
                TransactionEntity(
                    id = "tx_102",
                    type = "SWAP",
                    assetSymbol = "SOL",
                    amount = 15.0,
                    amountUsd = 3276.0,
                    feeUsd = 0.35,
                    txHash = "5KqX...mP9zL",
                    timestamp = System.currentTimeMillis() - 3600000 * 7,
                    status = "CONFIRMED",
                    destinationAddress = "Bina Smart Router (Pool v3)",
                    note = "Swapped 3276 USDT for 15.0 SOL",
                    cosignersSigned = 1,
                    requiredSignatures = 1
                ),
                TransactionEntity(
                    id = "tx_103",
                    type = "BOT_TRADE",
                    assetSymbol = "BTC",
                    amount = 0.025,
                    amountUsd = 2370.5,
                    feeUsd = 0.50,
                    txHash = "0x3a4b...e8f9c1",
                    timestamp = System.currentTimeMillis() - 3600000 * 14,
                    status = "CONFIRMED",
                    destinationAddress = "Bina Central Limit Order Book",
                    note = "AI Grid Bot take-profit execution",
                    cosignersSigned = 2,
                    requiredSignatures = 2
                )
            )
            initialTxs.forEach { dao.insertTransaction(it) }

            // Seed Multi-Sig Proposal (2 of 3 required)
            val initialProposal = MultiSigProposalEntity(
                id = "prop_treasury_01",
                walletId = "vault_primary",
                title = "Treasury Reserve Yield Allocation",
                type = "TRANSFER",
                assetSymbol = "ETH",
                amount = 2.5,
                amountUsd = 8626.25,
                destinationAddress = "0x4b7260d7f33a64291165fc7a391104e4f9b88e1a",
                requiredSignatures = 2,
                signaturesJson = "[{\"cosignerId\":\"cosigner_1\",\"cosignerName\":\"Pixel 9 Pro (This Device)\",\"signedAt\":${System.currentTimeMillis() - 7200000},\"signatureHash\":\"0x88f2...e391\"}]",
                status = "PENDING_APPROVAL",
                createdAt = System.currentTimeMillis() - 7200000,
                reason = "Allocation to Staked ETH Validator - requires 2nd Co-signer confirmation"
            )
            dao.insertProposal(initialProposal)

            // Seed AI Trading Bots
            val initialBots = listOf(
                TradingBotEntity(
                    id = "bot_alpha_grid",
                    name = "Bina Grid Sentinel",
                    pairSymbol = "BTC/USDT",
                    strategyTypeName = "GRID_TRADING",
                    statusName = "RUNNING",
                    allocatedCapitalUsd = 8000.0,
                    currentBalanceUsd = 9245.80,
                    totalProfitUsd = 1245.80,
                    profitPercent = 15.57,
                    totalTrades = 46,
                    winRatePercent = 89.1,
                    targetRsiOversold = 28,
                    targetRsiOverbought = 72,
                    stopLossPercent = 3.0,
                    takeProfitPercent = 5.5,
                    requiresMultiSigApproval = false,
                    logsJson = "BUY 0.025 BTC at $93,420 (Lower Grid Fill)|SELL 0.025 BTC at $94,650 (+$30.75 profit)|BUY 0.025 BTC at $93,900|SELL 0.025 BTC at $94,800 (+$22.50 profit)"
                ),
                TradingBotEntity(
                    id = "bot_rsi_momentum",
                    name = "DeepAlpha RSI/MACD",
                    pairSymbol = "SOL/USDT",
                    strategyTypeName = "RSI_MACD_MOMENTUM",
                    statusName = "RUNNING",
                    allocatedCapitalUsd = 4500.0,
                    currentBalanceUsd = 5320.10,
                    totalProfitUsd = 820.10,
                    profitPercent = 18.22,
                    totalTrades = 29,
                    winRatePercent = 79.3,
                    targetRsiOversold = 30,
                    targetRsiOverbought = 70,
                    stopLossPercent = 4.0,
                    takeProfitPercent = 8.0,
                    requiresMultiSigApproval = true,
                    logsJson = "RSI hit 27.8 -> Signal Detected|BUY 12.0 SOL at $208.50|MACD Bullish Cross Confirmed|SELL 12.0 SOL at $217.80 (+$111.60 profit)"
                ),
                TradingBotEntity(
                    id = "bot_dca_smart",
                    name = "Smart Dip DCA Shield",
                    pairSymbol = "ETH/USDT",
                    strategyTypeName = "DCA_SMART_DIP",
                    statusName = "RUNNING",
                    allocatedCapitalUsd = 5000.0,
                    currentBalanceUsd = 5480.0,
                    totalProfitUsd = 480.0,
                    profitPercent = 9.60,
                    totalTrades = 18,
                    winRatePercent = 83.3,
                    targetRsiOversold = 32,
                    targetRsiOverbought = 68,
                    stopLossPercent = 5.0,
                    takeProfitPercent = 7.0,
                    requiresMultiSigApproval = false,
                    logsJson = "ETH Dip 2.8% below 20-EMA -> Triggered Accumulation|BUY 0.65 ETH at $3,380|Trailing stop updated to $3,420"
                )
            )
            initialBots.forEach { dao.insertBot(it) }

            // Seed Initial Trade Orders
            val initialOrders = listOf(
                TradeOrderEntity(
                    id = "ord_01",
                    pairSymbol = "BTC/USDT",
                    side = "BUY",
                    type = "LIMIT",
                    price = 93500.0,
                    amount = 0.05,
                    filledAmount = 0.0,
                    status = "OPEN",
                    timestamp = System.currentTimeMillis() - 1800000,
                    isBotOrder = true
                ),
                TradeOrderEntity(
                    id = "ord_02",
                    pairSymbol = "BTC/USDT",
                    side = "SELL",
                    type = "LIMIT",
                    price = 96000.0,
                    amount = 0.03,
                    filledAmount = 0.0,
                    status = "OPEN",
                    timestamp = System.currentTimeMillis() - 3600000,
                    isBotOrder = false
                )
            )
            initialOrders.forEach { dao.insertOrder(it) }
        }
    }
}
