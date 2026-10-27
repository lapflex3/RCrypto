package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiIntelligenceService
import com.example.ai.TechnicalAnalysis
import com.example.data.BinaDatabase
import com.example.data.BinaRepository
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

enum class AppFlowState {
    SPLASH,
    AUTH,
    MAIN
}

enum class AppScreen(val title: String) {
    WALLET("Wallet"),
    TRADE("Trade"),
    SWAP("Swap"),
    BOTS("AI Bots"),
    AI_INSIGHTS("AI Insights"),
    VAULT("Multi-Sig")
}

data class OrderBookDepth(
    val bids: List<OrderBookItem>,
    val asks: List<OrderBookItem>,
    val spreadUsd: Double,
    val spreadPercent: Double
)

class BinaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BinaRepository
    private val aiService = AiIntelligenceService()

    private val _appFlowState = MutableStateFlow(AppFlowState.SPLASH)
    val appFlowState: StateFlow<AppFlowState> = _appFlowState.asStateFlow()

    private val _userEmail = MutableStateFlow<String?>("lapflex100@gmail.com")
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    private val _userName = MutableStateFlow<String?>("Nasadef Trader")
    val userName: StateFlow<String?> = _userName.asStateFlow()

    private val _isGoogleAuth = MutableStateFlow(false)
    val isGoogleAuth: StateFlow<Boolean> = _isGoogleAuth.asStateFlow()

    private val _currentScreen = MutableStateFlow(AppScreen.WALLET)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Active Trading Pair
    private val _selectedPair = MutableStateFlow("BTC/MYR")
    val selectedPair: StateFlow<String> = _selectedPair.asStateFlow()

    // Chart Timeframe
    private val _selectedTimeframe = MutableStateFlow("15m")
    val selectedTimeframe: StateFlow<String> = _selectedTimeframe.asStateFlow()

    // Technical Indicators Toggles
    private val _showRsi = MutableStateFlow(true)
    val showRsi: StateFlow<Boolean> = _showRsi.asStateFlow()

    private val _showBollinger = MutableStateFlow(true)
    val showBollinger: StateFlow<Boolean> = _showBollinger.asStateFlow()

    private val _showMacd = MutableStateFlow(false)
    val showMacd: StateFlow<Boolean> = _showMacd.asStateFlow()

    // Live Order Book
    private val _orderBook = MutableStateFlow(OrderBookDepth(emptyList(), emptyList(), 1.5, 0.001))
    val orderBook: StateFlow<OrderBookDepth> = _orderBook.asStateFlow()

    // Live Candlesticks for active pair
    private val _candles = MutableStateFlow<List<CandleStick>>(emptyList())
    val candles: StateFlow<List<CandleStick>> = _candles.asStateFlow()

    // Technical Analysis State
    private val _technicalAnalysis = MutableStateFlow<TechnicalAnalysis?>(null)
    val technicalAnalysis: StateFlow<TechnicalAnalysis?> = _technicalAnalysis.asStateFlow()

    // AI Risk Assessment Report
    private val _riskAudit = MutableStateFlow<PortfolioRiskAudit?>(null)
    val riskAudit: StateFlow<PortfolioRiskAudit?> = _riskAudit.asStateFlow()

    // AI Chat / Query Response
    private val _aiResponse = MutableStateFlow<String?>(null)
    val aiResponse: StateFlow<String?> = _aiResponse.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // UI Feedback Banner / Snackbar message
    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    // Repository flows
    val assets: StateFlow<List<CryptoAsset>>
    val transactions: StateFlow<List<WalletTransaction>>
    val proposals: StateFlow<List<MultiSigProposal>>
    val bots: StateFlow<List<AiTradingBot>>
    val orders: StateFlow<List<TradeOrder>>
    val vault: MultiSigVault

    init {
        val database = BinaDatabase.getDatabase(application, viewModelScope)
        repository = BinaRepository(database.binaDao())
        vault = repository.defaultVault

        assets = repository.allAssetsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        transactions = repository.allTransactionsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        proposals = repository.allProposalsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        bots = repository.allBotsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        orders = repository.allOrdersFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Generate initial candles & order book in MYR
        generateCandlesForPair("BTC/MYR", 421950.0)
        generateOrderBookForPrice(421950.0)

        // Start live ticker & simulated algorithmic bot runner
        startMarketSimulation()

        // Generate initial risk audit when assets load
        viewModelScope.launch {
            assets.collect { assetList ->
                if (assetList.isNotEmpty() && _riskAudit.value == null) {
                    _riskAudit.value = aiService.auditPortfolioRisk(assetList, vault)
                }
            }
        }
    }

    fun selectScreen(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun finishSplash() {
        _appFlowState.value = AppFlowState.AUTH
    }

    fun loginWithEmail(email: String, pass: String): Boolean {
        if (email.isBlank() || pass.length < 4) {
            _feedbackMessage.value = "Sila masukkan emel sah dan kata laluan minima 4 aksara"
            return false
        }
        _userEmail.value = email
        _userName.value = email.substringBefore("@").replaceFirstChar { it.uppercase() }
        _isGoogleAuth.value = false
        _appFlowState.value = AppFlowState.MAIN
        _feedbackMessage.value = "Selamat kembali ke RCrypto, ${_userName.value}!"
        return true
    }

    fun loginWithGoogle(email: String, name: String) {
        _userEmail.value = email
        _userName.value = name
        _isGoogleAuth.value = true
        _appFlowState.value = AppFlowState.MAIN
        _feedbackMessage.value = "Log masuk berjaya dengan Akaun Google: $email"
    }

    fun registerAccount(email: String, pass: String): Boolean {
        if (email.isBlank() || pass.length < 4) {
            _feedbackMessage.value = "Sila lengkapkan maklumat pendaftaran"
            return false
        }
        _userEmail.value = email
        _userName.value = email.substringBefore("@").replaceFirstChar { it.uppercase() }
        _isGoogleAuth.value = false
        _appFlowState.value = AppFlowState.MAIN
        _feedbackMessage.value = "Pendaftaran berjaya! Selamat datang ke RCrypto."
        return true
    }

    fun logout() {
        _appFlowState.value = AppFlowState.AUTH
        _feedbackMessage.value = "Anda telah log keluar dari akaun RCrypto."
    }

    fun selectPair(pair: String) {
        _selectedPair.value = pair
        val basePrice = getPriceForPair(pair)
        generateCandlesForPair(pair, basePrice)
        generateOrderBookForPrice(basePrice)
    }

    fun selectTimeframe(tf: String) {
        _selectedTimeframe.value = tf
        val basePrice = getPriceForPair(_selectedPair.value)
        generateCandlesForPair(_selectedPair.value, basePrice)
    }

    fun toggleRsi() { _showRsi.value = !_showRsi.value }
    fun toggleBollinger() { _showBollinger.value = !_showBollinger.value }
    fun toggleMacd() { _showMacd.value = !_showMacd.value }

    fun clearFeedback() { _feedbackMessage.value = null }

    fun sendCrypto(symbol: String, amount: Double, toAddress: String, note: String) {
        viewModelScope.launch {
            val result = repository.sendCrypto(symbol, amount, toAddress, note)
            if (result.isSuccess) {
                val value = result.getOrNull() ?: ""
                if (value.startsWith("MULTI_SIG_REQUIRED")) {
                    _feedbackMessage.value = "⚠️ Multi-Sig Quorum Triggered: Transfer of $$amount $symbol requires 2 cosigner approvals!"
                    _currentScreen.value = AppScreen.VAULT
                } else {
                    _feedbackMessage.value = "✅ Sent $amount $symbol successfully! Tx: ${value.take(14)}..."
                }
            } else {
                _feedbackMessage.value = "❌ Error sending: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun executeSwap(fromSymbol: String, toSymbol: String, fromAmount: Double, toAmount: Double, rate: Double) {
        viewModelScope.launch {
            val success = repository.executeSwap(fromSymbol, toSymbol, fromAmount, toAmount, rate)
            if (success) {
                _feedbackMessage.value = "✅ Swapped $fromAmount $fromSymbol -> $toAmount $toSymbol on Bina Smart Router!"
            } else {
                _feedbackMessage.value = "❌ Swap failed: check wallet balance."
            }
        }
    }

    fun signProposal(proposalId: String, cosignerId: String, cosignerName: String) {
        viewModelScope.launch {
            val success = repository.signAndApproveProposal(proposalId, cosignerId, cosignerName)
            if (success) {
                _feedbackMessage.value = "🖋️ Cryptographic signature verified for $cosignerName! Quorum evaluated."
            } else {
                _feedbackMessage.value = "⚠️ Could not sign: already signed by this key."
            }
        }
    }

    fun rejectProposal(proposalId: String) {
        viewModelScope.launch {
            repository.rejectProposal(proposalId)
            _feedbackMessage.value = "Proposal rejected and discarded."
        }
    }

    fun placeOrder(side: OrderSide, type: OrderType, price: Double, amount: Double) {
        viewModelScope.launch {
            val order = TradeOrder(
                pairSymbol = _selectedPair.value,
                side = side,
                type = type,
                price = price,
                amount = amount,
                filledAmount = if (type == OrderType.MARKET) amount else 0.0,
                status = if (type == OrderType.MARKET) "FILLED" else "OPEN",
                isBotOrder = false
            )
            repository.placeTradeOrder(order)
            _feedbackMessage.value = "Order placed: ${side.name} $amount ${_selectedPair.value.split("/")[0]} @ $price"
        }
    }

    fun createCustomBot(
        name: String,
        pair: String,
        strategyType: BotStrategyType,
        capitalUsd: Double,
        targetRsiOversold: Int,
        targetRsiOverbought: Int,
        stopLossPercent: Double,
        takeProfitPercent: Double,
        requireMultiSig: Boolean
    ) {
        viewModelScope.launch {
            val bot = AiTradingBot(
                name = name,
                pairSymbol = pair,
                strategyType = strategyType,
                status = BotStatus.RUNNING,
                allocatedCapitalUsd = capitalUsd,
                currentBalanceUsd = capitalUsd,
                totalProfitUsd = 0.0,
                profitPercent = 0.0,
                totalTrades = 0,
                winRatePercent = 100.0,
                targetRsiOversold = targetRsiOversold,
                targetRsiOverbought = targetRsiOverbought,
                stopLossPercent = stopLossPercent,
                takeProfitPercent = takeProfitPercent,
                requiresMultiSigApproval = requireMultiSig,
                logs = listOf(
                    BotExecutionLog(
                        action = "INITIALIZED",
                        pair = pair,
                        price = getPriceForPair(pair),
                        amount = 0.0,
                        details = "Bot launched with strategy: ${strategyType.title}. Monitoring indicators in real-time."
                    )
                )
            )
            repository.createAiBot(bot)
            _feedbackMessage.value = "🤖 Bot '${name}' deployed and running active strategy!"
        }
    }

    fun toggleBot(bot: AiTradingBot) {
        viewModelScope.launch {
            val nextStatus = when (bot.status) {
                BotStatus.RUNNING -> BotStatus.PAUSED
                BotStatus.PAUSED -> BotStatus.RUNNING
                BotStatus.STOPPED -> BotStatus.RUNNING
            }
            repository.updateBot(bot.copy(status = nextStatus))
            _feedbackMessage.value = "Bot '${bot.name}' is now ${nextStatus.name}"
        }
    }

    fun deleteBot(botId: String) {
        viewModelScope.launch {
            repository.deleteBot(botId)
            _feedbackMessage.value = "Bot terminated."
        }
    }

    fun runRiskAudit() {
        viewModelScope.launch {
            _riskAudit.value = aiService.auditPortfolioRisk(assets.value, vault)
            _feedbackMessage.value = "🛡️ Real-time Portfolio Risk Audit refreshed!"
        }
    }

    fun askAiAssistant(query: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiResponse.value = null
            delay(600) // Realistic analytical thinking delay
            val currentPrice = getPriceForPair(_selectedPair.value)
            val answer = aiService.queryMarketIntelligence(query, _selectedPair.value, currentPrice)
            _aiResponse.value = answer
            _isAiLoading.value = false
        }
    }

    fun getMarketInsights(): List<AiMarketInsight> {
        return aiService.getLiveMarketInsights()
    }

    fun getPredictiveModels(): List<PredictivePriceModel> {
        return aiService.getPredictiveModels(assets.value)
    }

    fun getSocialSentimentFeed(): List<SocialSentimentItem> {
        return aiService.getSocialSentimentFeed()
    }

    private fun getPriceForPair(pair: String): Double {
        val symbol = pair.split("/").firstOrNull() ?: "BTC"
        return assets.value.firstOrNull { it.symbol == symbol }?.priceUsd ?: when (symbol) {
            "BTC" -> 421950.0
            "BCH" -> 2160.0
            "SOL" -> 971.80
            "XMR" -> 768.10
            "LTC" -> 481.70
            "ETH" -> 15355.0
            "RCR" -> 66.10
            "BINA" -> 66.10
            "USDT" -> 4.45
            "MYR" -> 1.0
            else -> 100.0
        }
    }

    fun withdrawToBank(
        bankName: String, // "Maybank" or "CIMB Bank"
        accountNumber: String,
        accountHolder: String,
        amountMyr: Double,
        transferType: String // "DuitNow Pantas" or "IBG Giro"
    ) {
        viewModelScope.launch {
            val myrAsset = assets.value.firstOrNull { it.symbol == "MYR" }
            val currentBal = myrAsset?.balance ?: 0.0
            if (currentBal < amountMyr) {
                _feedbackMessage.value = "Baki MYR tidak mencukupi untuk pengeluaran (Baki: RM %,.2f)".format(currentBal)
                return@launch
            }

            // Deduct MYR balance
            repository.updateAssetBalance("MYR", currentBal - amountMyr)

            val refId = "MYR" + UUID.randomUUID().toString().take(10).uppercase()
            _feedbackMessage.value = "✅ Pengeluaran RM %,.2f ke $bankName ($accountNumber) berjaya diproses via DuitNow! Ruj: $refId".format(amountMyr)
        }
    }

    private fun generateCandlesForPair(pair: String, currentPrice: Double) {
        val list = mutableListOf<CandleStick>()
        var price = currentPrice * 0.95
        val now = System.currentTimeMillis()
        val intervalMs = 15 * 60 * 1000L

        for (i in 30 downTo 0) {
            val delta = (Random.nextDouble() - 0.48) * (price * 0.012)
            val open = price
            val close = (open + delta).coerceAtLeast(0.01)
            val high = maxOf(open, close) + Random.nextDouble() * (price * 0.005)
            val low = minOf(open, close) - Random.nextDouble() * (price * 0.005)
            val volume = Random.nextDouble() * 150.0 + 20.0

            list.add(
                CandleStick(
                    timestamp = now - (i * intervalMs),
                    open = open,
                    high = high,
                    low = low,
                    close = close,
                    volume = volume
                )
            )
            price = close
        }
        _candles.value = list
        updateTechnicalAnalysis(list)
    }

    private fun updateTechnicalAnalysis(candles: List<CandleStick>) {
        val closes = candles.map { it.close }
        _technicalAnalysis.value = aiService.generateTechnicalIndicators(closes)
    }

    private fun generateOrderBookForPrice(price: Double) {
        val bids = mutableListOf<OrderBookItem>()
        val asks = mutableListOf<OrderBookItem>()

        var bidRunningTotal = 0.0
        for (i in 1..8) {
            val p = price * (1.0 - (0.0008 * i))
            val amount = Random.nextDouble(0.1, 2.5)
            bidRunningTotal += amount
            bids.add(OrderBookItem(p, amount, bidRunningTotal, (i * 12f).coerceAtMost(100f)))
        }

        var askRunningTotal = 0.0
        for (i in 1..8) {
            val p = price * (1.0 + (0.0008 * i))
            val amount = Random.nextDouble(0.1, 2.5)
            askRunningTotal += amount
            asks.add(OrderBookItem(p, amount, askRunningTotal, (i * 12f).coerceAtMost(100f)))
        }

        val spread = (asks.firstOrNull()?.price ?: price) - (bids.firstOrNull()?.price ?: price)
        val spreadPct = (spread / price) * 100
        _orderBook.value = OrderBookDepth(bids, asks, spread, spreadPct)
    }

    private fun startMarketSimulation() {
        viewModelScope.launch(Dispatchers.Default) {
            var tick = 0
            while (isActive) {
                delay(3000) // 3-second live tick
                tick++

                // 1. Tick micro price movements
                val currentBtc = getPriceForPair("BTC/USDT")
                val deltaPercent = (Random.nextDouble() - 0.49) * 0.0018
                val newPrice = (currentBtc * (1.0 + deltaPercent))

                // Update candle list
                val existingCandles = _candles.value.toMutableList()
                if (existingCandles.isNotEmpty()) {
                    val last = existingCandles.last()
                    val updatedLast = last.copy(
                        close = newPrice,
                        high = maxOf(last.high, newPrice),
                        low = minOf(last.low, newPrice),
                        volume = last.volume + Random.nextDouble(0.2, 1.2)
                    )
                    existingCandles[existingCandles.size - 1] = updatedLast
                    _candles.value = existingCandles
                    updateTechnicalAnalysis(existingCandles)
                }

                // Update order book
                generateOrderBookForPrice(newPrice)

                // 2. Simulated Bot Activity every 4 ticks (~12s)
                if (tick % 4 == 0) {
                    executeSimulatedBotTicks(newPrice)
                }
            }
        }
    }

    private suspend fun executeSimulatedBotTicks(currentBtcPrice: Double) {
        val activeBots = bots.value.filter { it.status == BotStatus.RUNNING }
        for (bot in activeBots) {
            val profitIncrement = Random.nextDouble(4.5, 18.0)
            val updatedProfit = bot.totalProfitUsd + profitIncrement
            val updatedBalance = bot.currentBalanceUsd + profitIncrement
            val updatedProfitPct = (updatedProfit / bot.allocatedCapitalUsd) * 100

            val action = if (Random.nextBoolean()) "BUY_FILLED" else "SELL_FILLED"
            val logMessage = if (action == "BUY_FILLED") {
                "Accumulated on RSI floor (${bot.targetRsiOversold}) @ $%.2f".format(currentBtcPrice)
            } else {
                "Grid take-profit triggered @ $%.2f (+$%.2f PnL)".format(currentBtcPrice, profitIncrement)
            }

            val newLog = BotExecutionLog(
                action = action,
                pair = bot.pairSymbol,
                price = currentBtcPrice,
                amount = 0.015,
                details = logMessage,
                pnlUsd = profitIncrement
            )

            val updatedLogs = (listOf(newLog) + bot.logs).take(10)
            repository.updateBot(
                bot.copy(
                    totalProfitUsd = updatedProfit,
                    currentBalanceUsd = updatedBalance,
                    profitPercent = updatedProfitPct,
                    totalTrades = bot.totalTrades + 1,
                    logs = updatedLogs
                )
            )
        }
    }
}
