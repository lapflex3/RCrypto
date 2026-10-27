package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.model.CryptoAsset
import com.example.ui.components.BinaCard
import com.example.ui.components.CryptoIconBadge
import com.example.ui.components.StatusPill
import com.example.ui.theme.*
import com.example.viewmodel.BinaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwapScreen(viewModel: BinaViewModel) {
    val assets by viewModel.assets.collectAsState()

    var fromSymbol by remember { mutableStateOf("MYR") }
    var toSymbol by remember { mutableStateOf("BTC") }
    var fromAmountText by remember { mutableStateOf("1000") }
    var slippageTolerance by remember { mutableStateOf(0.5) }

    var showFromAssetSheet by remember { mutableStateOf(false) }
    var showToAssetSheet by remember { mutableStateOf(false) }

    val fromAsset = assets.firstOrNull { it.symbol == fromSymbol }
    val toAsset = assets.firstOrNull { it.symbol == toSymbol }

    val fromPrice = fromAsset?.priceUsd ?: 1.0
    val toPrice = toAsset?.priceUsd ?: 421950.0

    val fromAmount = fromAmountText.toDoubleOrNull() ?: 0.0
    val rate = if (toPrice > 0.0) fromPrice / toPrice else 0.0
    val calculatedToAmount = fromAmount * rate

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
                        text = "RCRYPTO DEX & PERTUKARAN WANG",
                        color = BinaTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Pertukaran MYR & Kripto",
                        color = BinaTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                StatusPill(text = "FPX & DuitNow Aktif", color = BinaPrimary)
            }
        }

        // Swap Card Box
        item {
            BinaCard(modifier = Modifier.fillMaxWidth()) {
                // FROM BOX
                Text(
                    text = "ANDA BAYAR (MYR / KRIPTO)",
                    color = BinaTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BinaSurfaceVariant)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = fromAmountText,
                            onValueChange = { fromAmountText = it },
                            placeholder = { Text("0.0", color = BinaTextMuted) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = BinaTextPrimary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_swap_from_amount")
                        )
                        val usdEstimate = fromAmount * fromPrice
                        Text(
                            text = "≈ RM %,.2f".format(usdEstimate),
                            color = BinaTextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }

                    // Token Selector Pill
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { showFromAssetSheet = true }
                            .testTag("select_from_asset"),
                        color = BinaSurfaceHighlight,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (fromAsset != null) {
                                CryptoIconBadge(
                                    symbol = fromAsset.symbol,
                                    color = Color(fromAsset.hexColor),
                                    sizeDp = 24
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = fromSymbol,
                                color = BinaTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = BinaTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Baki: ${if (fromSymbol == "MYR") "RM %,.2f".format(fromAsset?.balance ?: 0.0) else "${"%.4f".format(fromAsset?.balance ?: 0.0)} $fromSymbol"}",
                        color = BinaTextSecondary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "MAKS",
                        color = BinaPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            fromAmountText = if (fromSymbol == "MYR") "%.2f".format(fromAsset?.balance ?: 0.0) else "%.4f".format(fromAsset?.balance ?: 0.0)
                        }
                    )
                }

                // Invert Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BinaPrimary)
                            .clickable {
                                val temp = fromSymbol
                                fromSymbol = toSymbol
                                toSymbol = temp
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "Invert Tokens",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // TO BOX
                Text(
                    text = "ANDA TERIMA (ANGGARAN)",
                    color = BinaTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BinaSurfaceVariant)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (toSymbol == "MYR") "RM %,.2f".format(calculatedToAmount) else "%.6f".format(calculatedToAmount),
                            color = BinaTextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                        val usdEstimate = calculatedToAmount * toPrice
                        Text(
                            text = "≈ RM %,.2f".format(usdEstimate),
                            color = BinaTextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }

                    // Token Selector Pill
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { showToAssetSheet = true }
                            .testTag("select_to_asset"),
                        color = BinaSurfaceHighlight,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (toAsset != null) {
                                CryptoIconBadge(
                                    symbol = toAsset.symbol,
                                    color = Color(toAsset.hexColor),
                                    sizeDp = 24
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = toSymbol,
                                color = BinaTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = BinaTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Rate Breakdown & Routing Path
                Surface(
                    color = BinaBackground,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Kadar Pertukaran", color = BinaTextSecondary, fontSize = 12.sp)
                            Text("1 $fromSymbol = ${"%.6f".format(rate)} $toSymbol", color = BinaTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Laluan Pertukaran", color = BinaTextSecondary, fontSize = 12.sp)
                            Text("$fromSymbol → DuitNow FPX Gateway → $toSymbol", color = BinaSecondary, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Toleransi Gelinciran", color = BinaTextSecondary, fontSize = 12.sp)
                            Text("$slippageTolerance%", color = BinaTextPrimary, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Yuran Rangkaian", color = BinaTextSecondary, fontSize = 12.sp)
                            Text("RM 0.00 (Ditaja oleh RCrypto L2)", color = BinaProfitGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Execute Button
                Button(
                    onClick = {
                        viewModel.executeSwap(fromSymbol, toSymbol, fromAmount, calculatedToAmount, rate)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_execute_swap"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BinaPrimary),
                    enabled = fromAmount > 0.0 && (fromAsset?.balance ?: 0.0) >= fromAmount
                ) {
                    Text(
                        text = if ((fromAsset?.balance ?: 0.0) < fromAmount) "Baki $fromSymbol Tidak Mencukupi" else "Tukar Wang Sekarang",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Quick Swap Pairs in MYR
        item {
            Text(
                text = "PASANGAN PERTUKARAN POPULAR (MYR)",
                color = BinaTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            val quickPairs = listOf(
                Pair("MYR", "BTC"),
                Pair("MYR", "ETH"),
                Pair("MYR", "SOL"),
                Pair("MYR", "USDT"),
                Pair("MYR", "BCH"),
                Pair("MYR", "XMR")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickPairs.take(3).forEach { (f, t) ->
                    QuickPairChip(
                        from = f,
                        to = t,
                        onClick = {
                            fromSymbol = f
                            toSymbol = t
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickPairs.drop(3).forEach { (f, t) ->
                    QuickPairChip(
                        from = f,
                        to = t,
                        onClick = {
                            fromSymbol = f
                            toSymbol = t
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // Modal Bottom Sheet for From Asset
    if (showFromAssetSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFromAssetSheet = false },
            containerColor = BinaSurface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select Pay Token", color = BinaTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                assets.forEach { asset ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                fromSymbol = asset.symbol
                                showFromAssetSheet = false
                            }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CryptoIconBadge(symbol = asset.symbol, color = Color(asset.hexColor), sizeDp = 32)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(asset.symbol, color = BinaTextPrimary, fontWeight = FontWeight.Bold)
                            Text(asset.name, color = BinaTextSecondary, fontSize = 12.sp)
                        }
                        Text("RM %,.2f".format(asset.priceUsd), color = BinaTextSecondary, fontSize = 13.sp)
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet for To Asset
    if (showToAssetSheet) {
        ModalBottomSheet(
            onDismissRequest = { showToAssetSheet = false },
            containerColor = BinaSurface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select Receive Token", color = BinaTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                assets.forEach { asset ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                toSymbol = asset.symbol
                                showToAssetSheet = false
                            }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CryptoIconBadge(symbol = asset.symbol, color = Color(asset.hexColor), sizeDp = 32)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(asset.symbol, color = BinaTextPrimary, fontWeight = FontWeight.Bold)
                            Text(asset.name, color = BinaTextSecondary, fontSize = 12.sp)
                        }
                        Text("RM %,.2f".format(asset.priceUsd), color = BinaTextSecondary, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun QuickPairChip(from: String, to: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = BinaSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, BinaBorder),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = "$from → $to",
            color = BinaTextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 8.dp).wrapContentWidth(Alignment.CenterHorizontally)
        )
    }
}
