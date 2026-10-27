package com.example.ui.dialogs

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CryptoAsset
import com.example.ui.components.StatusPill
import com.example.ui.components.TransparentWatermark
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankWithdrawalDialog(
    assets: List<CryptoAsset>,
    onDismiss: () -> Unit,
    onWithdraw: (bankName: String, accountNumber: String, accountHolder: String, amount: Double, method: String) -> Unit
) {
    var selectedBank by remember { mutableStateOf("Maybank") } // "Maybank" or "CIMB Bank"
    var accountNumber by remember { mutableStateOf("114012345678") }
    var accountHolder by remember { mutableStateOf("NASADEF BIN AHMAD") }
    var amountText by remember { mutableStateOf("1500") }
    var selectedMethod by remember { mutableStateOf("DuitNow Pantas") }

    val myrAsset = assets.firstOrNull { it.symbol == "MYR" }
    val availableMyr = myrAsset?.balance ?: 35000.0
    val amount = amountText.toDoubleOrNull() ?: 0.0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BinaSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PENGELUARAN KE BANK TEMPATAN",
                        color = BinaTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Pengeluaran Ringgit (MYR)",
                        color = BinaTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                StatusPill(text = "DuitNow 24/7", color = BinaProfitGreen)
            }

            // Bank Selection Tabs (Maybank vs CIMB Bank)
            Text(
                text = "Pilih Bank Sasaran:",
                color = BinaTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Maybank Card
                val isMaybank = selectedBank == "Maybank"
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            selectedBank = "Maybank"
                            accountNumber = "114012345678"
                        }
                        .testTag("select_bank_maybank"),
                    color = if (isMaybank) Color(0xFF2A2400) else BinaSurfaceVariant,
                    border = if (isMaybank) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFC800)) else null,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFC800)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("M", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Maybank",
                            color = if (isMaybank) Color(0xFFFFC800) else BinaTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Maybank2u / MAE",
                            color = BinaTextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                // CIMB Bank Card
                val isCimb = selectedBank == "CIMB Bank"
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            selectedBank = "CIMB Bank"
                            accountNumber = "7012345678"
                        }
                        .testTag("select_bank_cimb"),
                    color = if (isCimb) Color(0xFF2A0D10) else BinaSurfaceVariant,
                    border = if (isCimb) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFED1C24)) else null,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFED1C24)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("C", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "CIMB Bank",
                            color = if (isCimb) Color(0xFFED1C24) else BinaTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "CIMB Clicks / OCTO",
                            color = BinaTextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Account Details
            OutlinedTextField(
                value = accountNumber,
                onValueChange = { accountNumber = it },
                label = { Text("Nombor Akaun $selectedBank") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_bank_account"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BinaPrimary,
                    unfocusedBorderColor = BinaBorder
                )
            )

            OutlinedTextField(
                value = accountHolder,
                onValueChange = { accountHolder = it },
                label = { Text("Nama Pemegang Akaun") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BinaPrimary,
                    unfocusedBorderColor = BinaBorder
                )
            )

            // Amount Field
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Jumlah Pengeluaran (RM)") },
                prefix = { Text("RM ", color = BinaPrimary, fontWeight = FontWeight.Bold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_withdraw_amount"),
                trailingIcon = {
                    TextButton(onClick = { amountText = "%.2f".format(availableMyr) }) {
                        Text("MAX", color = BinaPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                supportingText = {
                    Text(
                        text = "Baki Tersedia: RM %,.2f".format(availableMyr),
                        color = BinaTextMuted
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BinaPrimary,
                    unfocusedBorderColor = BinaBorder
                )
            )

            // Transfer Summary Box
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
                        Text("Saluran Pindahan", color = BinaTextSecondary, fontSize = 12.sp)
                        Text("DuitNow Instant Transfer (Pantas)", color = BinaSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tempoh Pindahan", color = BinaTextSecondary, fontSize = 12.sp)
                        Text("Serta-merta (1 - 3 saat)", color = BinaProfitGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Yuran Pemprosesan", color = BinaTextSecondary, fontSize = 12.sp)
                        Text("RM 0.00 (Percuma)", color = BinaProfitGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Submit Button
            Button(
                onClick = {
                    onWithdraw(selectedBank, accountNumber, accountHolder, amount, selectedMethod)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_confirm_withdraw"),
                colors = ButtonDefaults.buttonColors(containerColor = BinaPrimary),
                shape = RoundedCornerShape(12.dp),
                enabled = amount > 0.0 && availableMyr >= amount
            ) {
                Text(
                    text = if (availableMyr < amount) "Baki MYR Tidak Mencukupi" else "Keluarkan RM %,.2f ke $selectedBank".format(amount),
                    color = Color.Black,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Transparent Watermark
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TransparentWatermark()
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
