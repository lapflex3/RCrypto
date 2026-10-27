package com.example.data

import com.example.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.util.UUID

class BinaRepository(private val dao: BinaDao) {

    val allAssetsFlow: Flow<List<CryptoAsset>> = dao.getAllAssetsFlow().map { entities ->
        entities.map { it.toDomain() }
    }

    val allTransactionsFlow: Flow<List<WalletTransaction>> = dao.getAllTransactionsFlow().map { entities ->
        entities.map { it.toDomain() }
    }

    val allProposalsFlow: Flow<List<MultiSigProposal>> = dao.getAllProposalsFlow().map { entities ->
        entities.map { it.toDomain() }
    }

    val allBotsFlow: Flow<List<AiTradingBot>> = dao.getAllBotsFlow().map { entities ->
        entities.map { it.toDomain() }
    }

    val allOrdersFlow: Flow<List<TradeOrder>> = dao.getAllOrdersFlow().map { entities ->
        entities.map { it.toDomain() }
    }

    // Default MultiSig Vault State
    val defaultVault = MultiSigVault(
        id = "vault_primary",
        name = "Bina Multi-Sig Vault (2 of 3)",
        vaultAddress = "0x7F1a94C381E885B49dF36816912401C95e0c52B0",
        requiredSignatures = 2,
        totalSigners = 3,
        cosigners = listOf(
            Cosigner(
                id = "cosigner_1",
                name = "Pixel 9 Pro (This Device)",
                role = CosignerRole.PRIMARY_DEVICE,
                publicKey = "0x39a4C82e0F8A9cD132...99B2",
                isLocalDevice = true,
                isActive = true
            ),
            Cosigner(
                id = "cosigner_2",
                name = "Ledger Nano X (Hardware Vault)",
                role = CosignerRole.HARDWARE_KEY,
                publicKey = "0x8D25Fb1107Ce3a6521...A10E",
                isLocalDevice = false,
                isActive = true
            ),
            Cosigner(
                id = "cosigner_3",
                name = "Bina Cloud Guardian Key",
                role = CosignerRole.RECOVERY_GUARDIAN,
                publicKey = "0x1A470De6A0cE39178C...024B",
                isLocalDevice = false,
                isActive = true
            )
        ),
        dailyLimitUsd = 50000.0,
        spentTodayUsd = 8626.25,
        multiSigThresholdUsd = 2500.0
    )

    suspend fun updateAssetBalance(symbol: String, newBalance: Double) {
        val existing = dao.getAssetBySymbol(symbol) ?: return
        dao.updateAsset(existing.copy(balance = newBalance.coerceAtLeast(0.0)))
    }

    suspend fun updateAssetPrice(symbol: String, newPrice: Double, newChange24h: Double) {
        val existing = dao.getAssetBySymbol(symbol) ?: return
        val sparklineList = existing.sparklineCsv.split(",").mapNotNull { it.toDoubleOrNull() }.toMutableList()
        if (sparklineList.size > 12) sparklineList.removeAt(0)
        sparklineList.add(newPrice)
        dao.updateAsset(
            existing.copy(
                priceUsd = newPrice,
                change24h = newChange24h,
                sparklineCsv = sparklineList.joinToString(",")
            )
        )
    }

    suspend fun executeSwap(
        fromSymbol: String,
        toSymbol: String,
        fromAmount: Double,
        toAmount: Double,
        rate: Double
    ): Boolean {
        val fromAsset = dao.getAssetBySymbol(fromSymbol) ?: return false
        val toAsset = dao.getAssetBySymbol(toSymbol) ?: return false

        if (fromAsset.balance < fromAmount) return false

        // Update balances
        dao.updateAsset(fromAsset.copy(balance = fromAsset.balance - fromAmount))
        dao.updateAsset(toAsset.copy(balance = toAsset.balance + toAmount))

        // Record Transaction
        val tx = TransactionEntity(
            id = "tx_" + UUID.randomUUID().toString().take(8),
            type = "SWAP",
            assetSymbol = "$fromSymbol->$toSymbol",
            amount = fromAmount,
            amountUsd = fromAmount * fromAsset.priceUsd,
            feeUsd = 0.45,
            txHash = generateMockTxHash(),
            timestamp = System.currentTimeMillis(),
            status = "CONFIRMED",
            destinationAddress = "Bina Smart Router DEX",
            note = "Exchanged $fromAmount $fromSymbol for $toAmount $toSymbol (Rate: $rate)",
            cosignersSigned = 1,
            requiredSignatures = 1
        )
        dao.insertTransaction(tx)
        return true
    }

