package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.model.OrderSide
import com.example.model.OrderType
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.BinaViewModel

@Composable
fun TradeScreen(viewModel: BinaViewModel) {
    val selectedPair by viewModel.selectedPair.collectAsState()
    val selectedTimeframe by viewModel.selectedTimeframe.collectAsState()
    val candles by viewModel.candles.collectAsState()
    val orderBook by viewModel.orderBook.collectAsState()
    val techAnalysis by viewModel.technicalAnalysis.collectAsState()
    val showRsi by viewModel.showRsi.collectAsState()
    val showBollinger by viewModel.showBollinger.collectAsState()
    val orders by viewModel.orders.collectAsState()

    var orderSide by remember { mutableStateOf(OrderSide.BUY) }
    var orderType by remember { mutableStateOf(OrderType.LIMIT) }
    var priceText by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }

    val currentPrice = candles.lastOrNull()?.close ?: 94820.0

    // Auto update price input when changing pair if empty
    LaunchedEffect(selectedPair, currentPrice) {
        if (priceText.isEmpty()) {
            priceText = "%.2f".format(currentPrice)
        }
    }

    val availablePairs = listOf("BTC/MYR", "BCH/MYR", "SOL/MYR", "XMR/MYR", "LTC/MYR", "ETH/MYR", "USDT/MYR", "RCR/MYR")
    val timeframes = listOf("1m", "5m", "15m", "1h", "1D")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BinaBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Pair selector chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availablePairs.take(4).forEach { pair ->
                    val isSelected = pair == selectedPair
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                viewModel.selectPair(pair)
                                priceText = ""
                            },
                        color = if (isSelected) BinaPrimary.copy(alpha = 0.2f) else BinaSurface,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, BinaPrimary) else null,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = pair,
                            color = if (isSelected) BinaPrimary else BinaTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(vertical = 8.dp).wrapContentWidth(Alignment.CenterHorizontally)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availablePairs.drop(4).forEach { pair ->
                    val isSelected = pair == selectedPair
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                viewModel.selectPair(pair)
                                priceText = ""
                            },
                        color = if (isSelected) BinaPrimary.copy(alpha = 0.2f) else BinaSurface,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, BinaPrimary) else null,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = pair,
                            color = if (isSelected) BinaPrimary else BinaTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(vertical = 8.dp).wrapContentWidth(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }

        // Price & 24h ticker header
        item {
            BinaCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "RM %,.2f".format(currentPrice),
                            color = BinaTextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "+3.42%",
                                color = BinaProfitGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Vol 24j: RM 18.6M",
                                color = BinaTextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // 24h High/Low Stats
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Tinggi 24j: RM %,.2f".format(currentPrice * 1.025),
                            color = BinaTextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Rendah 24j: RM %,.2f".format(currentPrice * 0.972),
                            color = BinaTextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Oracle: DuitNow / FPX",
                            color = BinaSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Timeframe & Indicator Selector
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Timeframes
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    timeframes.forEach { tf ->
                        val isSelected = tf == selectedTimeframe
                        Text(
                            text = tf,
                            color = if (isSelected) BinaPrimary else BinaTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) BinaSurfaceHighlight else Color.Transparent)
                                .clickable { viewModel.selectTimeframe(tf) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Indicator Toggles
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = showRsi,
                        onClick = { viewModel.toggleRsi() },
                        label = { Text("RSI", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BinaPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = BinaPrimary
                        )
                    )
                    FilterChip(
                        selected = showBollinger,
                        onClick = { viewModel.toggleBollinger() },
                        label = { Text("BOLL", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BinaSecondary.copy(alpha = 0.2f),
                            selectedLabelColor = BinaSecondary
                        )
                    )
                }
            }
        }

        // Live Candlestick & Volume Chart
        item {
            BinaCard(modifier = Modifier.fillMaxWidth()) {
                CandlestickChart(
                    candles = candles,
                    showBollinger = showBollinger,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

                if (showRsi && techAnalysis != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = BinaBorder, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "RSI(14): ${"%.1f".format(techAnalysis!!.rsi)}",
                            color = when {
                                techAnalysis!!.rsi < 30 -> BinaProfitGreen
                                techAnalysis!!.rsi > 70 -> BinaLossRed
                                else -> BinaSecondary
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = techAnalysis!!.marketSignal,
                            color = BinaTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Order Book & Depth Ladder
        item {
            BinaCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "ORDER BOOK DEPTH",
                    color = BinaTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    // Bids (Buy orders in Green)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Bids (USDT)", color = BinaProfitGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        orderBook.bids.take(5).forEach { item ->
                            OrderBookRow(item = item, isBid = true)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Asks (Sell orders in Red)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Asks (USDT)", color = BinaLossRed, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        orderBook.asks.take(5).forEach { item ->
                            OrderBookRow(item = item, isBid = false)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Spread: RM %,.2f".format(orderBook.spreadUsd),
                        color = BinaTextMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Spread %: ${"%.3f".format(orderBook.spreadPercent)}%",
                        color = BinaTextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Order Execution Form
        item {
            BinaCard(modifier = Modifier.fillMaxWidth()) {
                // Buy / Sell Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(BinaSurfaceVariant)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (orderSide == OrderSide.BUY) BinaProfitGreen else Color.Transparent)
                            .clickable { orderSide = OrderSide.BUY }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "BUY",
                            color = if (orderSide == OrderSide.BUY) Color.Black else BinaTextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (orderSide == OrderSide.SELL) BinaLossRed else Color.Transparent)
                            .clickable { orderSide = OrderSide.SELL }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SELL",
                            color = if (orderSide == OrderSide.SELL) Color.White else BinaTextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Order Type Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(OrderType.LIMIT, OrderType.MARKET, OrderType.STOP_LOSS).forEach { type ->
                        val isSelected = orderType == type
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { orderType = type },
                            color = if (isSelected) BinaSurfaceHighlight else BinaBackground,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = type.name,
                                color = if (isSelected) BinaPrimary else BinaTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier
                                    .padding(vertical = 6.dp)
                                    .wrapContentWidth(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Price Input (if not MARKET)
                if (orderType != OrderType.MARKET) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Order Price (USDT)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_order_price"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BinaPrimary,
                            unfocusedBorderColor = BinaBorder
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Amount Input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (${selectedPair.split("/")[0]})") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_order_amount"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BinaPrimary,
                        unfocusedBorderColor = BinaBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick percentage buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("25%", "50%", "75%", "100%").forEach { pct ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    val factor = when (pct) {
                                        "25%" -> 0.05
                                        "50%" -> 0.10
                                        "75%" -> 0.20
                                        else -> 0.35
                                    }
                                    amountText = "%.4f".format(factor)
                                },
                            color = BinaSurfaceHighlight,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = pct,
                                color = BinaTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .padding(vertical = 6.dp)
                                    .wrapContentWidth(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Button
                val buttonColor = if (orderSide == OrderSide.BUY) BinaProfitGreen else BinaLossRed
                Button(
                    onClick = {
                        val p = priceText.toDoubleOrNull() ?: currentPrice
                        val a = amountText.toDoubleOrNull() ?: 0.01
                        viewModel.placeOrder(orderSide, orderType, p, a)
                        amountText = ""
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_place_order"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                ) {
                    Text(
                        text = "${orderSide.name} ${selectedPair.split("/")[0]}",
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Open Orders List
        item {
            Text(
                text = "OPEN ORDERS (${orders.filter { it.status == "OPEN" }.size})",
                color = BinaTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        items(orders.filter { it.status == "OPEN" }, key = { it.id }) { order ->
            BinaCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusPill(
                                text = order.side.name,
                                color = if (order.side == OrderSide.BUY) BinaProfitGreen else BinaLossRed
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = order.pairSymbol,
                                color = BinaTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            if (order.isBotOrder) {
                                Spacer(modifier = Modifier.width(6.dp))
                                StatusPill(text = "AI BOT", color = BinaTertiary)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Harga: RM %,.2f • Jumlah: ${order.amount}".format(order.price),
                            color = BinaTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = "OPEN",
                        color = BinaSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun OrderBookRow(item: com.example.model.OrderBookItem, isBid: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
    ) {
        // Depth background bar
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = item.depthPercentage / 100f)
                .align(if (isBid) Alignment.CenterStart else Alignment.CenterEnd)
                .background(
                    if (isBid) BinaProfitGreen.copy(alpha = 0.12f) else BinaLossRed.copy(alpha = 0.12f)
                )
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "%.2f".format(item.price),
                color = if (isBid) BinaProfitGreen else BinaLossRed,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "%.3f".format(item.amount),
                color = BinaTextSecondary,
                fontSize = 11.sp
            )
        }
    }
}
