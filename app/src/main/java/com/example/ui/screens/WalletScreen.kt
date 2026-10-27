package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CryptoAsset
import com.example.model.TransactionStatus
import com.example.model.WalletTransaction
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.BinaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WalletScreen(
    viewModel: BinaViewModel,
    onOpenSend: () -> Unit,
    onOpenReceive: () -> Unit,
    onOpenWithdraw: () -> Unit
) {
    val assets by viewModel.assets.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val proposals by viewModel.proposals.collectAsState()
    val vault = viewModel.vault

    var selectedSubTab by remember { mutableStateOf(0) } // 0: Assets, 1: Activity
    var isBalanceVisible by remember { mutableStateOf(true) }

    val totalBalanceUsd = assets.sumOf { it.balanceUsd }
    val pendingProposalsCount = proposals.count { it.status == com.example.model.ProposalStatus.PENDING_APPROVAL }

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
            // Portfolio Hero Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF131D31),
                                Color(0xFF0F1524)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL NET ASSET VALUE",
                            color = BinaTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        IconButton(
                            onClick = { isBalanceVisible = !isBalanceVisible },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isBalanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Balance",
                                tint = BinaTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isBalanceVisible) "RM %,.2f".format(totalBalanceUsd) else "••••••••",
                        color = BinaTextPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = BinaProfitGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = BinaProfitGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+RM 21,450.50 (+9.12%)",
                                    color = BinaProfitGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "24j Lepas",
                            color = BinaTextMuted,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Multi-Sig Vault Security Status Banner
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectScreen(AppScreen.VAULT) },
                        color = BinaSurfaceHighlight.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (pendingProposalsCount > 0) BinaTertiary else BinaPrimary)
                                )
                                Column {
                                    Text(
                                        text = "Bilik Kebal: ${vault.requiredSignatures}-drp-${vault.totalSigners} Multi-Sig Aktif",
                                        color = BinaTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = if (pendingProposalsCount > 0) "⚠️ $pendingProposalsCount cadangan memerlukan tandatangan" else "Kuorum keselamatan institusi diaktifkan",
                                        color = if (pendingProposalsCount > 0) BinaTertiary else BinaTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Lihat Kebal",
                                tint = BinaTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Quick Actions Row (Hantar, Terima, Tukar, Keluarkan ke Bank, Multi-Sig)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickActionButton(
                    icon = Icons.AutoMirrored.Filled.Send,
                    label = "Hantar",
                    tag = "action_send",
                    onClick = onOpenSend
                )
                QuickActionButton(
                    icon = Icons.Default.QrCodeScanner,
                    label = "Terima",
                    tag = "action_receive",
                    onClick = onOpenReceive
                )
                QuickActionButton(
                    icon = Icons.Default.SwapHoriz,
                    label = "Tukar",
                    tag = "action_swap",
                    onClick = { viewModel.selectScreen(AppScreen.SWAP) }
                )
                QuickActionButton(
                    icon = Icons.Default.AccountBalance,
                    label = "Keluarkan",
                    tag = "action_withdraw_bank",
                    onClick = onOpenWithdraw
                )
                QuickActionButton(
                    icon = Icons.Default.Security,
                    label = "Multi-Sig",
                    tag = "action_multisig",
                    badge = if (pendingProposalsCount > 0) "$pendingProposalsCount" else null,
                    onClick = { viewModel.selectScreen(AppScreen.VAULT) }
                )
            }
        }

        // Market Price Coin Utama (BTC, BCH, SOL, XMR, LTC, ETH)
        item {
            val mainCoinSymbols = listOf("BTC", "BCH", "SOL", "XMR", "LTC", "ETH")
            val mainCoins = mainCoinSymbols.mapNotNull { sym -> assets.firstOrNull { it.symbol == sym } }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(BinaPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "MARKET PRICE COIN UTAMA",
                            color = BinaTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "Live Ticker",
                        color = BinaSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(mainCoins, key = { it.symbol }) { coin ->
                        MainCoinPriceCard(
                            asset = coin,
                            onClick = {
                                viewModel.selectPair("${coin.symbol}/USDT")
                                viewModel.selectScreen(AppScreen.TRADE)
                            }
                        )
                    }
                }
            }
        }

        // Sub Tabs: Assets vs Activity
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BinaSurface)
                    .padding(4.dp)
            ) {
                TabButton(
                    title = "Crypto Assets (${assets.size})",
                    isSelected = selectedSubTab == 0,
                    onClick = { selectedSubTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    title = "Activity History",
                    isSelected = selectedSubTab == 1,
                    onClick = { selectedSubTab = 1 },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (selectedSubTab == 0) {
            // Assets List
            items(assets, key = { it.id }) { asset ->
                AssetRowItem(
                    asset = asset,
                    onTradeClick = {
                        viewModel.selectPair("${asset.symbol}/USDT")
                        viewModel.selectScreen(AppScreen.TRADE)
                    }
                )
            }
        } else {
            // Transaction Activity List
            if (transactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No transaction history yet", color = BinaTextMuted)
                    }
                }
            } else {
                items(transactions, key = { it.id }) { tx ->
                    TransactionRowItem(tx = tx)
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    tag: String,
    badge: String? = null,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag(tag)
            .padding(4.dp)
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(BinaSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = BinaPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(BinaTertiary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badge,
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = BinaTextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) BinaSurfaceHighlight else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) BinaPrimary else BinaTextSecondary,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun AssetRowItem(
    asset: CryptoAsset,
    onTradeClick: () -> Unit
) {
    BinaCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onTradeClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CryptoIconBadge(
                symbol = asset.symbol,
                color = Color(asset.hexColor),
                sizeDp = 44
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = asset.symbol,
                        color = BinaTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = asset.name,
                        color = BinaTextSecondary,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "RM %,.2f".format(asset.priceUsd),
                    color = BinaTextSecondary,
                    fontSize = 13.sp
                )
            }

            // Sparkline
            SparklineChart(
                data = asset.sparkline,
                color = if (asset.change24h >= 0) BinaProfitGreen else BinaLossRed,
                modifier = Modifier
                    .width(60.dp)
                    .height(26.dp)
                    .padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Holdings
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "RM %,.2f".format(asset.balanceUsd),
                    color = BinaTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                val isPositive = asset.change24h >= 0
                Text(
                    text = "${if (isPositive) "+" else ""}${"%.2f".format(asset.change24h)}%",
                    color = if (isPositive) BinaProfitGreen else BinaLossRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun TransactionRowItem(tx: WalletTransaction) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    val formattedDate = dateFormat.format(Date(tx.timestamp))

    BinaCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when (tx.type) {
                com.example.model.TransactionType.SEND -> Icons.AutoMirrored.Filled.Send
                com.example.model.TransactionType.RECEIVE -> Icons.Default.CallReceived
                com.example.model.TransactionType.SWAP -> Icons.Default.SwapHoriz
                com.example.model.TransactionType.BOT_TRADE -> Icons.Default.SmartToy
            }
            val iconColor = when (tx.type) {
                com.example.model.TransactionType.SEND -> BinaLossRed
                com.example.model.TransactionType.RECEIVE -> BinaProfitGreen
                com.example.model.TransactionType.SWAP -> BinaSecondary
                com.example.model.TransactionType.BOT_TRADE -> BinaTertiary
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${tx.type.name} ${tx.assetSymbol}",
                        color = BinaTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (tx.requiredSignatures > 1) {
                        Spacer(modifier = Modifier.width(6.dp))
                        StatusPill(
                            text = "${tx.cosignersSigned}/${tx.requiredSignatures} Multi-Sig",
                            color = BinaTertiary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formattedDate,
                    color = BinaTextMuted,
                    fontSize = 11.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                val isCredit = tx.type == com.example.model.TransactionType.RECEIVE
                Text(
                    text = "${if (isCredit) "+" else "-"}${tx.amount} ${tx.assetSymbol.split("->").first()}",
                    color = if (isCredit) BinaProfitGreen else BinaTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "RM %,.2f".format(tx.amountUsd),
                    color = BinaTextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun MainCoinPriceCard(
    asset: CryptoAsset,
    onClick: () -> Unit
) {
    val isPositive = asset.change24h >= 0
    Surface(
        modifier = Modifier
            .width(150.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color = BinaSurface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BinaBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CryptoIconBadge(
                        symbol = asset.symbol,
                        color = Color(asset.hexColor),
                        sizeDp = 28
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = asset.symbol,
                        color = BinaTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Surface(
                    color = (if (isPositive) BinaProfitGreen else BinaLossRed).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${if (isPositive) "+" else ""}${"%.2f".format(asset.change24h)}%",
                        color = if (isPositive) BinaProfitGreen else BinaLossRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "RM %,.2f".format(asset.priceUsd),
                color = BinaTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            SparklineChart(
                data = asset.sparkline,
                color = if (isPositive) BinaProfitGreen else BinaLossRed,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
            )
        }
    }
}

