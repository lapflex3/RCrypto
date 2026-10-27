package com.example.model

import java.util.UUID

enum class AssetNetwork(val displayName: String) {
    BITCOIN("Bitcoin L1"),
    BITCOIN_CASH("Bitcoin Cash L1"),
    SOLANA("Solana (SPL)"),
    MONERO("Monero Privacy Net"),
    LITECOIN("Litecoin L1"),
    ETHEREUM("Ethereum (ERC-20)"),
    BNB_CHAIN("BNB Smart Chain (BEP-20)"),
    BINA_L2("RCrypto zkRollup L2"),
    DUITNOW_FPX("DuitNow / FPX (MYR)")
}

data class CryptoAsset(
    val id: String,
    val symbol: String,
    val name: String,
    val priceUsd: Double, // Stores value in MYR as primary base
    val change24h: Double,
    val balance: Double,
    val network: AssetNetwork,
    val hexColor: Long,
    val sparkline: List<Double>
) {
    val balanceUsd: Double get() = balance * priceUsd
    val balanceMyr: Double get() = balance * priceUsd
    val priceMyr: Double get() = priceUsd
}

enum class TransactionType {
    SEND,
    RECEIVE,
    SWAP,
    BOT_TRADE
}

enum class TransactionStatus {
    CONFIRMED,
    PENDING_MULTISIG,
    REJECTED
}

data class WalletTransaction(
    val id: String = UUID.randomUUID().toString(),
    val type: TransactionType,
    val assetSymbol: String,
    val amount: Double,
    val amountUsd: Double,
    val feeUsd: Double = 0.85,
    val txHash: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: TransactionStatus = TransactionStatus.CONFIRMED,
    val destinationAddress: String,
    val note: String = "",
    val cosignersSigned: Int = 1,
    val requiredSignatures: Int = 1
)

enum class CosignerRole {
    PRIMARY_DEVICE,
    HARDWARE_KEY,
    RECOVERY_GUARDIAN,
    INSTITUTIONAL_ORACLE
}

data class Cosigner(
    val id: String,
    val name: String,
    val role: CosignerRole,
    val publicKey: String,
    val isLocalDevice: Boolean = false,
    val isActive: Boolean = true
)

enum class ProposalType {
    TRANSFER,
    EXCHANGE_SWAP,
    BOT_MARGIN_ALLOCATION,
    SECURITY_POLICY_UPDATE
}

enum class ProposalStatus {
    PENDING_APPROVAL,
    READY_TO_BROADCAST,
    EXECUTED,
    REJECTED
}

data class SignatureRecord(
    val cosignerId: String,
    val cosignerName: String,
    val signedAt: Long = System.currentTimeMillis(),
    val signatureHash: String
)

data class MultiSigProposal(
    val id: String = UUID.randomUUID().toString(),
    val walletId: String,
    val title: String,
    val type: ProposalType,
    val assetSymbol: String,
    val amount: Double,
    val amountUsd: Double,
    val destinationAddress: String,
    val requiredSignatures: Int,
    val signatures: List<SignatureRecord> = emptyList(),
    val status: ProposalStatus = ProposalStatus.PENDING_APPROVAL,
    val createdAt: Long = System.currentTimeMillis(),
    val reason: String = "Automated risk protocol verification"
) {
    val currentSignatures: Int get() = signatures.size
    val isQuorumMet: Boolean get() = currentSignatures >= requiredSignatures
}

data class MultiSigVault(
    val id: String = "vault_primary",
    val name: String = "Bina Treasury Multi-Sig",
    val vaultAddress: String = "0x7F1a94C381E885B49dF36816912401C95e0c52B0",
    val requiredSignatures: Int = 2,
    val totalSigners: Int = 3,
    val cosigners: List<Cosigner> = emptyList(),
    val dailyLimitUsd: Double = 50000.0,
    val spentTodayUsd: Double = 8400.0,
    val multiSigThresholdUsd: Double = 2500.0
)

