package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey val id: String,
    val symbol: String,
    val name: String,
    val priceUsd: Double,
    val change24h: Double,
    val balance: Double,
    val networkName: String,
    val hexColor: Long,
    val sparklineCsv: String // e.g. "94000,94200,94100..."
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val type: String, // SEND, RECEIVE, SWAP, BOT_TRADE
    val assetSymbol: String,
    val amount: Double,
    val amountUsd: Double,
    val feeUsd: Double,
    val txHash: String,
    val timestamp: Long,
    val status: String, // CONFIRMED, PENDING_MULTISIG, REJECTED
    val destinationAddress: String,
    val note: String,
    val cosignersSigned: Int,
    val requiredSignatures: Int
)

@Entity(tableName = "multisig_proposals")
data class MultiSigProposalEntity(
    @PrimaryKey val id: String,
    val walletId: String,
    val title: String,
    val type: String,
    val assetSymbol: String,
    val amount: Double,
    val amountUsd: Double,
    val destinationAddress: String,
    val requiredSignatures: Int,
    val signaturesJson: String, // serialised cosigner signatures
    val status: String,
    val createdAt: Long,
    val reason: String
)

@Entity(tableName = "trading_bots")
data class TradingBotEntity(
    @PrimaryKey val id: String,
    val name: String,
    val pairSymbol: String,
    val strategyTypeName: String,
    val statusName: String,
    val allocatedCapitalUsd: Double,
    val currentBalanceUsd: Double,
    val totalProfitUsd: Double,
    val profitPercent: Double,
    val totalTrades: Int,
    val winRatePercent: Double,
    val targetRsiOversold: Int,
    val targetRsiOverbought: Int,
    val stopLossPercent: Double,
    val takeProfitPercent: Double,
    val requiresMultiSigApproval: Boolean,
    val logsJson: String
)

@Entity(tableName = "trade_orders")
data class TradeOrderEntity(
    @PrimaryKey val id: String,
    val pairSymbol: String,
    val side: String,
    val type: String,
    val price: Double,
    val amount: Double,
    val filledAmount: Double,
    val status: String,
    val timestamp: Long,
    val isBotOrder: Boolean
)
