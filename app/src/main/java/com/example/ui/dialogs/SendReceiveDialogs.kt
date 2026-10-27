package com.example.ui.dialogs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CryptoAsset
import com.example.ui.components.CryptoIconBadge
import com.example.ui.components.StatusPill
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendCryptoDialog(
    assets: List<CryptoAsset>,
    onDismiss: () -> Unit,
    onSend: (symbol: String, amount: Double, toAddress: String, note: String) -> Unit
) {
    var selectedSymbol by remember { mutableStateOf(assets.firstOrNull()?.symbol ?: "BTC") }
    var amountText by remember { mutableStateOf("") }
    var addressText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    val selectedAsset = assets.firstOrNull { it.symbol == selectedSymbol }
    val price = selectedAsset?.priceUsd ?: 1.0
    val amount = amountText.toDoubleOrNull() ?: 0.0
    val amountUsd = amount * price
    val isMultiSigTriggered = amountUsd >= 2500.0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BinaSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Send Crypto",
                    color = BinaTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                if (isMultiSigTriggered) {
                    StatusPill(text = "Multi-Sig Protected", color = BinaTertiary)
                }
            }

            // Asset selector
            Text("Select Asset", color = BinaTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                assets.take(5).forEach { asset ->
                    val isSelected = asset.symbol == selectedSymbol
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedSymbol = asset.symbol },
                        color = if (isSelected) BinaPrimary.copy(alpha = 0.2f) else BinaSurfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = asset.symbol,
                            color = if (isSelected) BinaPrimary else BinaTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .wrapContentWidth(Alignment.CenterHorizontally)
                        )
                    }
                }
            }

            // Amount Field
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount ($selectedSymbol)") },
                placeholder = { Text("0.0") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_send_amount"),
                trailingIcon = {
                    TextButton(onClick = {
                        amountText = "%.4f".format(selectedAsset?.balance ?: 0.0)
                    }) {
                        Text("MAX", color = BinaPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                supportingText = {
                    Text(
                        text = "≈ RM %,.2f • Baki: ${selectedAsset?.balance ?: 0.0} $selectedSymbol".format(amountUsd),
                        color = BinaTextMuted
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BinaPrimary,
                    unfocusedBorderColor = BinaBorder
                )
            )

            // Address Field
            OutlinedTextField(
                value = addressText,
                onValueChange = { addressText = it },
                label = { Text("Recipient Public Address") },
                placeholder = { Text("Enter address or ENS / Unstoppable domain") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_send_address"),
                trailingIcon = {
                    IconButton(onClick = {
                        addressText = "0x7F1a94C381E885B49dF36816912401C95e0c52B0"
                    }) {
                        Icon(imageVector = Icons.Default.ContentPaste, contentDescription = "Paste Demo Address", tint = BinaPrimary)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BinaPrimary,
                    unfocusedBorderColor = BinaBorder
                )
            )

            // Multi-Sig Threshold Notification Banner
            if (isMultiSigTriggered) {
                Surface(
                    color = BinaTertiary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = BinaTertiary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Multi-Signature Threshold Exceeded",
                                color = BinaTertiary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Transfers > $2,500 automatically generate a 2-of-3 threshold proposal requiring hardware key approval.",
                                color = BinaTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Submit Button
            Button(
                onClick = {
                    onSend(selectedSymbol, amount, addressText.ifEmpty { "0x98bC...31F4" }, noteText)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_confirm_send"),
                colors = ButtonDefaults.buttonColors(containerColor = BinaPrimary),
                shape = RoundedCornerShape(12.dp),
                enabled = amount > 0.0 && (selectedAsset?.balance ?: 0.0) >= amount
            ) {
                Text(
                    text = if (isMultiSigTriggered) "Authorize & Submit to Multi-Sig" else "Confirm Transfer",
                    color = Color.Black,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveCryptoDialog(
    assets: List<CryptoAsset>,
    onDismiss: () -> Unit
) {
    var selectedSymbol by remember { mutableStateOf(assets.firstOrNull()?.symbol ?: "BTC") }
    val clipboardManager = LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) }

    val selectedAsset = assets.firstOrNull { it.symbol == selectedSymbol }
    val depositAddress = when (selectedSymbol) {
        "BTC" -> "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq"
        "BCH" -> "bitcoincash:qpm2qsznhks23z7629mms6s4cwef74vcwvy22gdx6a"
        "SOL" -> "7Nxvd2nvk5mPjL7R6dKm93KzFzWvP8zQf5v8R8m4KxL1"
        "XMR" -> "888tNkZrPN6JsEgekjMnABU4TBzc2Dt29EPAvkFxbTNsFo5VNcmViKdaZX303"
        "LTC" -> "ltc1qg6v299px94vd2e0j3g7x4z0k9r5p8m2q3l1f0a"
        "ETH" -> "0x7F1a94C381E885B49dF36816912401C95e0c52B0"
        else -> "0x7F1a94C381E885B49dF36816912401C95e0c52B0"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BinaSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Receive $selectedSymbol",
                color = BinaTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            // Asset selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                assets.take(6).forEach { asset ->
                    val isSelected = asset.symbol == selectedSymbol
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                selectedSymbol = asset.symbol
                                isCopied = false
                            },
                        color = if (isSelected) BinaPrimary.copy(alpha = 0.2f) else BinaSurfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = asset.symbol,
                            color = if (isSelected) BinaPrimary else BinaTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(vertical = 6.dp)
                                .wrapContentWidth(Alignment.CenterHorizontally)
                        )
                    }
                }
            }

            Text(
                text = "Network: ${selectedAsset?.network?.displayName ?: "Mainnet"}",
                color = BinaSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            // QR Code Matrix Simulation
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val gridSize = 12
                    val cellSize = size.width / gridSize
                    for (x in 0 until gridSize) {
                        for (y in 0 until gridSize) {
                            val isCorner = (x < 3 && y < 3) || (x > 8 && y < 3) || (x < 3 && y > 8)
                            val isFilled = isCorner || ((x * 7 + y * 13 + selectedSymbol.hashCode()) % 3 == 0)
                            if (isFilled) {
                                drawRect(
                                    color = Color.Black,
                                    topLeft = Offset(x * cellSize, y * cellSize),
                                    size = Size(cellSize * 0.9f, cellSize * 0.9f)
                                )
                            }
                        }
                    }
                }
                CryptoIconBadge(
                    symbol = selectedSymbol,
                    color = Color(selectedAsset?.hexColor ?: 0xFF00F0A0),
                    sizeDp = 36
                )
            }

            // Copy Address Box
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        clipboardManager.setText(AnnotatedString(depositAddress))
                        isCopied = true
                    },
                color = BinaSurfaceVariant,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Deposit Address", color = BinaTextSecondary, fontSize = 11.sp)
                        Text(
                            text = depositAddress,
                            color = BinaTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = if (isCopied) BinaProfitGreen else BinaPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (isCopied) {
                Text("Address copied to clipboard!", color = BinaProfitGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