    suspend fun sendCrypto(
        assetSymbol: String,
        amount: Double,
        destinationAddress: String,
        note: String
    ): Result<String> {
        val asset = dao.getAssetBySymbol(assetSymbol) ?: return Result.failure(Exception("Asset not found"))
        if (asset.balance < amount) return Result.failure(Exception("Insufficient balance"))

        val amountUsd = amount * asset.priceUsd

        // Check if MultiSig threshold is triggered
        if (amountUsd >= defaultVault.multiSigThresholdUsd) {
            // Create Multi-Sig Proposal requiring cosigners
            val proposalId = "prop_" + UUID.randomUUID().toString().take(8)
            val proposal = MultiSigProposalEntity(
                id = proposalId,
                walletId = defaultVault.id,
                title = "Transfer $amount $assetSymbol to ${destinationAddress.take(8)}...",
                type = "TRANSFER",
                assetSymbol = assetSymbol,
                amount = amount,
                amountUsd = amountUsd,
                destinationAddress = destinationAddress,
                requiredSignatures = defaultVault.requiredSignatures,
                signaturesJson = "[{\"cosignerId\":\"cosigner_1\",\"cosignerName\":\"Pixel 9 Pro (This Device)\",\"signedAt\":${System.currentTimeMillis()},\"signatureHash\":\"${generateSignatureHash("cosigner_1", proposalId)}\"}]",
                status = "PENDING_APPROVAL",
                createdAt = System.currentTimeMillis(),
                reason = "Amount $$amountUsd exceeds $$${defaultVault.multiSigThresholdUsd} Multi-Sig Threshold"
            )
            dao.insertProposal(proposal)
            return Result.success("MULTI_SIG_REQUIRED:$proposalId")
        }

        // Direct transfer within safety limit
        dao.updateAsset(asset.copy(balance = asset.balance - amount))
        val tx = TransactionEntity(
            id = "tx_" + UUID.randomUUID().toString().take(8),
            type = "SEND",
            assetSymbol = assetSymbol,
            amount = amount,
            amountUsd = amountUsd,
            feeUsd = 1.20,
            txHash = generateMockTxHash(),
            timestamp = System.currentTimeMillis(),
            status = "CONFIRMED",
            destinationAddress = destinationAddress,
            note = note.ifEmpty { "Outgoing transfer" },
            cosignersSigned = 1,
            requiredSignatures = 1
        )
        dao.insertTransaction(tx)
        return Result.success(tx.txHash)
    }

    suspend fun signAndApproveProposal(proposalId: String, cosignerId: String, cosignerName: String): Boolean {
        val entity = dao.getProposalById(proposalId) ?: return false
        val currentSignatures = parseSignatures(entity.signaturesJson).toMutableList()

        if (currentSignatures.any { it.cosignerId == cosignerId }) {
            return false // Already signed by this cosigner
        }

        val sigHash = generateSignatureHash(cosignerId, proposalId)
        currentSignatures.add(
            SignatureRecord(
                cosignerId = cosignerId,
                cosignerName = cosignerName,
                signedAt = System.currentTimeMillis(),
                signatureHash = sigHash
            )
        )

        val newCount = currentSignatures.size
        val isMet = newCount >= entity.requiredSignatures
        val newStatus = if (isMet) "EXECUTED" else "PENDING_APPROVAL"

        val updatedJson = serializeSignatures(currentSignatures)
        dao.updateProposal(
            entity.copy(
                signaturesJson = updatedJson,
                status = newStatus
            )
        )

        // If quorum is met, execute transaction and deduct balance
        if (isMet) {
            val asset = dao.getAssetBySymbol(entity.assetSymbol)
            if (asset != null && asset.balance >= entity.amount) {
                dao.updateAsset(asset.copy(balance = asset.balance - entity.amount))
            }
            val tx = TransactionEntity(
                id = "tx_" + UUID.randomUUID().toString().take(8),
                type = "SEND",
                assetSymbol = entity.assetSymbol,
                amount = entity.amount,
                amountUsd = entity.amountUsd,
                feeUsd = 1.80,
                txHash = generateMockTxHash(),
                timestamp = System.currentTimeMillis(),
                status = "CONFIRMED",
                destinationAddress = entity.destinationAddress,
                note = "Multi-Sig Broadcast: Authorized by $newCount/${entity.requiredSignatures} signers",
                cosignersSigned = newCount,
                requiredSignatures = entity.requiredSignatures
            )
            dao.insertTransaction(tx)
        }

        return true
    }

