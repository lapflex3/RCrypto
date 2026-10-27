package com.example.ui.screens

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BinaCard
import com.example.ui.components.TransparentWatermark
import com.example.ui.theme.*
import com.example.viewmodel.BinaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(viewModel: BinaViewModel) {
    var isRegisterTab by remember { mutableStateOf(false) }

    var emailText by remember { mutableStateOf("lapflex100@gmail.com") }
    var passwordText by remember { mutableStateOf("securePass123") }
    var confirmPasswordText by remember { mutableStateOf("securePass123") }
    var passwordVisible by remember { mutableStateOf(false) }

    var showGoogleAccountSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BinaBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // App Brand Logo
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(BinaPrimary, BinaSecondary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "R",
                    color = Color.Black,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "RCRYPTO",
                color = BinaTextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Text(
                text = "Next-Gen Crypto E-Wallet & AI Trading",
                color = BinaTextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Card Container
            BinaCard(modifier = Modifier.fillMaxWidth()) {
                // Tab Switcher: Log Masuk vs Daftar
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
                            .background(if (!isRegisterTab) BinaSurfaceHighlight else Color.Transparent)
                            .clickable { isRegisterTab = false }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Log Masuk",
                            color = if (!isRegisterTab) BinaPrimary else BinaTextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isRegisterTab) BinaSurfaceHighlight else Color.Transparent)
                            .clickable { isRegisterTab = true }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Daftar Akaun",
                            color = if (isRegisterTab) BinaPrimary else BinaTextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Email field
                OutlinedTextField(
                    value = emailText,
                    onValueChange = { emailText = it },
                    label = { Text("Emel atau Nama Pengguna") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = BinaTextSecondary)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_auth_email"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BinaPrimary,
                        unfocusedBorderColor = BinaBorder
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password field
                OutlinedTextField(
                    value = passwordText,
                    onValueChange = { passwordText = it },
                    label = { Text("Kata Laluan") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = BinaTextSecondary)
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Password",
                                tint = BinaTextSecondary
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_auth_password"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BinaPrimary,
                        unfocusedBorderColor = BinaBorder
                    )
                )

                if (isRegisterTab) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = confirmPasswordText,
                        onValueChange = { confirmPasswordText = it },
                        label = { Text("Sahkan Kata Laluan") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.LockReset, contentDescription = null, tint = BinaTextSecondary)
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BinaPrimary,
                            unfocusedBorderColor = BinaBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Primary Submit Button
                Button(
                    onClick = {
                        if (isRegisterTab) {
                            viewModel.registerAccount(emailText, passwordText)
                        } else {
                            viewModel.loginWithEmail(emailText, passwordText)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_auth_submit"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BinaPrimary)
                ) {
                    Text(
                        text = if (isRegisterTab) "Daftar Akaun Sekarang" else "Log Masuk ke RCrypto",
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // OR Divider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(modifier = Modifier.weight(1f), color = BinaBorder)
                    Text(
                        text = "  ATAU  ",
                        color = BinaTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Divider(modifier = Modifier.weight(1f), color = BinaBorder)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Google Sign In Button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showGoogleAccountSheet = true }
                        .testTag("btn_google_signin"),
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Google "G" Logo
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4285F4)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "G",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Log Masuk Melalui Akaun Google",
                            color = Color(0xFF1F1F1F),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Transparent Watermark at bottom
            TransparentWatermark()
        }
    }

    // Google Account Picker Modal Sheet
    if (showGoogleAccountSheet) {
        ModalBottomSheet(
            onDismissRequest = { showGoogleAccountSheet = false },
            containerColor = BinaSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4285F4)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("G", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Pilih Akaun Google",
                        color = BinaTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = BinaBorder, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Account 1: User's Account
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            viewModel.loginWithGoogle("lapflex100@gmail.com", "Nasadef")
                            showGoogleAccountSheet = false
                        }
                        .testTag("google_account_lapflex"),
                    color = BinaSurfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(BinaPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("N", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Nasadef (Utama)", color = BinaTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("lapflex100@gmail.com", color = BinaTextSecondary, fontSize = 12.sp)
                        }
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = BinaPrimary, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Account 2: Demo Institutional Account
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            viewModel.loginWithGoogle("trader@rcrypto.vault", "RCrypto Vault Trader")
                            showGoogleAccountSheet = false
                        },
                    color = BinaSurfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(BinaSecondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("R", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("RCrypto Institutional Trader", color = BinaTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("trader@rcrypto.vault", color = BinaTextSecondary, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Google Identity Services • Dilindungi oleh Enkripsi RCrypto",
                    color = BinaTextMuted,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
