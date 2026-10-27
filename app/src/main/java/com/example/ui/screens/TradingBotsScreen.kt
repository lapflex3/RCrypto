package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AiTradingBot
import com.example.model.BotStatus
import com.example.model.BotStrategyType
import com.example.ui.components.BinaCard
import com.example.ui.components.StatusPill
import com.example.ui.theme.*
import com.example.viewmodel.BinaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradingBotsScreen(viewModel: BinaViewModel) {
    val bots by viewModel.bots.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    val totalProfitUsd = bots.sumOf { it.totalProfitUsd }
    val runningCount = bots.count { it.status == BotStatus.RUNNING }
    val totalTrades = bots.sumOf { it.totalTrades }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BinaBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ALGORITHMIC TRADING",
                        color = BinaTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "AI Trading Bots",
                        color = BinaTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = BinaPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_open_create_bot")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Deploy Bot", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Bots Performance Hero Stats
        item {
            BinaCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL ALGORITHMIC PNL",
                            color = BinaTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "+RM %,.2f".format(totalProfitUsd),
                            color = BinaProfitGreen,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    StatusPill(text = "$runningCount Bots Running", color = BinaProfitGreen)
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = BinaBorder, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Total Trades", color = BinaTextSecondary, fontSize = 11.sp)
                        Text("$totalTrades", color = BinaTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Average Win Rate", color = BinaTextSecondary, fontSize = 11.sp)
                        Text("84.2%", color = BinaSecondary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Execution Guard", color = BinaTextSecondary, fontSize = 11.sp)
                        Text("Multi-Sig M-of-N", color = BinaTertiary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Active Bots List
        item {
            Text(
                text = "DEPLOYED STRATEGIES (${bots.size})",
                color = BinaTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        items(bots, key = { it.id }) { bot ->
            BotCardItem(
                bot = bot,
                onToggleStatus = { viewModel.toggleBot(bot) },
                onDelete = { viewModel.deleteBot(bot.id) }
            )
        }
    }

    // Create Bot Strategy Modal
    if (showCreateDialog) {
        CreateBotDialog(
            onDismiss = { showCreateDialog = false },
            onDeploy = { name, pair, strategy, capital, rsiBuy, rsiSell, stopLoss, takeProfit, requireMultiSig ->
                viewModel.createCustomBot(
                    name = name,
                    pair = pair,
                    strategyType = strategy,
                    capitalUsd = capital,
                    targetRsiOversold = rsiBuy,
                    targetRsiOverbought = rsiSell,
                    stopLossPercent = stopLoss,
                    takeProfitPercent = takeProfit,
                    requireMultiSig = requireMultiSig
                )
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun BotCardItem(
    bot: AiTradingBot,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    var expandedLogs by remember { mutableStateOf(false) }

    BinaCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = bot.name,
                        color = BinaTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusPill(
                        text = bot.pairSymbol,
                        color = BinaSecondary
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = bot.strategyType.title,
                    color = BinaTextMuted,
                    fontSize = 12.sp
                )
            }

            StatusPill(
                text = bot.status.name,
                color = if (bot.status == BotStatus.RUNNING) BinaProfitGreen else BinaTertiary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // PnL & Balance Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(BinaSurfaceVariant)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Nilai Ekuiti Bot", color = BinaTextSecondary, fontSize = 11.sp)
                Text("RM %,.2f".format(bot.currentBalanceUsd), color = BinaTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Jumlah Keuntungan", color = BinaTextSecondary, fontSize = 11.sp)
                val formattedProfit = "RM %,.2f".format(bot.totalProfitUsd)
                val formattedPercent = "%.2f".format(bot.profitPercent)
                Text(
                    text = "+$formattedProfit (+$formattedPercent%)",
                    color = BinaProfitGreen,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Indicator parameters & Multi-sig badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(color = BinaSurfaceHighlight, shape = RoundedCornerShape(6.dp)) {
                Text("RSI: <${bot.targetRsiOversold} / >${bot.targetRsiOverbought}", color = BinaTextSecondary, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
            }
            Surface(color = BinaSurfaceHighlight, shape = RoundedCornerShape(6.dp)) {
                Text("SL: -${bot.stopLossPercent}% | TP: +${bot.takeProfitPercent}%", color = BinaTextSecondary, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
            }
            if (bot.requiresMultiSigApproval) {
                Surface(color = BinaTertiary.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                    Text("🛡️ Multi-Sig Guard", color = BinaTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (expandedLogs) "Hide Execution Logs" else "View Live Trades (${bot.logs.size})",
                color = BinaSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { expandedLogs = !expandedLogs }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onToggleStatus, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (bot.status == BotStatus.RUNNING) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Toggle Status",
                        tint = if (bot.status == BotStatus.RUNNING) BinaTertiary else BinaProfitGreen
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Bot",
                        tint = BinaLossRed
                    )
                }
            }
        }

        // Real-Time Execution Logs
        AnimatedVisibility(visible = expandedLogs) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BinaBackground)
                    .padding(10.dp)
            ) {
                Text("REAL-TIME EXECUTION LOGS", color = BinaTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                bot.logs.take(5).forEach { log ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "• ${log.details}",
                            color = if (log.action.contains("BUY")) BinaProfitGreen else if (log.action.contains("SELL")) BinaSecondary else BinaTextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBotDialog(
    onDismiss: () -> Unit,
    onDeploy: (
        name: String,
        pair: String,
        strategy: BotStrategyType,
        capital: Double,
        rsiBuy: Int,
        rsiSell: Int,
        stopLoss: Double,
        takeProfit: Double,
        requireMultiSig: Boolean
    ) -> Unit
) {
    var botName by remember { mutableStateOf("DeepAlpha Sentinel") }
    var selectedPair by remember { mutableStateOf("BTC/USDT") }
    var selectedStrategy by remember { mutableStateOf(BotStrategyType.RSI_MACD_MOMENTUM) }
    var capitalText by remember { mutableStateOf("2500") }
    var rsiBuyText by remember { mutableStateOf("30") }
    var rsiSellText by remember { mutableStateOf("70") }
    var stopLossText by remember { mutableStateOf("3.5") }
    var takeProfitText by remember { mutableStateOf("6.0") }
    var requireMultiSig by remember { mutableStateOf(false) }

    val pairs = listOf("BTC/USDT", "BCH/USDT", "SOL/USDT", "XMR/USDT", "LTC/USDT", "ETH/USDT", "BINA/USDT")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BinaSurface
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Deploy AI Trading Bot",
                    color = BinaTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Customized algorithmic execution engine",
                    color = BinaTextSecondary,
                    fontSize = 12.sp
                )
            }

            item {
                OutlinedTextField(
                    value = botName,
                    onValueChange = { botName = it },
                    label = { Text("Bot Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_bot_name"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BinaPrimary,
                        unfocusedBorderColor = BinaBorder
                    )
                )
            }

            item {
                Text("Select Trading Pair", color = BinaTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    pairs.take(4).forEach { p ->
                        val isSelected = p == selectedPair
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedPair = p },
                            color = if (isSelected) BinaPrimary.copy(alpha = 0.2f) else BinaSurfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = p.split("/")[0],
                                color = if (isSelected) BinaPrimary else BinaTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp).wrapContentWidth(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    pairs.drop(4).forEach { p ->
                        val isSelected = p == selectedPair
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedPair = p },
                            color = if (isSelected) BinaPrimary.copy(alpha = 0.2f) else BinaSurfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = p.split("/")[0],
                                color = if (isSelected) BinaPrimary else BinaTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp).wrapContentWidth(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }

            item {
                Text("Select Strategy Type", color = BinaTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                BotStrategyType.values().forEach { st ->
                    val isSelected = st == selectedStrategy
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedStrategy = st },
                        color = if (isSelected) BinaSurfaceHighlight else BinaBackground,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, BinaPrimary) else null,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = st.title, color = if (isSelected) BinaPrimary else BinaTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = st.description, color = BinaTextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = capitalText,
                    onValueChange = { capitalText = it },
                    label = { Text("Allocated Capital (USD)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("input_bot_capital"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BinaPrimary,
                        unfocusedBorderColor = BinaBorder
                    )
                )
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = rsiBuyText,
                        onValueChange = { rsiBuyText = it },
                        label = { Text("RSI Oversold Buy") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = rsiSellText,
                        onValueChange = { rsiSellText = it },
                        label = { Text("RSI Overbought Sell") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stopLossText,
                        onValueChange = { stopLossText = it },
                        label = { Text("Stop Loss %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = takeProfitText,
                        onValueChange = { takeProfitText = it },
                        label = { Text("Take Profit %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(BinaSurfaceVariant)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Multi-Sig Co-Authorization Guard", color = BinaTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Require 2-of-3 multi-sig approval before executing trades > $2,500", color = BinaTextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = requireMultiSig,
                        onCheckedChange = { requireMultiSig = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = BinaPrimary)
                    )
                }
            }

            item {
                Button(
                    onClick = {
                        val cap = capitalText.toDoubleOrNull() ?: 1000.0
                        val rsiB = rsiBuyText.toIntOrNull() ?: 30
                        val rsiS = rsiSellText.toIntOrNull() ?: 70
                        val sl = stopLossText.toDoubleOrNull() ?: 3.5
                        val tp = takeProfitText.toDoubleOrNull() ?: 6.0
                        onDeploy(botName, selectedPair, selectedStrategy, cap, rsiB, rsiS, sl, tp, requireMultiSig)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_confirm_deploy_bot"),
                    colors = ButtonDefaults.buttonColors(containerColor = BinaPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Deploy Algorithmic Bot", color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