data class CandleStick(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double
)

data class OrderBookItem(
    val price: Double,
    val amount: Double,
    val total: Double,
    val depthPercentage: Float
)

enum class OrderSide {
    BUY,
    SELL
}

enum class OrderType {
    LIMIT,
    MARKET,
    STOP_LOSS
}

data class TradeOrder(
    val id: String = UUID.randomUUID().toString(),
    val pairSymbol: String,
    val side: OrderSide,
    val type: OrderType,
    val price: Double,
    val amount: Double,
    val filledAmount: Double,
    val status: String = "OPEN",
    val timestamp: Long = System.currentTimeMillis(),
    val isBotOrder: Boolean = false
)

enum class BotStrategyType(val title: String, val description: String) {
    RSI_MACD_MOMENTUM("RSI & MACD Momentum", "Executes scalp entries on oversold RSI (<30) with bullish MACD crossover confirmations"),
    GRID_TRADING("Dynamic Grid Trading", "Automates continuous laddered buy-low and sell-high orders across a volatility band"),
    VOLATILITY_BREAKOUT("Bollinger Breakout", "Catches high-momentum price expansions when closing outside 2.0σ Bollinger Bands"),
    DCA_SMART_DIP("Smart DCA Dip Buyer", "Systematically accumulates on algorithmic price dips with risk multiplier throttling")
}

enum class BotStatus {
    RUNNING,
    PAUSED,
    STOPPED
}

data class BotExecutionLog(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val action: String, // e.g. "BUY_FILLED", "SELL_FILLED", "SIGNAL_DETECTED"
    val pair: String,
    val price: Double,
    val amount: Double,
    val details: String,
    val pnlUsd: Double = 0.0
)

data class AiTradingBot(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val pairSymbol: String,
    val strategyType: BotStrategyType,
    val status: BotStatus = BotStatus.RUNNING,
    val allocatedCapitalUsd: Double,
    val currentBalanceUsd: Double,
    val totalProfitUsd: Double,
    val profitPercent: Double,
    val totalTrades: Int,
    val winRatePercent: Double,
    val targetRsiOversold: Int = 30,
    val targetRsiOverbought: Int = 70,
    val stopLossPercent: Double = 3.5,
    val takeProfitPercent: Double = 6.0,
    val requiresMultiSigApproval: Boolean = false,
    val logs: List<BotExecutionLog> = emptyList()
)

data class AiMarketInsight(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val summary: String,
    val sentiment: String, // "BULLISH", "BEARISH", "NEUTRAL"
    val confidence: Int, // 0 - 100
    val impactAsset: String,
    val category: String, // "TECHNICAL", "ON_CHAIN", "MACRO", "WHALE"
    val timestamp: Long = System.currentTimeMillis()
)

data class PortfolioRiskAudit(
    val overallRiskScore: Int, // 0 - 100 (e.g. 34 is Moderate/Safe)
    val riskRatingLabel: String, // "LOW RISK", "MODERATE RISK", "ELEVATED RISK"
    val concentrationRisk: Int, // 0 - 100
    val volatilityExposure: Int, // 0 - 100
    val smartContractRisk: Int, // 0 - 100
    val multiSigSecurityCoverage: Int, // 0 - 100
    val recommendations: List<String>,
    val auditSummary: String
)

data class PredictivePriceModel(
    val coinSymbol: String,
    val coinName: String,
    val currentPrice: Double,
    val targetBullPrice: Double,
    val targetBearPrice: Double,
    val bullProbability: Int, // e.g. 74%
    val timeHorizon: String, // "24h - 72h"
    val confidenceScore: Int, // e.g. 88%
    val trendVerdict: String,
    val keyDrivers: List<String>
)

data class SocialSentimentItem(
    val id: String = UUID.randomUUID().toString(),
    val source: String,
    val headline: String,
    val sentiment: String,
    val engagement: String,
    val timeAgo: String
)