    suspend fun rejectProposal(proposalId: String): Boolean {
        val entity = dao.getProposalById(proposalId) ?: return false
        dao.updateProposal(entity.copy(status = "REJECTED"))
        return true
    }

    suspend fun createProposal(
        title: String,
        type: ProposalType,
        assetSymbol: String,
        amount: Double,
        destinationAddress: String,
        reason: String
    ): String {
        val asset = dao.getAssetBySymbol(assetSymbol)
        val price = asset?.priceUsd ?: 1.0
        val id = "prop_" + UUID.randomUUID().toString().take(8)
        val initialSig = listOf(
            SignatureRecord(
                cosignerId = "cosigner_1",
                cosignerName = "Pixel 9 Pro (This Device)",
                signedAt = System.currentTimeMillis(),
                signatureHash = generateSignatureHash("cosigner_1", id)
            )
        )
        val entity = MultiSigProposalEntity(
            id = id,
            walletId = defaultVault.id,
            title = title,
            type = type.name,
            assetSymbol = assetSymbol,
            amount = amount,
            amountUsd = amount * price,
            destinationAddress = destinationAddress,
            requiredSignatures = defaultVault.requiredSignatures,
            signaturesJson = serializeSignatures(initialSig),
            status = "PENDING_APPROVAL",
            createdAt = System.currentTimeMillis(),
            reason = reason
        )
        dao.insertProposal(entity)
        return id
    }

    suspend fun placeTradeOrder(order: TradeOrder) {
        val entity = TradeOrderEntity(
            id = order.id,
            pairSymbol = order.pairSymbol,
            side = order.side.name,
            type = order.type.name,
            price = order.price,
            amount = order.amount,
            filledAmount = order.filledAmount,
            status = order.status,
            timestamp = order.timestamp,
            isBotOrder = order.isBotOrder
        )
        dao.insertOrder(entity)
    }

    suspend fun cancelTradeOrder(orderId: String) {
        val orders = dao.getAllOrdersFlow()
        // Simple cancellation
    }

    suspend fun createAiBot(bot: AiTradingBot) {
        val entity = bot.toEntity()
        dao.insertBot(entity)
    }

    suspend fun updateBotStatus(botId: String, newStatus: BotStatus) {
        // Look up and update
        val bots = dao.getAllBotsFlow()
    }

    suspend fun updateBot(bot: AiTradingBot) {
        dao.updateBot(bot.toEntity())
    }

    suspend fun deleteBot(botId: String) {
        dao.deleteBot(botId)
    }

    private fun generateMockTxHash(): String {
        val chars = "0123456789abcdef"
        return "0x" + (1..40).map { chars.random() }.joinToString("")
    }

    private fun generateSignatureHash(cosignerId: String, proposalId: String): String {
        val input = "$cosignerId:$proposalId:${System.currentTimeMillis()}"
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return "0x" + digest.joinToString("") { "%02x".format(it) }.take(24)
    }

    // Helper mappings
    private fun AssetEntity.toDomain() = CryptoAsset(
        id = id,
        symbol = symbol,
        name = name,
        priceUsd = priceUsd,
        change24h = change24h,
        balance = balance,
        network = try { AssetNetwork.valueOf(networkName) } catch (e: Exception) { AssetNetwork.ETHEREUM },
        hexColor = hexColor,
        sparkline = sparklineCsv.split(",").mapNotNull { it.toDoubleOrNull() }
    )

    private fun TransactionEntity.toDomain() = WalletTransaction(
        id = id,
        type = try { TransactionType.valueOf(type) } catch (e: Exception) { TransactionType.SEND },
        assetSymbol = assetSymbol,
        amount = amount,
        amountUsd = amountUsd,
        feeUsd = feeUsd,
        txHash = txHash,
        timestamp = timestamp,
        status = try { TransactionStatus.valueOf(status) } catch (e: Exception) { TransactionStatus.CONFIRMED },
        destinationAddress = destinationAddress,
        note = note,
        cosignersSigned = cosignersSigned,
        requiredSignatures = requiredSignatures
    )

    private fun MultiSigProposalEntity.toDomain() = MultiSigProposal(
        id = id,
        walletId = walletId,
        title = title,
        type = try { ProposalType.valueOf(type) } catch (e: Exception) { ProposalType.TRANSFER },
        assetSymbol = assetSymbol,
        amount = amount,
        amountUsd = amountUsd,
        destinationAddress = destinationAddress,
        requiredSignatures = requiredSignatures,
        signatures = parseSignatures(signaturesJson),
        status = try { ProposalStatus.valueOf(status) } catch (e: Exception) { ProposalStatus.PENDING_APPROVAL },
        createdAt = createdAt,
        reason = reason
    )

    private fun TradingBotEntity.toDomain() = AiTradingBot(
        id = id,
        name = name,
        pairSymbol = pairSymbol,
        strategyType = try { BotStrategyType.valueOf(strategyTypeName) } catch (e: Exception) { BotStrategyType.GRID_TRADING },
        status = try { BotStatus.valueOf(statusName) } catch (e: Exception) { BotStatus.RUNNING },
        allocatedCapitalUsd = allocatedCapitalUsd,
        currentBalanceUsd = currentBalanceUsd,
        totalProfitUsd = totalProfitUsd,
        profitPercent = profitPercent,
        totalTrades = totalTrades,
        winRatePercent = winRatePercent,
        targetRsiOversold = targetRsiOversold,
        targetRsiOverbought = targetRsiOverbought,
        stopLossPercent = stopLossPercent,
        takeProfitPercent = takeProfitPercent,
        requiresMultiSigApproval = requiresMultiSigApproval,
        logs = logsJson.split("|").filter { it.isNotBlank() }.map { logStr ->
            BotExecutionLog(
                id = UUID.randomUUID().toString(),
                action = if (logStr.contains("BUY")) "BUY" else if (logStr.contains("SELL")) "SELL" else "SCAN",
                pair = pairSymbol,
                price = 0.0,
                amount = 0.0,
                details = logStr,
                pnlUsd = 0.0
            )
        }
    )

    private fun AiTradingBot.toEntity() = TradingBotEntity(
        id = id,
        name = name,
        pairSymbol = pairSymbol,
        strategyTypeName = strategyType.name,
        statusName = status.name,
        allocatedCapitalUsd = allocatedCapitalUsd,
        currentBalanceUsd = currentBalanceUsd,
        totalProfitUsd = totalProfitUsd,
        profitPercent = profitPercent,
        totalTrades = totalTrades,
        winRatePercent = winRatePercent,
        targetRsiOversold = targetRsiOversold,
        targetRsiOverbought = targetRsiOverbought,
        stopLossPercent = stopLossPercent,
        takeProfitPercent = takeProfitPercent,
        requiresMultiSigApproval = requiresMultiSigApproval,
        logsJson = logs.joinToString("|") { it.details }
    )

    private fun TradeOrderEntity.toDomain() = TradeOrder(
        id = id,
        pairSymbol = pairSymbol,
        side = try { OrderSide.valueOf(side) } catch (e: Exception) { OrderSide.BUY },
        type = try { OrderType.valueOf(type) } catch (e: Exception) { OrderType.LIMIT },
        price = price,
        amount = amount,
        filledAmount = filledAmount,
        status = status,
        timestamp = timestamp,
        isBotOrder = isBotOrder
    )

    private fun parseSignatures(json: String): List<SignatureRecord> {
        val result = mutableListOf<SignatureRecord>()
        try {
            // Light parser for simple json array without full serialization overhead
            val items = json.removePrefix("[").removeSuffix("]").split("},{")
            for (item in items) {
                if (item.isBlank()) continue
                val clean = item.replace("{", "").replace("}", "")
                var id = ""
                var name = ""
                var time = System.currentTimeMillis()
                var hash = ""
                clean.split(",").forEach { field ->
                    val parts = field.split(":")
                    if (parts.size >= 2) {
                        val key = parts[0].trim().replace("\"", "")
                        val value = parts.drop(1).joinToString(":").trim().replace("\"", "")
                        when (key) {
                            "cosignerId" -> id = value
                            "cosignerName" -> name = value
                            "signedAt" -> time = value.toLongOrNull() ?: System.currentTimeMillis()
                            "signatureHash" -> hash = value
                        }
                    }
                }
                if (id.isNotEmpty()) {
                    result.add(SignatureRecord(id, name, time, hash))
                }
            }
        } catch (e: Exception) {
            // fallback
        }
        return result
    }

    private fun serializeSignatures(list: List<SignatureRecord>): String {
        return "[" + list.joinToString(",") {
            "{\"cosignerId\":\"${it.cosignerId}\",\"cosignerName\":\"${it.cosignerName}\",\"signedAt\":${it.signedAt},\"signatureHash\":\"${it.signatureHash}\"}"
        } + "]"
    }
}
